# Dev Observability Stack Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give the dev environment searchable logs, JVM/HTTP metrics, and Kubernetes state, all in-cluster and ephemeral, so failures can be diagnosed without kubectl archaeology.

**Architecture:** Three Argo CD Applications (`kube-prometheus-stack`, `loki`, `alloy`) deploy into a new `monitoring` namespace ahead of the app's sync wave. The Spring Boot app gains a Micrometer Prometheus registry and JSON logging, and moves actuator to a dedicated management port that ingress never routes. All telemetry storage is `emptyDir` and dies with the cluster by design.

**Tech Stack:** Spring Boot 2.7.4 / Java 11, Micrometer 1.9.x, logstash-logback-encoder 7.x, Helm, Argo CD, kube-prometheus-stack 88.3.0, Loki 7.3.0, Grafana Alloy 1.11.1.

**Source spec:** `docs/superpowers/specs/2026-08-08-observability-design.md`

## Global Constraints

- **Java 11 only.** The project sets `<java.version>11</java.version>` (`pom.xml:28`) and CI uses Temurin 11. Spring Boot 2.7 does not support JDK 21. Before any Maven command: `export JAVA_HOME=$(/usr/libexec/java_home -v 11)`. Verify with `java -version` before running `mvn`.
- **logstash-logback-encoder must be the 7.x line.** The 8.x line requires logback 1.3+; Boot 2.7.4 resolves logback 1.2.11. Using 8.x produces `NoClassDefFoundError` at runtime, not at compile time.
- **Chart versions are exact pins, never ranges.** `kube-prometheus-stack` `88.3.0`, `loki` `7.3.0`, `alloy` `1.11.1`. These were resolved against the live chart repositories on 2026-08-13.
- **All observability storage is `emptyDir` with an explicit `sizeLimit`.** No PVCs. An unbounded `emptyDir` that fills the node's disk evicts every pod on it, including `okrs-backend`.
- **Every observability workload sets `priorityClassName: okrs-observability`** (negative priority) so memory pressure evicts monitoring before the application.
- **No new public ingress.** Grafana is reached by port-forward only.
- **Do not modify `deploy/charts/okrs-backend/values/dev.yaml:3`** (`tag: REPLACE_WITH_IMAGE_SHA`). That line is owned by the CI pin step (`.github/workflows/ci.yml:64-77`).
- **Use absolute binary paths in verification steps that count or measure output.** This workstation runs an `rtk` shell hook that rewrites and compacts command output. A bare `helm template ... | grep -c` silently returns counts computed from truncated output — a full `kube-prometheus-stack` render is 431 KB but reads as 1.3 KB through the wrapper, which makes every check look like it failed. The verification steps below use `$(command -v helm)` and `/usr/bin/grep` for this reason. Do not "simplify" them back to bare names.

## Deviations from the spec, and why

Two, both discovered while verifying against the real charts and codebase. Flag to the reviewer if either is unacceptable.

1. **The `/actuator/prometheus` HTTP integration test is replaced by an `ApplicationContextRunner` wiring test.** The spec calls for an HTTP-level assertion. `okrs-api` has **no `src/test` directory at all** and no test infrastructure; a `@SpringBootTest` would boot the full context and require live PostgreSQL, Redis, and SMTP. Standing up Testcontainers is well outside this scope. The substitute test asserts the `PrometheusMeterRegistry` bean is created by our configuration and that its scrape output contains `jvm_memory_used_bytes` — it verifies the same wiring risk without infrastructure. Task 11 adds a manual `curl` smoke check against the running pod to cover the HTTP layer.

2. **Sync waves are `-16` for `kube-prometheus-stack` and `-15` for `loki` and `alloy`**, not `-15` for all three as the spec states. `kube-prometheus-stack` creates both the `ServiceMonitor` CRD and the `okrs-observability` PriorityClass. Pods referencing a PriorityClass that does not yet exist are rejected outright by the API server, and same-wave ordering is not guaranteed.

## File Structure

**Created:**
- `okrs-api/src/test/java/org/ptit/okrs/api/observability/PrometheusMetricsWiringTest.java` — proves the Micrometer registry is wired
- `okrs-api/src/test/java/org/ptit/okrs/api/observability/JsonLoggingDependencyTest.java` — proves the logback encoder is loadable at the resolved version
- `okrs-api/src/main/resources/logback-spring.xml` — profile-selected plain vs JSON console output
- `deploy/charts/okrs-backend/templates/servicemonitor.yaml` — scrape target for the app, gated off by default
- `deploy/argocd/apps/monitoring-kube-prometheus-stack.yaml` — Prometheus, Grafana, operator, kube-state-metrics, node-exporter, PriorityClass
- `deploy/argocd/apps/monitoring-loki.yaml` — log store
- `deploy/argocd/apps/monitoring-alloy.yaml` — log collector

**Modified:**
- `okrs-api/pom.xml:36-49` — two dependencies
- `okrs-api/src/main/resources/application.yml:88-96` — management port, exposure, metric tags
- `deploy/charts/okrs-backend/values.yaml` — `metrics` block, `management` port
- `deploy/charts/okrs-backend/values/dev.yaml` — enable ServiceMonitor
- `deploy/charts/okrs-backend/templates/deployment.yaml:33-55` — management port and probe retarget
- `deploy/charts/okrs-backend/templates/service.yaml:9-14` — expose management port
- `deploy/argocd/apps/projects.yaml:26-46,66-78` — four allowlist edits
- `scripts/validate-deploy.sh:17-26` — ServiceMonitor render + Argo manifest schema pass
- `.github/workflows/deployment-validation.yml:38-45` — install kubeconform
- `.github/workflows/ci.yml:3-13` — PR-time verification job
- `Makefile:14-17,109-113` — Grafana targets
- `docs/deployment.md` — observability section

---

### Task 1: Micrometer Prometheus registry

**Files:**
- Modify: `okrs-api/pom.xml:36-49`
- Modify: `okrs-api/src/main/resources/application.yml:88-96`
- Test: `okrs-api/src/test/java/org/ptit/okrs/api/observability/PrometheusMetricsWiringTest.java`

**Interfaces:**
- Consumes: nothing.
- Produces: a `PrometheusMeterRegistry` bean in the application context, and the `/actuator/prometheus` endpoint. Task 4's ServiceMonitor scrapes the path `/actuator/prometheus`. Task 3 moves it to port 8081.

- [ ] **Step 1: Set the JDK and confirm it**

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
java -version
```

Expected: output mentions `11.` — if it says 21, stop and install JDK 11. Every later Maven step in this plan assumes this export is active in the shell.

- [ ] **Step 2: Write the failing test**

Create `okrs-api/src/test/java/org/ptit/okrs/api/observability/PrometheusMetricsWiringTest.java`. The directory does not exist yet; create the full path.

```java
package org.ptit.okrs.api.observability;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.JvmMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class PrometheusMetricsWiringTest {

  private final ApplicationContextRunner runner =
      new ApplicationContextRunner()
          .withConfiguration(
              AutoConfigurations.of(
                  MetricsAutoConfiguration.class,
                  CompositeMeterRegistryAutoConfiguration.class,
                  JvmMetricsAutoConfiguration.class,
                  PrometheusMetricsExportAutoConfiguration.class));

  @Test
  void prometheusRegistryIsCreated() {
    runner.run(context -> assertThat(context).hasSingleBean(PrometheusMeterRegistry.class));
  }

  @Test
  void scrapeOutputContainsJvmMemoryMetrics() {
    runner.run(
        context -> {
          String scrape = context.getBean(PrometheusMeterRegistry.class).scrape();
          assertThat(scrape).contains("jvm_memory_used_bytes");
        });
  }

  @Test
  void scrapeOutputContainsCommonTags() {
    runner
        .withPropertyValues(
            "management.metrics.tags.application=okrs-backend",
            "management.metrics.tags.environment=test")
        .run(
            context -> {
              String scrape = context.getBean(PrometheusMeterRegistry.class).scrape();
              assertThat(scrape).contains("application=\"okrs-backend\"");
              assertThat(scrape).contains("environment=\"test\"");
            });
  }
}
```

- [ ] **Step 3: Run the test to verify it fails**

```bash
./mvnw --batch-mode -pl okrs-api -am test -Dtest=PrometheusMetricsWiringTest
```

Expected: FAIL. Compilation error — `package io.micrometer.prometheus does not exist`. The registry dependency is not yet present.

- [ ] **Step 4: Add the registry dependency**

In `okrs-api/pom.xml`, inside `<dependencies>`, after the `spring-boot-starter-actuator` block (ends line 49), add. No `<version>` — `spring-boot-dependencies` 2.7.4 manages it to Micrometer 1.9.x.

```xml
    <dependency>
      <groupId>io.micrometer</groupId>
      <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>
```

- [ ] **Step 5: Run the test to verify it passes**

```bash
./mvnw --batch-mode -pl okrs-api -am test -Dtest=PrometheusMetricsWiringTest
```

Expected: PASS, 3 tests.

- [ ] **Step 6: Widen actuator exposure and add metric tags**

In `okrs-api/src/main/resources/application.yml`, replace the `management` block that currently occupies lines 88-96:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health
  endpoint:
    health:
      probes:
        enabled: true
```

with:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus
  endpoint:
    health:
      probes:
        enabled: true
  metrics:
    tags:
      application: okrs-backend
      environment: ${SPRING_PROFILES_ACTIVE:local}
```

Do not add `metrics`, `env`, or `heapdump` to the exposure list. `probes.enabled: true` must stay — the Kubernetes probes depend on it.

- [ ] **Step 7: Verify the app still compiles**

```bash
./mvnw --batch-mode -pl okrs-api -am test-compile
```

Expected: BUILD SUCCESS.

- [ ] **Step 8: Commit**

```bash
git add okrs-api/pom.xml okrs-api/src/main/resources/application.yml okrs-api/src/test/java/org/ptit/okrs/api/observability/PrometheusMetricsWiringTest.java
git commit -m "feat(observability): add Micrometer Prometheus registry and metric tags"
```

---

### Task 2: JSON logging

**Files:**
- Modify: `okrs-api/pom.xml`
- Create: `okrs-api/src/main/resources/logback-spring.xml`
- Test: `okrs-api/src/test/java/org/ptit/okrs/api/observability/JsonLoggingDependencyTest.java`

**Interfaces:**
- Consumes: nothing from Task 1.
- Produces: JSON log lines on stdout under the `dev` and `qa` profiles, with fields `level`, `logger_name`, `message`, `thread_name`, `stack_trace`. Task 9's Alloy config parses `level` and `logger_name` by those exact names.

- [ ] **Step 1: Write the failing test**

Create `okrs-api/src/test/java/org/ptit/okrs/api/observability/JsonLoggingDependencyTest.java`. This guards the 7.x-vs-8.x incompatibility, which fails at runtime rather than compile time.

```java
package org.ptit.okrs.api.observability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.Level;
import net.logstash.logback.encoder.LogstashEncoder;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class JsonLoggingDependencyTest {

  @Test
  void logstashEncoderIsOnClasspathAndStartable() {
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    LogstashEncoder encoder = new LogstashEncoder();
    encoder.setContext(context);

    assertThatCode(encoder::start).doesNotThrowAnyException();
    assertThat(encoder.isStarted()).isTrue();
  }

  @Test
  void encodedEventIsJsonWithExpectedFields() {
    LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
    LogstashEncoder encoder = new LogstashEncoder();
    encoder.setContext(context);
    encoder.start();

    LoggingEvent event = new LoggingEvent();
    event.setLoggerContext(context);
    event.setLoggerName("org.ptit.okrs.api.SampleLogger");
    event.setLevel(Level.WARN);
    event.setMessage("sample message");
    event.setThreadName("main");

    String encoded = new String(encoder.encode(event));

    assertThat(encoded).startsWith("{");
    assertThat(encoded).contains("\"level\":\"WARN\"");
    assertThat(encoded).contains("\"logger_name\":\"org.ptit.okrs.api.SampleLogger\"");
    assertThat(encoded).contains("\"message\":\"sample message\"");
  }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
./mvnw --batch-mode -pl okrs-api -am test -Dtest=JsonLoggingDependencyTest
```

Expected: FAIL. Compilation error — `package net.logstash.logback.encoder does not exist`.

- [ ] **Step 3: Add the encoder dependency**

In `okrs-api/pom.xml`, after the `micrometer-registry-prometheus` block added in Task 1, add. The version is explicit and must stay on the 7.x line.

```xml
    <dependency>
      <groupId>net.logstash.logback</groupId>
      <artifactId>logstash-logback-encoder</artifactId>
      <version>7.4</version>
    </dependency>
```

- [ ] **Step 4: Run the test to verify it passes**

```bash
./mvnw --batch-mode -pl okrs-api -am test -Dtest=JsonLoggingDependencyTest
```

Expected: PASS, 2 tests. If this fails with `NoClassDefFoundError` mentioning `ch.qos.logback`, the encoder version is incompatible with the logback that Boot 2.7.4 resolves — drop to `7.3` and re-run. Do not "fix" it by upgrading logback.

- [ ] **Step 5: Create the logback configuration**

Create `okrs-api/src/main/resources/logback-spring.xml`. The filename matters: `logback.xml` is read before Spring initialises and the `<springProfile>` blocks would be ignored.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
  <include resource="org/springframework/boot/logging/logback/defaults.xml"/>

  <springProfile name="dev,qa">
    <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
      <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <includeMdcKeyName>traceId</includeMdcKeyName>
        <includeMdcKeyName>spanId</includeMdcKeyName>
      </encoder>
    </appender>
    <root level="INFO">
      <appender-ref ref="JSON"/>
    </root>
  </springProfile>

  <springProfile name="!dev &amp; !qa">
    <appender name="PLAIN" class="ch.qos.logback.core.ConsoleAppender">
      <encoder>
        <pattern>${CONSOLE_LOG_PATTERN}</pattern>
        <charset>UTF-8</charset>
      </encoder>
    </appender>
    <root level="INFO">
      <appender-ref ref="PLAIN"/>
    </root>
  </springProfile>
</configuration>
```

- [ ] **Step 6: Verify both profiles produce the expected format**

```bash
./mvnw --batch-mode -pl okrs-api -am test-compile
```

Expected: BUILD SUCCESS. Runtime format is verified against the live pod in Task 11; a full app boot is not possible here without a database.

- [ ] **Step 7: Commit**

```bash
git add okrs-api/pom.xml okrs-api/src/main/resources/logback-spring.xml okrs-api/src/test/java/org/ptit/okrs/api/observability/JsonLoggingDependencyTest.java
git commit -m "feat(observability): emit JSON logs on dev and qa profiles"
```

---

### Task 3: Split actuator onto a dedicated management port

**Files:**
- Modify: `okrs-api/src/main/resources/application.yml` (the `management` block from Task 1)
- Modify: `deploy/charts/okrs-backend/values.yaml:11-14`
- Modify: `deploy/charts/okrs-backend/templates/deployment.yaml:33-55`
- Modify: `deploy/charts/okrs-backend/templates/service.yaml:9-14`

**Interfaces:**
- Consumes: the actuator exposure from Task 1.
- Produces: a container port named `management` on 8081, a Service port named `management`, and probes targeting it. Task 4's ServiceMonitor references the Service port by the name `management`.

- [ ] **Step 1: Move actuator to its own port**

In `okrs-api/src/main/resources/application.yml`, add `server.port` under the `management` key so the block reads:

```yaml
management:
  server:
    port: 8081
  endpoints:
    web:
      exposure:
        include: health,prometheus
  endpoint:
    health:
      probes:
        enabled: true
  metrics:
    tags:
      application: okrs-backend
      environment: ${SPRING_PROFILES_ACTIVE:local}
```

- [ ] **Step 2: Add the management port to chart values**

In `deploy/charts/okrs-backend/values.yaml`, replace the `service` block at lines 11-14:

```yaml
service:
  type: ClusterIP
  port: 8080
```

with:

```yaml
service:
  type: ClusterIP
  port: 8080
  managementPort: 8081
```

- [ ] **Step 3: Add the container port and retarget the probes**

In `deploy/charts/okrs-backend/templates/deployment.yaml`, replace lines 33-55 (from `ports:` through the end of `readinessProbe`) with:

```yaml
          ports:
            - name: http
              containerPort: 8080
              protocol: TCP
            - name: management
              containerPort: {{ .Values.service.managementPort }}
              protocol: TCP
          startupProbe:
            httpGet:
              path: /actuator/health/liveness
              port: management
            initialDelaySeconds: {{ .Values.probes.startup.initialDelaySeconds }}
            periodSeconds: {{ .Values.probes.startup.periodSeconds }}
            failureThreshold: {{ .Values.probes.startup.failureThreshold }}
          livenessProbe:
            httpGet:
              path: /actuator/health/liveness
              port: management
            periodSeconds: {{ .Values.probes.liveness.periodSeconds }}
            failureThreshold: {{ .Values.probes.liveness.failureThreshold }}
          readinessProbe:
            httpGet:
              path: /actuator/health/readiness
              port: management
            periodSeconds: {{ .Values.probes.readiness.periodSeconds }}
            failureThreshold: {{ .Values.probes.readiness.failureThreshold }}
```

- [ ] **Step 4: Expose the management port on the Service**

In `deploy/charts/okrs-backend/templates/service.yaml`, replace the `ports:` list at lines 9-14 with:

```yaml
  ports:
    - port: {{ .Values.service.port }}
      targetPort: http
      protocol: TCP
      name: http
    - port: {{ .Values.service.managementPort }}
      targetPort: management
      protocol: TCP
      name: management
```

The Ingress template references `.Values.service.port` only (`ingress.yaml:26`) and is deliberately left untouched, so the management port stays unroutable from outside.

- [ ] **Step 5: Verify the chart renders both ports**

```bash
helm template okrs-backend deploy/charts/okrs-backend \
  --values deploy/charts/okrs-backend/values/dev.yaml \
  | grep -A2 'name: management'
```

Expected: two matches — `containerPort: 8081` in the Deployment and `targetPort: management` in the Service.

- [ ] **Step 6: Confirm no probe still points at the http port**

```bash
helm template okrs-backend deploy/charts/okrs-backend \
  --values deploy/charts/okrs-backend/values/dev.yaml \
  | grep -B1 -A1 'path: /actuator'
```

Expected: three `port: management` lines, zero `port: http` lines adjacent to an `/actuator` path.

- [ ] **Step 7: Run the full validation suite**

```bash
make validate-deploy
```

Expected: `[validate] Deployment artifacts passed available checks.`

- [ ] **Step 8: Commit**

```bash
git add okrs-api/src/main/resources/application.yml deploy/charts/okrs-backend/values.yaml deploy/charts/okrs-backend/templates/deployment.yaml deploy/charts/okrs-backend/templates/service.yaml
git commit -m "feat(observability): move actuator to a dedicated management port"
```

---

### Task 4: ServiceMonitor template and its validation

**Files:**
- Create: `deploy/charts/okrs-backend/templates/servicemonitor.yaml`
- Modify: `deploy/charts/okrs-backend/values.yaml` (append `metrics` block)
- Modify: `deploy/charts/okrs-backend/values/dev.yaml` (append `metrics` block)
- Modify: `scripts/validate-deploy.sh:17-26`

**Interfaces:**
- Consumes: the Service port named `management` from Task 3.
- Produces: a `ServiceMonitor` named after `okrs-backend.fullname` in namespace `okrs-dev`. Task 5 whitelists this kind; Task 7 configures Prometheus to discover it.

- [ ] **Step 1: Add the gating values**

Append to `deploy/charts/okrs-backend/values.yaml`:

```yaml
metrics:
  serviceMonitor:
    enabled: false
    path: /actuator/prometheus
    interval: 30s
    scrapeTimeout: 10s
```

Default `false` so `local.yaml` and `qa.yaml` are unaffected.

- [ ] **Step 2: Enable it for dev**

Append to `deploy/charts/okrs-backend/values/dev.yaml`:

```yaml
metrics:
  serviceMonitor:
    enabled: true
```

- [ ] **Step 3: Create the template**

Create `deploy/charts/okrs-backend/templates/servicemonitor.yaml`:

```yaml
{{- if .Values.metrics.serviceMonitor.enabled -}}
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: {{ include "okrs-backend.fullname" . }}
  labels:
    {{- include "okrs-backend.labels" . | nindent 4 }}
spec:
  selector:
    matchLabels:
      {{- include "okrs-backend.selectorLabels" . | nindent 6 }}
  endpoints:
    - port: management
      path: {{ .Values.metrics.serviceMonitor.path }}
      interval: {{ .Values.metrics.serviceMonitor.interval }}
      scrapeTimeout: {{ .Values.metrics.serviceMonitor.scrapeTimeout }}
{{- end }}
```

- [ ] **Step 4: Verify it renders for dev and stays absent elsewhere**

```bash
echo "--- dev (expect ServiceMonitor) ---"
helm template okrs-backend deploy/charts/okrs-backend --values deploy/charts/okrs-backend/values/dev.yaml | grep -c "kind: ServiceMonitor"
echo "--- local (expect 0) ---"
helm template okrs-backend deploy/charts/okrs-backend --values deploy/charts/okrs-backend/values/local.yaml | grep -c "kind: ServiceMonitor"
echo "--- qa (expect 0) ---"
helm template okrs-backend deploy/charts/okrs-backend --values deploy/charts/okrs-backend/values/qa.yaml | grep -c "kind: ServiceMonitor"
```

Expected: `1`, then `0`, then `0`.

- [ ] **Step 5: Add the explicit gated render to the validation script**

In `scripts/validate-deploy.sh`, after line 26 (the `eso-environment` template line), add:

```bash
helm template okrs-backend deploy/charts/okrs-backend --values deploy/charts/okrs-backend/values/local.yaml --set metrics.serviceMonitor.enabled=true >/dev/null
```

Without this, the default-off template is never exercised by CI.

- [ ] **Step 6: Run validation**

```bash
make validate-deploy
```

Expected: `[validate] Deployment artifacts passed available checks.`

- [ ] **Step 7: Commit**

```bash
git add deploy/charts/okrs-backend/templates/servicemonitor.yaml deploy/charts/okrs-backend/values.yaml deploy/charts/okrs-backend/values/dev.yaml scripts/validate-deploy.sh
git commit -m "feat(observability): add gated ServiceMonitor to the okrs-backend chart"
```

---

### Task 5: AppProject allowlist edits

**Files:**
- Modify: `deploy/argocd/apps/projects.yaml:26-46,66-78`

**Interfaces:**
- Consumes: nothing.
- Produces: permission for Tasks 6-9 to sync. Without this task every later Argo Application fails, and the ServiceMonitor from Task 4 is silently dropped.

- [ ] **Step 1: Allow the two chart repositories**

In `deploy/argocd/apps/projects.yaml`, in the `okrs-platform` AppProject, replace the `sourceRepos` list at lines 26-28:

```yaml
  sourceRepos:
    - git@github.com:kamikaze011001/okrs-backend.git
    - https://charts.external-secrets.io
```

with:

```yaml
  sourceRepos:
    - git@github.com:kamikaze011001/okrs-backend.git
    - https://charts.external-secrets.io
    - https://prometheus-community.github.io/helm-charts
    - https://grafana.github.io/helm-charts
```

- [ ] **Step 2: Allow the monitoring namespace**

In the same `okrs-platform` project, replace the `destinations` list at lines 29-33:

```yaml
  destinations:
    - server: https://kubernetes.default.svc
      namespace: external-secrets
    - server: https://kubernetes.default.svc
      namespace: okrs-dev
```

with:

```yaml
  destinations:
    - server: https://kubernetes.default.svc
      namespace: external-secrets
    - server: https://kubernetes.default.svc
      namespace: okrs-dev
    - server: https://kubernetes.default.svc
      namespace: monitoring
```

- [ ] **Step 3: Allow the PriorityClass**

In the same project's `clusterResourceWhitelist` (lines 34-46), append after the `Namespace` entry:

```yaml
    - group: scheduling.k8s.io
      kind: PriorityClass
```

- [ ] **Step 4: Allow the ServiceMonitor in the dev project**

In the `okrs-dev` AppProject, append to `namespaceResourceWhitelist` (lines 66-78):

```yaml
    - group: monitoring.coreos.com
      kind: ServiceMonitor
```

This is the silent failure: without it the app deploys normally and only the ServiceMonitor disappears.

- [ ] **Step 5: Verify the file still parses and contains all four edits**

```bash
python3 -c "import yaml,sys; list(yaml.safe_load_all(open('deploy/argocd/apps/projects.yaml'))); print('parsed ok')"
grep -c "prometheus-community.github.io\|grafana.github.io\|namespace: monitoring\|kind: PriorityClass\|kind: ServiceMonitor" deploy/argocd/apps/projects.yaml
```

Expected: `parsed ok`, then `5`.

- [ ] **Step 6: Commit**

```bash
git add deploy/argocd/apps/projects.yaml
git commit -m "feat(observability): widen AppProject allowlists for the monitoring stack"
```

---

### Task 6: kube-prometheus-stack Application

**Files:**
- Create: `deploy/argocd/apps/monitoring-kube-prometheus-stack.yaml`

**Interfaces:**
- Consumes: the AppProject permissions from Task 5.
- Produces: the `monitoring` namespace, the `ServiceMonitor` CRD, the `okrs-observability` PriorityClass, and a Prometheus reachable in-cluster at `http://kube-prometheus-stack-prometheus.monitoring.svc.cluster.local:9090`. Tasks 7 and 8 set `priorityClassName: okrs-observability`, created here.

- [ ] **Step 1: Create the Application**

Create `deploy/argocd/apps/monitoring-kube-prometheus-stack.yaml`. Wave `-16` is deliberate: it must precede Loki and Alloy at `-15` because they reference the PriorityClass created here.

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: kube-prometheus-stack
  namespace: argocd
  annotations:
    argocd.argoproj.io/sync-wave: "-16"
    argocd.argoproj.io/sync-options: Delete=confirm
spec:
  project: okrs-platform
  source:
    repoURL: https://prometheus-community.github.io/helm-charts
    chart: kube-prometheus-stack
    targetRevision: 88.3.0
    helm:
      releaseName: kube-prometheus-stack
      valuesObject:
        alertmanager:
          enabled: false
        defaultRules:
          create: false
        kubeControllerManager:
          enabled: false
        kubeScheduler:
          enabled: false
        kubeEtcd:
          enabled: false
        kubeProxy:
          enabled: false
        prometheusOperator:
          priorityClassName: okrs-observability
          resources:
            requests:
              memory: 128Mi
            limits:
              memory: 256Mi
        prometheus:
          prometheusSpec:
            priorityClassName: okrs-observability
            retention: 6h
            retentionSize: 3GB
            serviceMonitorSelectorNilUsesHelmValues: false
            podMonitorSelectorNilUsesHelmValues: false
            ruleSelectorNilUsesHelmValues: false
            resources:
              requests:
                cpu: 200m
                memory: 512Mi
              limits:
                memory: 1536Mi
            storageSpec:
              emptyDir:
                sizeLimit: 4Gi
        grafana:
          priorityClassName: okrs-observability
          persistence:
            enabled: false
          resources:
            requests:
              memory: 128Mi
            limits:
              memory: 256Mi
          additionalDataSources:
            - name: Loki
              type: loki
              access: proxy
              url: http://loki.monitoring.svc.cluster.local:3100
              isDefault: false
        kubeStateMetrics:
          enabled: true
        kube-state-metrics:
          priorityClassName: okrs-observability
          resources:
            requests:
              memory: 64Mi
            limits:
              memory: 128Mi
        nodeExporter:
          enabled: true
        prometheus-node-exporter:
          priorityClassName: okrs-observability
          resources:
            requests:
              memory: 64Mi
            limits:
              memory: 128Mi
        extraManifests:
          - apiVersion: scheduling.k8s.io/v1
            kind: PriorityClass
            metadata:
              name: okrs-observability
            value: -100
            globalDefault: false
            description: Evict observability workloads before application workloads.
  destination:
    server: https://kubernetes.default.svc
    namespace: monitoring
  syncPolicy:
    automated:
      enabled: true
      prune: true
      selfHeal: true
      allowEmpty: false
    retry:
      limit: 5
    syncOptions:
      - CreateNamespace=true
      - PruneLast=true
      - ServerSideApply=true
```

`ServerSideApply=true` is required: the Prometheus Operator CRDs exceed the annotation size limit that client-side apply imposes.

`serviceMonitorSelectorNilUsesHelmValues: false` is the setting that lets Prometheus discover the app's ServiceMonitor from Task 4. The chart default is `true` (verified at `values.yaml:4476`), which restricts discovery to ServiceMonitors labelled with this release name — the app chart does not carry that label.

- [ ] **Step 2: Verify the manifest parses**

```bash
python3 -c "import yaml; d=yaml.safe_load(open('deploy/argocd/apps/monitoring-kube-prometheus-stack.yaml')); print(d['kind'], d['spec']['source']['targetRevision'])"
```

Expected: `Application 88.3.0`

- [ ] **Step 3: Render the chart with these values**

```bash
HELM=$(command -v helm)
"$HELM" repo add prometheus-community https://prometheus-community.github.io/helm-charts
"$HELM" repo update prometheus-community
python3 -c "
import yaml
d = yaml.safe_load(open('deploy/argocd/apps/monitoring-kube-prometheus-stack.yaml'))
yaml.safe_dump(d['spec']['source']['helm']['valuesObject'], open('/tmp/kps-test-values.yaml','w'))
print('extracted')
"
"$HELM" template kube-prometheus-stack prometheus-community/kube-prometheus-stack \
  --version 88.3.0 --namespace monitoring \
  --values /tmp/kps-test-values.yaml > /tmp/kps-rendered.yaml
/usr/bin/wc -c /tmp/kps-rendered.yaml
```

Expected: `extracted`, then a size in the **400,000+ byte** range. If it reports roughly 1,300 bytes the output was truncated by the shell wrapper — re-run using the absolute `helm` path as shown, not a bare `helm`.

If the chart repository download fails with `EOF` (it is a ~900 KB tarball and the GitHub CDN is flaky), retry, or fetch once with `curl -sSL --retry 5 -o /tmp/kps.tgz "https://github.com/prometheus-community/helm-charts/releases/download/kube-prometheus-stack-88.3.0/kube-prometheus-stack-88.3.0.tgz"` and template from `/tmp/kps.tgz` instead.

- [ ] **Step 4: Confirm the rendered result matches intent**

```bash
F=/tmp/kps-rendered.yaml
for k in Alertmanager PrometheusRule PriorityClass Prometheus; do
  echo "$k: $(/usr/bin/grep -c "^kind: $k\$" "$F")"
done
echo "priorityClassName: $(/usr/bin/grep -c 'priorityClassName: okrs-observability' "$F")"
/usr/bin/grep -n 'serviceMonitorSelector\|retention:\|sizeLimit' "$F" | head -4
```

Expected exactly: `Alertmanager: 0`, `PrometheusRule: 0`, `PriorityClass: 1`, `Prometheus: 1`, `priorityClassName: 5`, and among the last lines `retention: "6h"`, `serviceMonitorSelector: {}`, `sizeLimit: 4Gi`.

`serviceMonitorSelector: {}` is the one to check most carefully — an empty selector means "discover every ServiceMonitor". If it instead renders with a `matchLabels` block, `serviceMonitorSelectorNilUsesHelmValues: false` did not take effect and the app's ServiceMonitor from Task 4 will never be scraped.

A `priorityClassName` count below 5 means one of the sub-chart value keys is misspelled. The five are Grafana, kube-state-metrics, node-exporter, the operator, and the Prometheus CR. Note that the sub-chart keys are `kube-state-metrics` and `prometheus-node-exporter` (hyphenated, matching the dependency names), while the toggles are `kubeStateMetrics` and `nodeExporter` (camelCase). Both spellings are required and they are not interchangeable.

- [ ] **Step 5: Commit**

```bash
git add deploy/argocd/apps/monitoring-kube-prometheus-stack.yaml
git commit -m "feat(observability): add kube-prometheus-stack Argo CD Application"
```

---

### Task 7: Loki Application

**Files:**
- Create: `deploy/argocd/apps/monitoring-loki.yaml`

**Interfaces:**
- Consumes: the `okrs-observability` PriorityClass and `monitoring` namespace from Task 6.
- Produces: a Loki push endpoint at `http://loki.monitoring.svc.cluster.local:3100/loki/api/v1/push`, consumed by Task 8's Alloy config, and already registered as a Grafana datasource in Task 6.

- [ ] **Step 1: Create the Application**

Create `deploy/argocd/apps/monitoring-loki.yaml`. Every value below overrides a chart default that is wrong for a single-node cluster — the defaults were verified against chart 7.3.0.

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: loki
  namespace: argocd
  annotations:
    argocd.argoproj.io/sync-wave: "-15"
    argocd.argoproj.io/sync-options: Delete=confirm
spec:
  project: okrs-platform
  source:
    repoURL: https://grafana.github.io/helm-charts
    chart: loki
    targetRevision: 7.3.0
    helm:
      releaseName: loki
      valuesObject:
        deploymentMode: SingleBinary
        loki:
          auth_enabled: false
          commonConfig:
            replication_factor: 1
          storage:
            type: filesystem
          schemaConfig:
            configs:
              - from: "2024-04-01"
                store: tsdb
                object_store: filesystem
                schema: v13
                index:
                  prefix: index_
                  period: 24h
          limits_config:
            retention_period: 24h
        singleBinary:
          replicas: 1
          priorityClassName: okrs-observability
          persistence:
            enabled: false
          extraVolumes:
            - name: loki-data
              emptyDir:
                sizeLimit: 3Gi
          extraVolumeMounts:
            - name: loki-data
              mountPath: /var/loki
          resources:
            requests:
              memory: 256Mi
            limits:
              memory: 512Mi
        chunksCache:
          enabled: false
        resultsCache:
          enabled: false
        gateway:
          enabled: false
        lokiCanary:
          enabled: false
        test:
          enabled: false
        backend:
          replicas: 0
        read:
          replicas: 0
        write:
          replicas: 0
  destination:
    server: https://kubernetes.default.svc
    namespace: monitoring
  syncPolicy:
    automated:
      enabled: true
      prune: true
      selfHeal: true
      allowEmpty: false
    retry:
      limit: 5
    syncOptions:
      - CreateNamespace=true
      - PruneLast=true
```

Why each override matters, all confirmed against chart 7.3.0 defaults:
- `deploymentMode` defaults to `SimpleScalable`, which runs separate read/write/backend StatefulSets.
- `singleBinary.replicas` defaults to `0`, so the single-binary pod would never start.
- `chunksCache` defaults to enabled with `allocatedMemory: 8192`, and the chart requests `1.2 ×` that — roughly **9.6 GB**. On a node with ~12.8 GB allocatable this alone would fail to schedule.
- `resultsCache` defaults to enabled with `allocatedMemory: 1024`, roughly 1.2 GB.
- `loki.storage.type` defaults to `s3`; `auth_enabled` defaults to `true`; `replication_factor` defaults to `3`; `schemaConfig` defaults to empty.
- `singleBinary.persistence.enabled` defaults to `true` at 10 Gi, which would create a PVC the spec forbids.

The `extraVolumes` and `extraVolumeMounts` pair is not optional. Setting `persistence.enabled: false`
alone renders a pod with **no volume at all** mounted at `/var/loki` — verified by rendering chart
7.3.0. Loki would then write chunks to the container's writable layer, which has no size bound and
fills the node's disk. The explicit `emptyDir` with `sizeLimit: 3Gi` is what keeps that bounded.

- [ ] **Step 2: Verify the manifest parses**

```bash
python3 -c "import yaml; d=yaml.safe_load(open('deploy/argocd/apps/monitoring-loki.yaml')); print(d['spec']['source']['targetRevision'])"
```

Expected: `7.3.0`.

- [ ] **Step 3: Verify the chart accepts these values**

```bash
HELM=$(command -v helm)
"$HELM" repo add grafana https://grafana.github.io/helm-charts
"$HELM" repo update grafana
python3 -c "
import yaml
d = yaml.safe_load(open('deploy/argocd/apps/monitoring-loki.yaml'))
yaml.safe_dump(d['spec']['source']['helm']['valuesObject'], open('/tmp/loki-test-values.yaml','w'))
"
"$HELM" template loki grafana/loki --version 7.3.0 --namespace monitoring \
  --values /tmp/loki-test-values.yaml >/dev/null
```

Expected: no output, exit 0.

- [ ] **Step 4: Confirm no memcached, no PVC, one StatefulSet, and a bounded data volume**

```bash
HELM=$(command -v helm)
"$HELM" template loki grafana/loki --version 7.3.0 --namespace monitoring \
  --values /tmp/loki-test-values.yaml > /tmp/loki-rendered.yaml
echo "memcached: $(/usr/bin/grep -c 'memcached' /tmp/loki-rendered.yaml || true)"
echo "PVCs: $(/usr/bin/grep -c 'kind: PersistentVolumeClaim' /tmp/loki-rendered.yaml || true)"
echo "StatefulSets: $(/usr/bin/grep -c 'kind: StatefulSet' /tmp/loki-rendered.yaml || true)"
echo "/var/loki mounts: $(/usr/bin/grep -c 'mountPath: /var/loki' /tmp/loki-rendered.yaml || true)"
echo "sizeLimit: $(/usr/bin/grep -c 'sizeLimit: 3Gi' /tmp/loki-rendered.yaml || true)"
```

Expected: `memcached: 0`, `PVCs: 0`, `StatefulSets: 1`, `/var/loki mounts: 1`, `sizeLimit: 1`. A non-zero memcached count means a cache is still enabled and the pod will not schedule on this node. A zero `/var/loki mounts` count means the `extraVolumeMounts` block was dropped and Loki will write unbounded to the container layer.

- [ ] **Step 5: Record the push service name**

```bash
/usr/bin/grep -A3 "kind: Service" /tmp/loki-rendered.yaml | /usr/bin/grep "name:" | head -5
```

Note the service that exposes port 3100. Task 8 assumes `loki`. If the rendered name differs, use the rendered name in Task 8 Step 1.

- [ ] **Step 6: Commit**

```bash
git add deploy/argocd/apps/monitoring-loki.yaml
git commit -m "feat(observability): add single-binary Loki Argo CD Application"
```

---

### Task 8: Grafana Alloy Application

**Files:**
- Create: `deploy/argocd/apps/monitoring-alloy.yaml`

**Interfaces:**
- Consumes: the Loki push URL from Task 7 and the PriorityClass from Task 6. Parses the JSON fields `level` and `logger_name` emitted by Task 2.
- Produces: pod logs in Loki labelled `namespace`, `pod`, `container`, `app`, and `level`.

- [ ] **Step 1: Create the Application**

Create `deploy/argocd/apps/monitoring-alloy.yaml`. If Task 7 Step 6 showed a service name other than `loki`, substitute it in the `loki.write` URL.

```yaml
apiVersion: argoproj.io/v1alpha1
kind: Application
metadata:
  name: alloy
  namespace: argocd
  annotations:
    argocd.argoproj.io/sync-wave: "-15"
    argocd.argoproj.io/sync-options: Delete=confirm
spec:
  project: okrs-platform
  source:
    repoURL: https://grafana.github.io/helm-charts
    chart: alloy
    targetRevision: 1.11.1
    helm:
      releaseName: alloy
      valuesObject:
        controller:
          type: daemonset
          priorityClassName: okrs-observability
        alloy:
          resources:
            requests:
              memory: 128Mi
            limits:
              memory: 256Mi
          configMap:
            create: true
            content: |
              discovery.kubernetes "pods" {
                role = "pod"
              }

              discovery.relabel "pod_logs" {
                targets = discovery.kubernetes.pods.targets

                rule {
                  source_labels = ["__meta_kubernetes_namespace"]
                  target_label  = "namespace"
                }
                rule {
                  source_labels = ["__meta_kubernetes_pod_name"]
                  target_label  = "pod"
                }
                rule {
                  source_labels = ["__meta_kubernetes_pod_container_name"]
                  target_label  = "container"
                }
                rule {
                  source_labels = ["__meta_kubernetes_pod_label_app_kubernetes_io_name"]
                  target_label  = "app"
                }
              }

              loki.source.kubernetes "pods" {
                targets    = discovery.relabel.pod_logs.output
                forward_to = [loki.process.parse_json.receiver]
              }

              loki.process "parse_json" {
                stage.json {
                  expressions = {
                    level  = "level",
                    logger = "logger_name",
                  }
                }

                stage.labels {
                  values = {
                    level = "",
                  }
                }

                forward_to = [loki.write.default.receiver]
              }

              loki.write "default" {
                endpoint {
                  url = "http://loki.monitoring.svc.cluster.local:3100/loki/api/v1/push"
                }
              }
  destination:
    server: https://kubernetes.default.svc
    namespace: monitoring
  syncPolicy:
    automated:
      enabled: true
      prune: true
      selfHeal: true
      allowEmpty: false
    retry:
      limit: 5
    syncOptions:
      - CreateNamespace=true
      - PruneLast=true
```

`loki.source.kubernetes` reads logs through the Kubernetes API rather than a hostPath mount of `/var/log`, so no `alloy.mounts.varlog` is needed. The chart creates the required RBAC by default.

- [ ] **Step 2: Verify the manifest parses and the config survives YAML round-trip**

```bash
python3 -c "
import yaml
d = yaml.safe_load(open('deploy/argocd/apps/monitoring-alloy.yaml'))
c = d['spec']['source']['helm']['valuesObject']['alloy']['configMap']['content']
assert 'loki.write' in c, 'config content lost'
assert 'logger_name' in c, 'json parse stage lost'
print('config ok,', len(c.splitlines()), 'lines')
"
```

Expected: `config ok, 46 lines` (or close — the assertion matters, not the count).

- [ ] **Step 3: Verify the chart accepts these values**

```bash
HELM=$(command -v helm)
python3 -c "
import yaml
d = yaml.safe_load(open('deploy/argocd/apps/monitoring-alloy.yaml'))
yaml.safe_dump(d['spec']['source']['helm']['valuesObject'], open('/tmp/alloy-test-values.yaml','w'))
"
"$HELM" template alloy grafana/alloy --version 1.11.1 --namespace monitoring \
  --values /tmp/alloy-test-values.yaml >/dev/null
```

Expected: no output, exit 0.

- [ ] **Step 4: Confirm a DaemonSet and RBAC are produced**

```bash
HELM=$(command -v helm)
"$HELM" template alloy grafana/alloy --version 1.11.1 --namespace monitoring \
  --values /tmp/alloy-test-values.yaml > /tmp/alloy-rendered.yaml
echo "DaemonSets: $(/usr/bin/grep -c 'kind: DaemonSet' /tmp/alloy-rendered.yaml || true)"
echo "ClusterRoles: $(/usr/bin/grep -c 'kind: ClusterRole$' /tmp/alloy-rendered.yaml || true)"
echo "priorityClassName: $(/usr/bin/grep -c 'priorityClassName: okrs-observability' /tmp/alloy-rendered.yaml || true)"
/usr/bin/grep -A12 "kind: ClusterRole$" /tmp/alloy-rendered.yaml | /usr/bin/grep -c "pods/log" || echo "WARNING: pods/log not in ClusterRole"
```

Expected: `DaemonSets: 1`, `ClusterRoles: 1`, and a non-zero `pods/log` count. If `pods/log` is missing, `loki.source.kubernetes` cannot read logs — add an `rbac` rule for it via chart values before committing.

- [ ] **Step 5: Commit**

```bash
git add deploy/argocd/apps/monitoring-alloy.yaml
git commit -m "feat(observability): add Grafana Alloy log collector Application"
```

---

### Task 9: Schema validation for Argo CD manifests

**Files:**
- Modify: `scripts/validate-deploy.sh`
- Modify: `.github/workflows/deployment-validation.yml:38-45`

**Interfaces:**
- Consumes: the Application manifests from Tasks 6-8.
- Produces: a `[validate] Argo CD manifests` stage that fails on malformed Application YAML.

- [ ] **Step 1: Add the validation stage**

In `scripts/validate-deploy.sh`, after the Helm block (following the line added in Task 4 Step 5) and before the `if command -v az` block at line 28, insert:

```bash
printf '[validate] Argo CD manifests\n'
if command -v kubeconform >/dev/null 2>&1; then
  kubeconform \
    -strict \
    -ignore-missing-schemas \
    -schema-location default \
    -schema-location 'https://raw.githubusercontent.com/datreeio/CRDs-catalog/main/{{.Group}}/{{.ResourceKind}}_{{.ResourceAPIVersion}}.json' \
    deploy/argocd/root-application.yaml \
    deploy/argocd/apps/
else
  printf '[validate] kubeconform not installed; skipping Argo CD manifest schema check.\n'
fi
```

The skip-with-notice pattern matches the existing ShellCheck and Azure CLI blocks at lines 10-15 and 28-41.

- [ ] **Step 2: Install kubeconform locally and run it**

```bash
GOBIN=/tmp/obs-tools go install github.com/yannh/kubeconform/cmd/kubeconform@latest 2>/dev/null \
  || brew install kubeconform
kubeconform -v 2>&1 | head -1
```

If neither works, download the release binary for your platform from `https://github.com/yannh/kubeconform/releases` and put it on `PATH`.

- [ ] **Step 3: Run validation and confirm the new stage executes**

```bash
make validate-deploy 2>&1 | grep "Argo CD manifests" -A2
```

Expected: the `[validate] Argo CD manifests` line followed by no error, not the "not installed" notice.

- [ ] **Step 4: Prove the check actually catches a broken manifest**

```bash
cp deploy/argocd/apps/monitoring-loki.yaml /tmp/loki-backup.yaml
printf '\n  bogusTopLevelKey: true\n' >> deploy/argocd/apps/monitoring-loki.yaml
make validate-deploy 2>&1 | tail -5
```

Expected: FAILURE mentioning the invalid manifest. If it passes, the check is not strict enough — verify `-strict` is present.

- [ ] **Step 5: Restore the manifest and confirm green**

```bash
cp /tmp/loki-backup.yaml deploy/argocd/apps/monitoring-loki.yaml
make validate-deploy 2>&1 | tail -2
```

Expected: `[validate] Deployment artifacts passed available checks.`

- [ ] **Step 6: Install kubeconform in CI**

In `.github/workflows/deployment-validation.yml`, after the ShellCheck step (lines 39-42) and before the `Validate deployment artifacts` step, add:

```yaml
      - name: Install kubeconform
        run: |
          set -Eeuo pipefail
          version=v0.6.7
          curl -sSL --retry 3 \
            "https://github.com/yannh/kubeconform/releases/download/${version}/kubeconform-linux-amd64.tar.gz" \
            | sudo tar -xz -C /usr/local/bin kubeconform
          kubeconform -v
```

- [ ] **Step 7: Commit**

```bash
git add scripts/validate-deploy.sh .github/workflows/deployment-validation.yml
git commit -m "ci(observability): schema-validate Argo CD manifests"
```

---

### Task 10: PR-time application verification

**Files:**
- Modify: `.github/workflows/ci.yml:3-13`

**Interfaces:**
- Consumes: the tests from Tasks 1 and 2.
- Produces: a `verify` check on pull requests touching application code. It never logs into Azure, pushes an image, or writes to `develop`.

- [ ] **Step 1: Add the pull_request trigger and verification job**

In `.github/workflows/ci.yml`, replace the `on:` block at lines 3-13:

```yaml
on:
  push:
    branches:
      - develop
    paths:
      - 'okrs-api/**'
      - 'okrs-core/**'
      - 'core/**'
      - 'pom.xml'
      - 'Dockerfile'
  workflow_dispatch:
```

with:

```yaml
on:
  push:
    branches:
      - develop
    paths:
      - 'okrs-api/**'
      - 'okrs-core/**'
      - 'core/**'
      - 'pom.xml'
      - 'Dockerfile'
  pull_request:
    paths:
      - 'okrs-api/**'
      - 'okrs-core/**'
      - 'core/**'
      - 'pom.xml'
      - 'Dockerfile'
  workflow_dispatch:
```

- [ ] **Step 2: Add the verify-only job and gate the existing job**

In the same file, replace the `jobs:` line and the `build-push-pin:` header (lines 29-32):

```yaml
jobs:
  build-push-pin:
    runs-on: ubuntu-latest
    environment: development
```

with:

```yaml
jobs:
  verify:
    if: github.event_name == 'pull_request'
    runs-on: ubuntu-latest
    steps:
      - name: Check out source revision
        uses: actions/checkout@v4

      - name: Set up JDK 11
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '11'
          cache: maven

      - name: Verify application
        run: mvn --batch-mode clean verify

  build-push-pin:
    if: github.event_name != 'pull_request'
    runs-on: ubuntu-latest
    environment: development
```

The `if` on `build-push-pin` is essential. Without it a pull request would attempt an Azure login, an image push, and a commit to `develop`.

- [ ] **Step 3: Verify the workflow parses**

```bash
python3 -c "
import yaml
w = yaml.safe_load(open('.github/workflows/ci.yml'))
jobs = w['jobs']
assert set(jobs) == {'verify', 'build-push-pin'}, jobs.keys()
assert jobs['verify']['if'] == \"github.event_name == 'pull_request'\"
assert jobs['build-push-pin']['if'] == \"github.event_name != 'pull_request'\"
steps = [s.get('name','') for s in jobs['verify']['steps']]
assert 'Verify application' in steps, steps
assert not any('Azure' in s for s in steps), 'verify job must not touch Azure'
print('workflow ok')
"
```

Expected: `workflow ok`.

- [ ] **Step 4: Confirm the verify job has no write permissions in effect**

```bash
grep -A3 "^permissions:" .github/workflows/ci.yml
```

Expected: `contents: write` and `id-token: write` at workflow level. Note this in the commit body — the `verify` job inherits them but uses neither. Tightening per-job permissions is a reasonable follow-up, not required here.

- [ ] **Step 5: Commit**

```bash
git add .github/workflows/ci.yml
git commit -m "ci: run mvn verify on pull requests touching application code"
```

---

### Task 11: Access, documentation, and end-to-end verification

**Files:**
- Modify: `Makefile:14-17,109-113`
- Modify: `docs/deployment.md`

**Interfaces:**
- Consumes: everything from Tasks 1-10.
- Produces: `make grafana-port-forward`, `make monitoring-status`, and the documented runbook.

- [ ] **Step 1: Add the Makefile targets**

In `Makefile`, extend the `.PHONY` list at lines 14-17 by appending `grafana-port-forward monitoring-status` to the final line, then append at the end of the file:

```make
monitoring-status:
	kubectl -n monitoring get pods,services
	kubectl -n monitoring get servicemonitors.monitoring.coreos.com --all-namespaces

grafana-port-forward:
	@echo "Grafana on http://localhost:3000 - user 'admin'"
	@echo "Password: kubectl -n monitoring get secret kube-prometheus-stack-grafana -o jsonpath='{.data.admin-password}' | base64 -d"
	kubectl -n monitoring port-forward service/kube-prometheus-stack-grafana 3000:80
```

- [ ] **Step 2: Verify the targets are declared and parse**

```bash
make -n grafana-port-forward monitoring-status >/dev/null && echo "targets ok"
grep "^.PHONY" -A4 Makefile | grep -c "grafana-port-forward"
```

Expected: `targets ok`, then `1`.

- [ ] **Step 3: Run the full validation suite**

```bash
make validate-deploy
```

Expected: `[validate] Deployment artifacts passed available checks.`

- [ ] **Step 4: Run the complete test suite**

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
./mvnw --batch-mode clean verify
```

Expected: BUILD SUCCESS with 5 tests run in `okrs-api`.

- [ ] **Step 5: Document the runbook**

Append to `docs/deployment.md`:

```markdown
## Observability

The dev cluster runs an ephemeral in-cluster stack: Prometheus and Grafana from
`kube-prometheus-stack`, Loki in single-binary mode, and Grafana Alloy as the log collector. All
storage is `emptyDir`, so telemetry is lost when `down.sh` stops the cluster and Argo CD recreates
the stack on `resume.sh`. There is no alerting.

```bash
make monitoring-status
make grafana-port-forward
```

Grafana is on `http://localhost:3000` as `admin`. Read the password with:

```bash
kubectl -n monitoring get secret kube-prometheus-stack-grafana \
  -o jsonpath='{.data.admin-password}' | base64 -d
```

Prometheus retains six hours and Loki twenty-four. The application exposes metrics on its
management port 8081 at `/actuator/prometheus`, which is never routed through the Ingress.
Application logs are JSON under the `dev` and `qa` profiles and carry the labels `namespace`, `pod`,
`container`, `app`, and `level` in Loki.
```

- [ ] **Step 6: Commit**

```bash
git add Makefile docs/deployment.md
git commit -m "docs(observability): document Grafana access and the monitoring runbook"
```

- [ ] **Step 7: Post-deployment smoke check**

These require a running cluster and cannot be done offline. Run them after the branch is merged and Argo CD has synced.

```bash
# All three Applications healthy
make argocd-status

# Prometheus is scraping the app - expect state "up"
kubectl -n monitoring port-forward service/kube-prometheus-stack-prometheus 9090:9090 &
sleep 5
curl -s 'http://localhost:9090/api/v1/targets?state=active' \
  | python3 -c "import json,sys; [print(t['labels'].get('job'), t['health']) for t in json.load(sys.stdin)['data']['activeTargets']]"

# The actuator endpoint really serves Prometheus format on the management port
kubectl -n okrs-dev port-forward service/okrs-backend-okrs-backend 8081:8081 &
sleep 3
curl -s http://localhost:8081/actuator/prometheus | grep -c jvm_memory_used_bytes

# Application logs are valid JSON
kubectl -n okrs-dev logs --selector app.kubernetes.io/name=okrs-backend --tail=1 \
  | python3 -c "import json,sys; print(json.loads(sys.stdin.read())['level'])"
```

Expected: all Applications `Synced/Healthy`; an `okrs-backend` target with health `up`; a non-zero `jvm_memory_used_bytes` count; and a log level printed without a JSON decode error. This step covers the HTTP-level assertion that Deviation 1 removed from the unit tests.

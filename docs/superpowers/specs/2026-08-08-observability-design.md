# Observability for dev

Status: approved, ready for implementation planning.

## Goal

When the dev environment misbehaves, answer *why* without kubectl archaeology. Four signals:
searchable application logs that survive a pod restart, JVM and HTTP metrics, Kubernetes and pod
state over time, and PostgreSQL and Redis connection behaviour.

## Non-goals

Telemetry does not outlive the cluster. `down.sh` stops AKS and everything here is lost by design;
Argo CD recreates the stack on `resume.sh` and history starts fresh. There is no alerting, no
paging, no SLO tracking, and no distributed tracing. Nothing leaves Azure for a third-party SaaS.
QA is not provisioned by this work, though every chart value is written so QA can opt in later.

## Constraints that shaped the design

The cluster is a single node: `count: 1`, `Standard_D4s_v6`, System mode
(`infrastructure/platform/modules/aks.bicep:28-38`). Sixteen GB raw leaves roughly 12.8 GB
allocatable after AKS reservations, already shared with kube-system, Argo CD, External Secrets
Operator, and the application JVM. The subscription budget is 50 USD per month
(`infrastructure/platform/parameters/dev.bicepparam:21`), which is why the runbook stops the
cluster between sessions.

No monitoring exists today. `infrastructure/` contains no Log Analytics workspace, no Container
Insights add-on, and no Azure Monitor managed Prometheus. The application has
`spring-boot-starter-actuator` (`okrs-api/pom.xml:45`) but exposes only `health`
(`okrs-api/src/main/resources/application.yml:89-93`), has no metrics registry, and has no
`logback-spring.xml` anywhere in the repository.

Spring Boot is 2.7.4 on Java 11. Tracing on that line means Spring Cloud Sleuth, not Micrometer
Tracing; this is one reason tracing is out of scope.

## Approach

`kube-prometheus-stack` with Alertmanager disabled, plus Loki in single-binary mode and Grafana
Alloy as the log collector. All storage is `emptyDir`. Chosen over a hand-rolled Prometheus for its
prebuilt Kubernetes and JVM dashboards, which answer the "is the node full, what restarted" questions
on day one, and for `ServiceMonitor` CRDs, which make scrape configuration declarative and therefore
GitOps-native.

Promtail and the `loki-stack` umbrella chart are both deprecated upstream in favour of Alloy and the
standalone `grafana/loki` chart. The design uses the current components.

Chart versions are pinned to exact releases — no ranges, no `*` — mirroring
`external-secrets.yaml:14`. The specific versions are chosen and recorded during implementation,
verified against the chart repositories at that moment rather than guessed here.

## Architecture

Three new Argo CD Applications in `deploy/argocd/apps/`, each shaped like
`external-secrets.yaml`: upstream chart by `repoURL`/`chart`/`targetRevision`, automated sync with
prune and self-heal, `Delete=confirm`, `CreateNamespace=true`, `PruneLast=true`. They deploy into a
new `monitoring` namespace, kept separate from `okrs-dev` so the stack outlives environment teardown
and so a future QA environment can scrape into the same Prometheus.

The three are `kube-prometheus-stack`, `loki`, and `alloy`. Grafana, Prometheus, the Prometheus
Operator, kube-state-metrics, and node-exporter are all sub-components of `kube-prometheus-stack` and
are configured through its values rather than deployed as Applications of their own. Where the
resource table below names them individually, it refers to values under that one chart.

All three sit at sync wave `-15`, between External Secrets (`-20`) and the environment (`-10`), and
therefore before the application at wave `0`. The ordering is load-bearing: the `ServiceMonitor` CRD
ships with `kube-prometheus-stack`, and the application chart declares a resource of that kind. CRDs
must exist before the custom resource is applied.

### AppProject changes, required before first sync

`deploy/argocd/apps/projects.yaml` is an explicit allowlist and will reject this work as written.
Four edits:

1. `okrs-platform.sourceRepos` (lines 26-28) gains
   `https://prometheus-community.github.io/helm-charts` and `https://grafana.github.io/helm-charts`.
   Without them Argo refuses the chart sources outright.
2. `okrs-platform.destinations` (lines 29-33) gains the `monitoring` namespace.
3. `okrs-platform.clusterResourceWhitelist` (lines 34-46) gains
   `group: scheduling.k8s.io, kind: PriorityClass`, for the eviction ordering described under
   Operations.
4. `okrs-dev.namespaceResourceWhitelist` (lines 66-78) gains
   `group: monitoring.coreos.com, kind: ServiceMonitor`.

The fourth failure is silent and deserves emphasis: the application deploys successfully and only the
`ServiceMonitor` is dropped, so the symptom is an app that runs fine but is never scraped.

## Application instrumentation

### Management port split

`values/dev.yaml:6` sets `ingress.enabled: false`, so nothing is publicly reachable today. That
changes when QA enables ingress, because `templates/ingress.yaml:19-27` maps supplied paths directly
to `service.port`; a catch-all `/` would publish `/actuator/prometheus` to the internet.

Actuator therefore moves to its own port. `management.server.port: 8081`, a second `containerPort`
named `management`, that port added to the Service, the three probes retargeted at it, and the
`ServiceMonitor` scraping it. Ingress continues to reference `service.port` only, which makes the
management surface structurally unreachable from outside instead of merely unrouted.

### Changes

`okrs-api/pom.xml` gains `io.micrometer:micrometer-registry-prometheus`, unversioned because
`spring-boot-dependencies` 2.7.4 manages it, and `net.logstash.logback:logstash-logback-encoder` at
an explicit **7.x** version — the 8.x line requires logback 1.3+, while Boot 2.7 resolves logback
1.2.11.

`okrs-api/src/main/resources/application.yml:89-93` widens exposure from `health` to
`health,prometheus`. Nothing further: not `metrics`, not `env`, not `heapdump`.
`management.endpoint.health.probes.enabled: true` stays exactly as it is, because the Kubernetes
probes depend on it. `management.metrics.tags.application` and `management.metrics.tags.environment`
are added so dev and later QA series stay distinguishable in one Prometheus; these are properties,
requiring no Java configuration class on Boot 2.7.

A new `okrs-api/src/main/resources/logback-spring.xml` defines two console appenders selected by
`<springProfile>`: a human-readable pattern for `local`, and `LogstashEncoder` JSON for `dev` and
`qa`. The filename matters — `logback.xml` would cause the profile blocks to be ignored. JSON output
is what lets Loki filter on `level` and `logger` rather than pattern-matching raw text.

A new `deploy/charts/okrs-backend/templates/servicemonitor.yaml` is gated behind
`.Values.metrics.serviceMonitor.enabled`, defaulting to `false` so `local.yaml` and `qa.yaml` are
unaffected, and enabled in `dev.yaml`.

HikariCP pool saturation, Lettuce command metrics, JVM heap, GC and thread counts, and per-endpoint
HTTP latency and error rate all arrive as standard Micrometer bindings once the registry is present.
They need no additional configuration.

## Operations

Grafana is reached through `make grafana-port-forward` on `localhost:3000`, mirroring the existing
`make argocd-port-forward` on `8081`. There is no ingress and no TLS. The admin password comes from
the chart-generated secret and is read with `kubectl`; sourcing it from Key Vault through External
Secrets was considered and rejected as unnecessary ceremony for a port-forward-only dev tool.

### Resource envelope

Every component carries explicit requests and limits, because a single node has no room for
guesswork.

| Component | Request | Limit |
| --- | --- | --- |
| Prometheus | 512Mi / 200m | 1.5Gi |
| Loki | 256Mi | 512Mi |
| Alloy (DaemonSet) | 128Mi | 256Mi |
| Grafana | 128Mi | 256Mi |
| Prometheus Operator | 128Mi | 256Mi |
| kube-state-metrics | 64Mi | 128Mi |
| node-exporter | 64Mi | 128Mi |

That is roughly 1.3 Gi requested against a 3 Gi ceiling. Alertmanager and Grafana persistence are
disabled. Prometheus retains six hours, Loki twenty-four, and both `emptyDir` volumes set
`sizeLimit`: an unbounded `emptyDir` that fills the node's disk triggers disk-pressure eviction of
every pod on it, including the application.

A cluster-scoped `PriorityClass` named `okrs-observability` is created with a negative value and
`globalDefault: false`, and every component in the three Applications sets
`priorityClassName: okrs-observability`. Workloads without a priority class default to zero, so the
negative value guarantees the kubelet evicts monitoring pods before `okrs-backend` under memory
pressure. Creating it depends on AppProject edit 3 above.

The `kubeControllerManager`, `kubeScheduler`, `kubeEtcd`, and `kubeProxy` scrape jobs are disabled.
AKS manages the control plane and does not expose those endpoints, so leaving them enabled produces
permanently failing targets, which teaches the operator to ignore failing targets.

### Failure modes

Data loss across `down.sh` is expected and requires no mitigation. The two silent failures are the
AppProject rejection of the `ServiceMonitor`, addressed by the `projects.yaml` edits above, and
scrapes failing because the management port never reached the Service, addressed by the port split.
Both are covered by validation below.

## Validation

`scripts/validate-deploy.sh` templates the application chart against all three values files at lines
23-25, but the new `servicemonitor.yaml` is gated off by default and would render to nothing. A
fourth template run with `--set metrics.serviceMonitor.enabled=true` exercises it.

`deploy/argocd/**` is currently not validated at all — the Application manifests that drive the
entire cluster receive no lint. The script gains a schema pass, using `kubeconform` with the Argo CD
CRD schemas, over `deploy/argocd/apps/*.yaml` and `deploy/argocd/root-application.yaml`. As with
ShellCheck and Bicep at lines 10-15 and 28-41, the check skips with a printed notice when the tool is
absent locally, and runs unconditionally in CI where the workflow installs it.

`mvn verify` gains one integration test asserting that `/actuator/prometheus` returns 200 and its
body contains `jvm_memory_used_bytes`. This proves the registry is wired into the context rather than
merely present on the classpath.

### CI gate

`.github/workflows/ci.yml` runs `mvn verify` only on push to `develop` and on `workflow_dispatch`,
and `deployment-validation.yml` matches only deploy and infrastructure paths. A pull request touching
`okrs-api/**` therefore runs no checks whatsoever, which is precisely the risk this work introduces
through `pom.xml`, `application.yml`, and `logback-spring.xml`.

A `pull_request` trigger running `mvn verify` is added for the application paths already listed in
`ci.yml:7-12` — `okrs-api/**`, `okrs-core/**`, `core/**`, `pom.xml`, `Dockerfile`. It performs
verification only: no Azure login, no image push, no GitOps commit. Those steps stay on the existing
push-to-`develop` path, so a pull request can never mutate dev's pinned image.

## Out of scope, recorded so it is not lost

QA enablement remains blocked on `infrastructure/environment/parameters/qa.bicepparam`,
`deploy/charts/eso-environment/values/qa.yaml`, the two QA Argo child Applications, and the
`deploy/environments/qa.enabled` marker. Setting `acrAdminUserEnabled = false` after the first
successful OIDC push is still outstanding. `main` trails `develop` by thirty commits and will need a
release pull request. None of these are touched here.

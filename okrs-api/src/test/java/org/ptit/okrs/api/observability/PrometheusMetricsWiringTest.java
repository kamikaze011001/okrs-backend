package org.ptit.okrs.api.observability;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.io.InputStream;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.JvmMetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.prometheus.PrometheusMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

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

  @Test
  void applicationYamlDeclaresExpectedManagementConfig() throws Exception {
    Yaml yaml = new Yaml();
    try (InputStream input = new ClassPathResource("application.yml").getInputStream()) {
      Map<String, Object> root = yaml.load(input);

      Map<?, ?> management = (Map<?, ?>) root.get("management");

      Map<?, ?> metrics = (Map<?, ?>) management.get("metrics");
      Map<?, ?> tags = (Map<?, ?>) metrics.get("tags");
      assertThat(tags.get("application")).isEqualTo("okrs-backend");
      assertThat(tags.get("environment")).isEqualTo("${SPRING_PROFILES_ACTIVE:local}");

      Map<?, ?> endpoints = (Map<?, ?>) management.get("endpoints");
      Map<?, ?> web = (Map<?, ?>) endpoints.get("web");
      Map<?, ?> exposure = (Map<?, ?>) web.get("exposure");
      assertThat(exposure.get("include")).isEqualTo("health,prometheus");
    }
  }
}

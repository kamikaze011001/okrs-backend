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

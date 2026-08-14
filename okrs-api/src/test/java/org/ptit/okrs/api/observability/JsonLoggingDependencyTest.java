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

    // Note: logback-classic 1.2.11 (resolved by Spring Boot 2.7.4) has no
    // LoggingEvent#setLoggerContext(LoggerContext) method (added in later
    // logback lines). Bind the event to the context via the
    // fqcn/Logger/Level/message constructor instead; assertions are
    // unchanged from the brief.
    ch.qos.logback.classic.Logger logger =
        context.getLogger("org.ptit.okrs.api.SampleLogger");
    LoggingEvent event =
        new LoggingEvent(LoggingEvent.class.getName(), logger, Level.WARN, "sample message", null, null);
    event.setThreadName("main");

    String encoded = new String(encoder.encode(event));

    assertThat(encoded).startsWith("{");
    assertThat(encoded).contains("\"level\":\"WARN\"");
    assertThat(encoded).contains("\"logger_name\":\"org.ptit.okrs.api.SampleLogger\"");
    assertThat(encoded).contains("\"message\":\"sample message\"");
  }
}

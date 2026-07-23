package org.ptit.okrs.core_swagger;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class CoreSwaggerConfiguration {

  private final Environment environment;

  public CoreSwaggerConfiguration(Environment environment) {
    this.environment = environment;
  }

  @Bean
  public GroupedOpenApi api() {
    // We grab the app name just like before, providing a fallback just in case
    String appName = environment.getProperty("spring.application.name", "app-");

    return GroupedOpenApi.builder()
            .group(appName + "api")
            .pathsToMatch("/**") // This is the Springdoc equivalent to RequestHandlerSelectors.any()
            .build();
  }
}
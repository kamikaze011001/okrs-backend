package org.ptit.okrs.core_api_exception.configuration;

import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core_api_exception.custom_handle.CustomHandleException;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import static org.ptit.okrs.core_api_exception.constant.ExceptionApiConstant.UTF_8_ENCODING;

@Configuration
@ComponentScan(basePackages = {"org.ptit.okrs.core_api_exception"})
@Slf4j
public class CoreApiExceptionConfiguration {
  @Bean
  public CustomHandleException apiExceptionHandler(MessageSource messageSource) {
    return new CustomHandleException(messageSource);
  }

  @Bean
  public MessageSource messageSource(MessageResourcesProperties resource) {
    log.info("(messageSource)resource: {}", resource.getResources());
    var messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setBasenames(resource.getResources().toArray(String[]::new));
    messageSource.setDefaultEncoding(UTF_8_ENCODING);
    return messageSource;
  }
}

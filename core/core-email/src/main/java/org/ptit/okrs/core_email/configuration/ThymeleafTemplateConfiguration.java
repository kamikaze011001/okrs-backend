package org.ptit.okrs.core_email.configuration;

import org.ptit.okrs.core_email.constant.EmailConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;

@Configuration
public class ThymeleafTemplateConfiguration {

  @Bean
  public SpringTemplateEngine springTemplateEngine() {
    var templateEngine = new SpringTemplateEngine();
    templateEngine.addTemplateResolver(emailTemplateSolver());
    return templateEngine;
  }

  @Bean
  public ClassLoaderTemplateResolver emailTemplateSolver() {
    var emailTemplateSolver = new ClassLoaderTemplateResolver();
    emailTemplateSolver.setPrefix(EmailConstant.EMAIL_TEMPLATE_PREFIX);
    emailTemplateSolver.setSuffix(EmailConstant.EMAIL_TEMPLATE_SUFFIX);
    emailTemplateSolver.setTemplateMode(TemplateMode.HTML);
    emailTemplateSolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
    emailTemplateSolver.setCacheable(false);
    return emailTemplateSolver;
  }
}

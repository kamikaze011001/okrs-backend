package org.ptit.okrs.core_email.configuration;

import org.ptit.okrs.core_email.service.EmailService;
import org.ptit.okrs.core_email.service.impl.EmailServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring5.SpringTemplateEngine;

@Configuration
public class CoreEmailConfiguration {

  @Value("${application.email.from}")
  private String emailFrom;

  @Bean
  public EmailService emailService(JavaMailSender emailSender, SpringTemplateEngine templateEngine) {
    return new EmailServiceImpl(emailSender, templateEngine, emailFrom);
  }

}

package org.ptit.okrs.core_email.service.impl;

import java.util.Objects;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core_email.constant.EmailConstant;
import org.ptit.okrs.core_email.service.EmailService;
import org.ptit.okrs.core_exception.InternalServerError;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.Map;

@Slf4j
public class EmailServiceImpl implements EmailService {

  private final JavaMailSender emailSender;
  private final SpringTemplateEngine templateEngine;
  private final String emailFrom;

  public EmailServiceImpl(JavaMailSender emailSender, SpringTemplateEngine templateEngine, String emailFrom) {
    this.emailSender = emailSender;
    this.templateEngine = templateEngine;
    this.emailFrom = emailFrom;
  }

  @Async
  @Override
  public void send(String subject, String to, String content) {
    log.info("(send)subject: {}, to: {}, content: {}", subject, to, content);
    try {
      var message = new SimpleMailMessage();
      message.setFrom(emailFrom);
      message.setTo(to);
      message.setSubject(subject);
      message.setText(content);
      emailSender.send(message);
    } catch (Exception ex) {
      log.error("(send)to: {}, subject: {}, ex: {}", to, subject, ex.getMessage());
      throw new InternalServerError("Send mail failed to email: {}" + to);
    }
  }

  @Async
  @Override
  public void send(String subject, String to, String template, Map<String, Object> properties) {
    log.info("(send)subject: {}, to: {}, template: {}, properties: {}", subject, to, template, properties);
    try {
      var message = emailSender.createMimeMessage();
      message.setRecipients(Message.RecipientType.TO, to);
      message.setSubject(subject);
      message.setContent(getContent(template, properties), EmailConstant.CONTENT_TYPE_TEXT_HTML);
      emailSender.send(message);
    } catch (Exception ex) {
      log.info("(send)subject: {}, to: {}, ex: {} ", subject, to, ex.getMessage());
      throw new InternalServerError("Send mail failed to email: {}" + to);
    }
  }

  @Override
  public void send(String subject, String to, String content, String fileToAttach) {
    log.info("(send)subject: {}, to: {}, content: {}, fileToAttach: {}", subject, to, content, fileToAttach);
    try{
      var message = emailSender.createMimeMessage();
      var helper = new MimeMessageHelper(message, true);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(content);

      FileSystemResource fileSystemResource = new FileSystemResource(fileToAttach);
      helper.addAttachment(Objects.requireNonNull(fileSystemResource.getFilename()), fileSystemResource);
      emailSender.send(message);
    } catch (Exception ex) {
      log.info("(send)subject: {}, to: {}, ex: {} ", subject, to, ex.getMessage());
      throw new InternalServerError("Send mail failed to email: {}" + to);
    }
  }

  private String getContent(String template, Map<String, Object> properties) {
     var context = new Context();
    context.setVariables(properties);
    return templateEngine.process(template, context);
  }
}

package org.ptit.okrs.test.controller;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core_email.configuration.EnableCoreEmail;
import org.ptit.okrs.core_email.constant.EmailConstant;
import org.ptit.okrs.core_email.service.EmailService;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@SpringBootApplication
@EnableCoreEmail
@RequestMapping("/api/v1/email")
@Slf4j
public class EmailControllerTest {
  private final EmailService service;

  @GetMapping("/text")
  @ResponseStatus(HttpStatus.OK)
  public String sendMailText(String subject, String to, String content) {
    log.info("(sendMailText)subject: {}, to: {}, content: {}", subject, to, content);
    service.send(subject, to, content);
    return "send success";
  }

  @GetMapping("/html")
  @ResponseStatus(HttpStatus.OK)
  public String sendMailHtml(String subject, String to, String template, Map<String ,Object> properties) {
    log.info("(sendMailText)subject: {}, to: {}, template: {}, properties: {}", subject, to, template, properties);
    var params = new HashMap<String, Object>();
    params.put("time_life", 3);
    params.put("otp", 2222);
    service.send(subject, to, "OTP-template", params);
    return "send success";
  }

  @GetMapping("/attach")
  @ResponseStatus(HttpStatus.OK)
  public String sendMailAttach(String subject, String to, String content) {
    String fileToAttach = "C:\\Users\\X1\\OneDrive\\Desktop\\tutotial.txt";
    log.info("(sendMailAttach)subject: {}, to: {}, content: {}, fileToAttach: {}", subject, to, content, fileToAttach);
    service.send(subject, to, content, fileToAttach);
    return "send success";
  }
}

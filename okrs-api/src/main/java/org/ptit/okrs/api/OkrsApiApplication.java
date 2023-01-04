package org.ptit.okrs.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class OkrsApiApplication implements CommandLineRunner {

  public static void main(String[] args) {
    SpringApplication.run(OkrsApiApplication.class, args);
  }

  @Override
  public void run(String... args) throws Exception {
    log.debug("------------------START------------------------------");
    System.out.println("Author: Leonard");
    System.out.println("Date: 19/10/2022");
    System.out.println("Project name: Okrs management");
    log.debug("------------------START------------------------------");
  }
}

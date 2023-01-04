package org.ptit.okrs.core_upload.configuration;

import org.ptit.okrs.core_swagger.EnableCoreSwagger;
import org.ptit.okrs.core_upload.service.CoreUploadService;
import org.ptit.okrs.core_upload.service.impl.CoreUploadServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "org.ptit.okrs.core_upload.controller")
@EnableCoreSwagger
public class CoreUploadConfiguration {

  @Value("${application.upload.path.file.storage}")
  String pathStorageFile;

  @Bean
  public CoreUploadService coreUploadService() {
    return new CoreUploadServiceImpl(pathStorageFile);
  }

}

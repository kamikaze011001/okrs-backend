package org.ptit.okrs.api.configuration;

import org.ptit.okrs.core.configuration.EnableOkrsCore;
import org.ptit.okrs.core_api_exception.configuration.EnableCoreApiException;
import org.ptit.okrs.core_authentication.configuration.EnableCoreAuthentication;
import org.ptit.okrs.core_swagger.EnableCoreSwagger;
import org.ptit.okrs.core_upload.configuration.EnableCoreUpload;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableCoreApiException
@EnableCoreSwagger
@EnableOkrsCore
@EnableCoreUpload
@EnableScheduling
@EnableCoreAuthentication
public class OkrsApiConfiguration {
}

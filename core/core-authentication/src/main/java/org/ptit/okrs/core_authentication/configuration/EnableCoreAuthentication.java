package org.ptit.okrs.core_authentication.configuration;

import org.ptit.okrs.core_authentication.controller.AuthUserController;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Import({
    CoreAuthenticationConfiguration.class,
    OkrsWebSecurityConfiguration.class,
    JpaAuthTransactionConfiguration.class,
    AuthUserController.class
})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableCoreAuthentication {
}

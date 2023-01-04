package org.ptit.okrs.core_authentication.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

@Constraint(validatedBy = ValidationUsername.UsernameValidation.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.FIELD})
public @interface ValidationUsername {

  String message() default "Username doesn't contain special characters";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  class UsernameValidation implements ConstraintValidator<ValidationUsername, String> {

    @Override
    public void initialize(ValidationUsername constraintAnnotation) {
      ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext constraintValidatorContext) {
      String usernameRegex = "[A-Za-z0-9]+";
      return username.matches(usernameRegex);
    }
  }
}

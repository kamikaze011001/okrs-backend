package org.ptit.okrs.core.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;

@Constraint(validatedBy = ValidatePhoneNumber.PhoneNumberValidator.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.FIELD})
public @interface ValidatePhoneNumber {

  String message() default "Invalid Phone Number";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  class PhoneNumberValidator implements ConstraintValidator<ValidatePhoneNumber, String> {

    @Override
    public void initialize(ValidatePhoneNumber constraintAnnotation) {}

    @Override
    public boolean isValid(
        String phoneNumber, ConstraintValidatorContext constraintValidatorContext) {
      var regexPhoneNumber = "(0)(3|5|7|8|9)+([0-9]{8})\\b";
      return phoneNumber.matches(regexPhoneNumber);
    }
  }
}

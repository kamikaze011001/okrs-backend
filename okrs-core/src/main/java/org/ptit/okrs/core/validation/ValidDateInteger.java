package org.ptit.okrs.core.validation;

import org.ptit.okrs.core_util.ValidationUtils;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.*;

@Constraint(validatedBy = ValidDateInteger.DateIntegerValidator.class)
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.FIELD})
public @interface ValidDateInteger {

  String message() default "Invalid date";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};

  class DateIntegerValidator implements ConstraintValidator<ValidDateInteger, Integer> {

    @Override
    public void initialize(ValidDateInteger constraintAnnotation) {
    }

    @Override
    public boolean isValid(Integer date, ConstraintValidatorContext constraintValidatorContext) {
      return ValidationUtils.validateDate(date);
    }
  }
}

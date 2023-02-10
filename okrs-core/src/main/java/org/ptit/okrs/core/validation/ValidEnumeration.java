package org.ptit.okrs.core.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import javax.validation.*;
import org.ptit.okrs.core.validation.ValidEnumeration.ValidEnumerationImpl;

@Documented
@Constraint(validatedBy = ValidEnumerationImpl.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ValidEnumeration {

  Class<? extends Enum<?>> enumName();

  String message() default "Enum value is not valid";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  public class ValidEnumerationImpl implements ConstraintValidator<ValidEnumeration, String> {

    List<String> enumValues = null;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
      return value != null && enumValues.contains(value.toUpperCase());
    }

    @Override
    public void initialize(ValidEnumeration constraintAnnotation) {
      enumValues = new ArrayList<String>();
      Class<? extends Enum<?>> enumClass = constraintAnnotation.enumName();
      Enum[] enumValueArr = enumClass.getEnumConstants();
      for (Enum enumVal : enumValueArr) {
        enumValues.add(enumVal.toString().toUpperCase());
      }
    }
  }
}

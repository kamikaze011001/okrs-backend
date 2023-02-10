package org.ptit.okrs.api.model.request;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ptit.okrs.core.constant.Gender;
import org.ptit.okrs.core.validation.ValidDateInteger;
import org.ptit.okrs.core.validation.ValidEnumeration;
import org.ptit.okrs.core.validation.ValidatePhoneNumber;

@Data
@NoArgsConstructor
public class UserUpdateRequest {
  @NotBlank
  private String name;
  @ValidatePhoneNumber
  private String phone;
  @ValidDateInteger
  private Integer dateOfBirth;

  @ValidEnumeration(enumName = Gender.class, message = "Invalid Enum Gender Value")
  private String gender;

  private String address;
}

package org.ptit.okrs.core_authentication.dto.request;

import javax.validation.constraints.Email;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import org.ptit.okrs.core_authentication.validation.ValidateEmail;

@Data
@NoArgsConstructor
public class AuthUserResetPasswordRequest {

  //Email
  @NotBlank
  @ValidateEmail
  private String email;
}

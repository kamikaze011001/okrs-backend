package org.ptit.okrs.core_authentication.dto.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class AuthUserActiveAccountRequest {

  @Email
  @NotBlank
  private String email;

  @NotBlank
  private String otp;
}

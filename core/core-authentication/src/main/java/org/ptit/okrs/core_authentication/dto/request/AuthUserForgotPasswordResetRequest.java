package org.ptit.okrs.core_authentication.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import org.ptit.okrs.core_authentication.validation.ValidateEmail;

@Data
@NoArgsConstructor
public class AuthUserForgotPasswordResetRequest {
  @NotBlank
  private String resetPasswordKey;
  @NotBlank
  @ValidateEmail
  private String email;
  @NotBlank
  private String newPassword;
  @NotBlank
  private String newPasswordConfirm;
}

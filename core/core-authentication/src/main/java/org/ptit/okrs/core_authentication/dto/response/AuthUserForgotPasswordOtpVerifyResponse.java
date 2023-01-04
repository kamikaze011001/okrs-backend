package org.ptit.okrs.core_authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class AuthUserForgotPasswordOtpVerifyResponse {
  private String resetPasswordKey;
}

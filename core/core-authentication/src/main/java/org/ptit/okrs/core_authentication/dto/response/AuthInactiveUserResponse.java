package org.ptit.okrs.core_authentication.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthInactiveUserResponse extends AuthUserLoginResponse{
  private String message;

  public static AuthInactiveUserResponse from(String message) {
    AuthInactiveUserResponse response = new AuthInactiveUserResponse();
    response.setMessage(message);
    return response;
  }
}

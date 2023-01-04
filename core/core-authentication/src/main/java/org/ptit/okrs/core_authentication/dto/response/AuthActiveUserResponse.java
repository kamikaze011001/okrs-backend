package org.ptit.okrs.core_authentication.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthActiveUserResponse extends AuthUserLoginResponse {
  private String accessToken;
  private String refreshToken;
  private final String tokenType = "Bearer";
  private long accessTokenLifeTime;
  private long refreshTokenLifeTime;

  public static AuthActiveUserResponse from(
      String accessToken,
      String refreshToken,
      long accessTokenLifeTime,
      long refreshTokenLifeTime) {
    AuthActiveUserResponse response = new AuthActiveUserResponse();
    response.setAccessToken(accessToken);
    response.setRefreshToken(refreshToken);
    response.setAccessTokenLifeTime(accessTokenLifeTime);
    response.setRefreshTokenLifeTime(refreshTokenLifeTime);
    return response;
  }
}

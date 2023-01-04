package org.ptit.okrs.core_authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor(staticName = "of")
@Data
@NoArgsConstructor
public class AuthUserRegisterResponse {
  private String id;
  private String email;
  private String accountId;
  private String username;
  private Boolean isActivated;
}

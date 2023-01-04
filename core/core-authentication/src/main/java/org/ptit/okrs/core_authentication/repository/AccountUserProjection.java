package org.ptit.okrs.core_authentication.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountUserProjection {

  private String accountId;
  private String username;
  private String password;
  private String userId;
  private String email;
  private Boolean isActivated;
  private Boolean isLockPermanent;
}

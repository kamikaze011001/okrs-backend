package org.ptit.okrs.core_authentication.entity;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@AllArgsConstructor(staticName = "of")
@Entity
@Data
@NoArgsConstructor
@Table(name = "account")
@EntityListeners(AuditingEntityListener.class)
public class AuthAccount extends BaseEntity {
  private String username;
  private String password;
  private Boolean isActivated = false;
  private Boolean isLockPermanent = false;
  private String userId;

  public static AuthAccount of(String userId, String username, String password) {
    return AuthAccount.of(username, password, false, false, userId);
  }
}

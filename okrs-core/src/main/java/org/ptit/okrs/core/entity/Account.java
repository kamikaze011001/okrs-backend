package org.ptit.okrs.core.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ptit.okrs.core.entity.base.BaseEntityWithUpdater;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Data
@Table(name = "account")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Account extends BaseEntityWithUpdater {
  @Column(nullable = false, unique = true)
  private String username;
  @Column(nullable = false)
  private String password;
  private Boolean isActivated = false;
  private Boolean isLockPermanent = false;
  private String userId;
}

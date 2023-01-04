package org.ptit.okrs.core.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ptit.okrs.core.constant.Gender;
import org.ptit.okrs.core.entity.base.BaseEntityWithUpdater;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Data
@Table(name = "user_okrs")
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseEntityWithUpdater {
  @Column(nullable = false)
  private String name;
  private String phone;
  @Column(nullable = false, unique = true)
  private String email;
  private Integer dateOfBirth;
  private Gender gender;
  private String address;
  private String avatar;

  public static User of(String name, String email) {
    var user = new User();
    user.setName(name);
    user.setEmail(email);
    return user;
  }
}

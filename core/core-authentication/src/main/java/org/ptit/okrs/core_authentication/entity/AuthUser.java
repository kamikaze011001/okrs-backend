package org.ptit.okrs.core_authentication.entity;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity(name = "AuthUser")
@NoArgsConstructor
@Table(name = "user_okrs")
@EntityListeners(AuditingEntityListener.class)
public class AuthUser extends BaseEntity {
  private String name;
  private String phone;
  private String email;
  private Integer dateOfBirth;
  private String gender;
  private String address;
  private String avatar;

  public static AuthUser from(String name, String email) {
    var user = new AuthUser();
    user.setName(name);
    user.setEmail(email);
    return user;
  }
}

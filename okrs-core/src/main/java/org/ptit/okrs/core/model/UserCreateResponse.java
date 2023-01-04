package org.ptit.okrs.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ptit.okrs.core.entity.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateResponse {
  private String id;
  private String name;
  private String email;

  public static UserCreateResponse from(User user) {
    var userCreateResponse = new UserCreateResponse();
    userCreateResponse.id = user.getId();
    userCreateResponse.name = user.getName();
    userCreateResponse.email = user.getEmail();
    return userCreateResponse;
  }
}

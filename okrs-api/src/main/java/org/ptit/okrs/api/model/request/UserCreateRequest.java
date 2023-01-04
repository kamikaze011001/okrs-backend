package org.ptit.okrs.api.model.request;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ptit.okrs.core_authentication.validation.ValidateEmail;

@Data
@NoArgsConstructor
public class UserCreateRequest {
  @NotBlank
  private String name;
  @NotBlank
  @ValidateEmail
  private String email;
}

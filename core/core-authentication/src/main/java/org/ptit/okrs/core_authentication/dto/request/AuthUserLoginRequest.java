package org.ptit.okrs.core_authentication.dto.request;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ptit.okrs.core_authentication.validation.ValidationUsername;

@Data
@NoArgsConstructor
public class AuthUserLoginRequest {

  @NotBlank @ValidationUsername private String username;

  @NotBlank private String password;
}

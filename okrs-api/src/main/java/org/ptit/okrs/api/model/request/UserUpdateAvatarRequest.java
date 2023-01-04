package org.ptit.okrs.api.model.request;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserUpdateAvatarRequest {
  @NotBlank
  private String pathAvatar;
}

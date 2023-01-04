package org.ptit.okrs.core_authentication.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.ptit.okrs.core_authentication.validation.ValidateEmail;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class AuthUserSentOtpToMail {

    @NotBlank
    @ValidateEmail
    private String email;
}

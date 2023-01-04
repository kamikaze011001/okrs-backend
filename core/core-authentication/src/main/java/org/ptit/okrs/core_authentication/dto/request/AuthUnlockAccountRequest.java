package org.ptit.okrs.core_authentication.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.ptit.okrs.core_authentication.validation.ValidateEmail;
import org.ptit.okrs.core_authentication.validation.ValidationUsername;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class AuthUnlockAccountRequest {

    @NotBlank
    @ValidateEmail
    private String email;

    @NotBlank
    private String otp;
}

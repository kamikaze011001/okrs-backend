package org.ptit.okrs.core_authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.ptit.okrs.core_util.DateUtils;

@Data
@AllArgsConstructor(staticName = "of")
public class AuthResponse {
    private int status;
    private String timestamp;
    private Object data;

    public static AuthResponse of(int status, Object data) {
        return AuthResponse.of(status, DateUtils.getCurrentDateTimeStr(), data);
    }

    public static AuthResponse of(int status) {
        return AuthResponse.of(status, DateUtils.getCurrentDateTimeStr(), null);
    }
}

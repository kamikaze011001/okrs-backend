package org.ptit.okrs.api.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
public class NotificationCreateRequest {

    @NotBlank
    private String content;

}

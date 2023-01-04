package org.ptit.okrs.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.ptit.okrs.core.entity.Notification;

@Data
@NoArgsConstructor
public class NotificationResponse {

    private String id;

    private String content;


    public static NotificationResponse from(Notification notification) {
        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setContent(notification.getContent());
        return response;
    }
}

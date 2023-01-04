package org.ptit.okrs.core.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.ptit.okrs.core.entity.base.BaseEntityWithUpdater;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
@Table(name = "notification")
@EntityListeners(AuditingEntityListener.class)
public class Notification extends BaseEntityWithUpdater {

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String userId;

    public static Notification of(String content, String userId) {
        Notification notification = new Notification();
        notification.setContent(content);
        notification.setUserId(userId);
        return notification;
    }
}
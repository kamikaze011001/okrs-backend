package org.ptit.okrs.api.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core.service.NotificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace;

@Component
@RequiredArgsConstructor
@Slf4j
public class NoteDailyPlanSchedule {

    @Value("${application.schedule.notification.note-daily.enable:true}")
    private Boolean enable;

    private final NotificationService notificationService;

    private String content = "Please note the daily plan";

    private String userId;

    @Scheduled(cron = "${application.schedule.notification.delete-overdue.cron:0 0 0 * * *}")
    public void noteDailyNotificationSchedule() {
        log.info("(noteDailyNotificationSchedule)enable: {}", enable);
        if (!enable) {
            return;
        }

        try {
                notificationService.create(content,userId);
        } catch (Exception ex) {
            log.error("(noteDailyNotificationSchedule)ex: {}", getFullStackTrace(ex));
        }
    }
}

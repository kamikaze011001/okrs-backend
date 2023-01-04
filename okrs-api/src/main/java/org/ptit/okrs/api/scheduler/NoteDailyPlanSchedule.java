package org.ptit.okrs.api.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core.service.NotificationService;
import org.ptit.okrs.core.service.UserService;
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

    @Value("${application.schedule.notification.delete-overdue.size:500}")
    private Integer size;

    private final NotificationService notificationService;

    private final UserService userService;

    @Value("${org.ptit.okrs.api.scheduler.NoteDailyPlanSchedule}")
    private String content;

    @Scheduled(cron = "${application.schedule.notification.delete-overdue.cron:0 0 0 * * *}")
    public void noteDailyNotificationSchedule() {
        log.info("(noteDailyNotificationSchedule)enable: {}", enable);
        if (!enable) {
            return;
        }

        try {
            int page = 0;
            while (true) {
                var user = userService.searchUserId(page, size);
                log.info("userId: {}" , user);
                if (!user.isEmpty()) {
                    for (String userId : user) {
                        notificationService.create(content, userId);
                    }
                }
                if (user.size() < size) {
                    break;
                }
                page++;
            }
        } catch (Exception ex) {
            log.error("(noteDailyNotificationSchedule)ex: {}", getFullStackTrace(ex));
        }
    }
}

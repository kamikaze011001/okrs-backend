package org.ptit.okrs.api.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core.service.KeyResultService;
import org.ptit.okrs.core.service.NotificationService;
import org.ptit.okrs.core.service.ObjectiveService;
import org.ptit.okrs.core_util.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEndDateSchedule {


    @Value("${application.schedule.notification.delete-overdue.enable:true}")
    private Boolean enable;

    @Value("${application.schedule.notification.delete-overdue.size:500}")
    private Integer size;

    private final ObjectiveService objectiveService;

    private final NotificationService notificationService;

    private final KeyResultService keyResultService;

    private String contentObjective = "Your objective has expired";

    private String contentKeyResult = "Your KeyResult has expired";

    String usedId1;

    String usedId2;

    @Scheduled(cron = "${application.schedule.notification.delete-overdue.cron:0 0 0 * * *}")
    public void returnNotificationScheduleObjective() {
        log.info("(returnNotificationSchedule)enable: {}", enable);
        if (!enable) {
            return;
        }

        try {
            int page = 0;
            while (true) {
                var objectives = objectiveService.searchByEndDate(DateUtils.getCurrentDateInteger(), page, size);
                if(!objectives.isEmpty() ) {
                    notificationService.create(contentObjective, usedId1);
                }
                if (objectives.size() < size) {
                    break;
                }
                page ++;
            }
        } catch (Exception ex) {
            log.error("()ex: {}", getFullStackTrace(ex));
        }
    }

    @Scheduled(cron = "${application.schedule.notification.delete-overdue.cron:0 0 0 * * *}")
    public void returnNotificationScheduleKeyResult() {
        log.info("(returnNotificationSchedule)enable: {}", enable);
        if (!enable) {
            return;
        }

        try {
            int page = 0;
            while (true) {
                var keyResult = keyResultService.searchByEndDate(DateUtils.getCurrentDateInteger(), page, size);
                if (!keyResult.isEmpty()) {
                    notificationService.create(contentKeyResult, usedId2);
                }
                if (keyResult.size() < size) {
                    break;
                }
                page ++;
            }
        } catch (Exception ex) {
            log.error("()ex: {}", getFullStackTrace(ex));
        }
    }
}

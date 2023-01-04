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
public class DeleteOverdueNotificationSchedule {

  @Value("${application.schedule.notification.delete-overdue.enable:true}")
  private Boolean enable;

  @Value("${application.schedule.notification.delete-overdue.size:500}")
  private Integer size;

  private final NotificationService notificationService;

  @Scheduled(cron = "${application.schedule.notification.delete-overdue.cron:0 0 0 * * *}")
  public void deleteOverdueNotificationSchedule() {
    log.info("(deleteOverdueNotificationSchedule)enable: {}", enable);
    if (!enable) {
      return;
    }

    try {
      int page = 0;
      while (true) {
        var notifications = notificationService.searchByDateLessThan(System.currentTimeMillis(), page, size);
        notificationService.deleteAll(notifications);
        if (notifications.size() < size) {
          break;
        }
        page ++;
      }
    } catch (Exception ex) {
      log.error("(deleteOverdueNotificationSchedule)ex: {}", getFullStackTrace(ex));
    }
  }
}

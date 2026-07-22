package org.ptit.okrs.api.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core.repository.projection.NotificationSchedule;
import org.ptit.okrs.core.service.KeyResultService;
import org.ptit.okrs.core.service.NotificationService;
import org.ptit.okrs.core.service.ObjectiveService;
import org.ptit.okrs.core_util.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang.exception.ExceptionUtils.getFullStackTrace;
import static org.ptit.okrs.api.constant.OkrsApiConstant.NOTIFICATION_CONTENT_KEYRESULT;
import static org.ptit.okrs.api.constant.OkrsApiConstant.NOTIFICATION_CONTENT_OBJECTIVE;

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

    private final MessageSource messageSource;

    @Scheduled(fixedRate = 100000)
    public void returnNotificationScheduleObjective() {
        log.info("(returnNotificationSchedule)enable: {}", enable);
        enable();
            int page = 0;
            while (true) {
                var objectives = objectiveService.searchByEndDate(DateUtils.getCurrentDateInteger(), page, size);
                execute(NOTIFICATION_CONTENT_OBJECTIVE,objectives);

                if (objectives.size() < size) {
                    break;
                }
                page++;
            }
        }


    @Scheduled(fixedRate = 100000)
    public void returnNotificationScheduleKeyResult() {
        log.info("(returnNotificationSchedule)enable: {}", enable);
        enable();
        int page = 0;
        while (true) {
            var keyResult = keyResultService.searchByEndDate(DateUtils.getCurrentDateInteger(), page, size);
            execute(NOTIFICATION_CONTENT_KEYRESULT,keyResult);
            if (keyResult.size() < size) {
                break;
            }
            page++;
        }
    }

    private String getContent(String code, Map<String, String> paramMaps) {
            String content = messageSource.getMessage(code, null, null);
            for (String key : paramMaps.keySet()) {
                content = content.replace(getParamKey(key),paramMaps.get(key));
            }
            return content;
    }

    private String getParamKey(String key) {
        return "%" + key + "%";
    }

    private void enable() {
        if (!enable) {
            return;
        }
    }

    private void execute(String contents, List<NotificationSchedule> notificationSchedules) {
        try {
            Map<String, String> paramMaps = new HashMap<>();
            if (!notificationSchedules.isEmpty()) {
                for (NotificationSchedule n : notificationSchedules) {
                    paramMaps.put("title", n.getTitle());
                    paramMaps.put("endDate", String.valueOf(n.getEndDate()));
                    String content = getContent(contents, paramMaps);
                    notificationService.create(content, n.getUserId());
                }
            }
        } catch (Exception ex) {
            log.error("()ex: {}", getFullStackTrace(ex));
        }
    }
}

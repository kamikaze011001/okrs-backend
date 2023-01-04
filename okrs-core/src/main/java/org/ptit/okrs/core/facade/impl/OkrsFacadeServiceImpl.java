package org.ptit.okrs.core.facade.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core.constant.DailyPlanStatus;
import org.ptit.okrs.core.facade.OkrsFacadeService;
import org.ptit.okrs.core.model.ObjectiveStatsResponse;
import org.ptit.okrs.core.service.DailyPlanService;
import org.ptit.okrs.core.service.KeyResultService;
import org.ptit.okrs.core.service.NotificationService;
import org.ptit.okrs.core.service.ObjectiveService;

import static org.ptit.orks.core_audit.SecurityService.getUserId;

@RequiredArgsConstructor
@Slf4j
public class OkrsFacadeServiceImpl implements OkrsFacadeService {

  private final ObjectiveService objectiveService;

  private final KeyResultService keyResultService;

  private final DailyPlanService dailyPlanService;

  private String content = "Your daily plan status has been updated";

  private final NotificationService notificationService;

  @Override
  public ObjectiveStatsResponse statistic(String objectiveId) {
    log.info("(statistic)objectiveId : {}", objectiveId);
    objectiveService.validateExist(objectiveId);
    return ObjectiveStatsResponse.of(
        objectiveId,
        keyResultService.findAvgProgressByObjectiveId(objectiveId),
        keyResultService.countAllByObjectiveId(objectiveId),
        keyResultService.countIncompleteByObjectiveId(objectiveId));
  }

  @Override
  public void updateStatusDailyPlan(String id, DailyPlanStatus status) {
    log.info("(updateStatusDailyPlan)id: {}, status: {}", id, status);
    dailyPlanService.updateStatusDailyPlan(id, status);
    notificationService.create(content,getUserId());
  }
}

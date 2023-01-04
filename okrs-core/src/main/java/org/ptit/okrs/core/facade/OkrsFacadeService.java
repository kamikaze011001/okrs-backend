package org.ptit.okrs.core.facade;

import org.ptit.okrs.core.constant.DailyPlanStatus;
import org.ptit.okrs.core.model.ObjectiveStatsResponse;

public interface OkrsFacadeService {

  /**
   * Give the stats of an objective by id
   * @param objectiveId - id of an objective
   * @return stats of an objective
   */
  ObjectiveStatsResponse statistic(String objectiveId);

  void updateStatusDailyPlan(
          String dailyPlanId,
          DailyPlanStatus status
  );
}

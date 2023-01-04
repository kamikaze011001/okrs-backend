package org.ptit.okrs.core.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ptit.okrs.core.constant.DailyPlanStatus;
import org.ptit.okrs.core.entity.DailyPlan;
import org.ptit.okrs.core_util.DateUtils;

@Data
@NoArgsConstructor
public class DailyPlanResponse {
  private String id;
  private String title;
  private String description;
  private DailyPlanStatus status;
  private int date;
  private String note;
  private String userId;
  private String keyResultId;

  public static DailyPlanResponse from(DailyPlan dailyPlan) {
    DailyPlanResponse response = new DailyPlanResponse();
    response.setId(dailyPlan.getId());
    response.setTitle(dailyPlan.getTitle());
    response.setDescription(dailyPlan.getDescription());
    response.setStatus(dailyPlan.getStatus());
    response.setDate(dailyPlan.getDate());
    response.setNote(dailyPlan.getNote());
    response.setUserId(dailyPlan.getUserId());
    response.setKeyResultId(dailyPlan.getKeyResultId());
    return response;
  }
}

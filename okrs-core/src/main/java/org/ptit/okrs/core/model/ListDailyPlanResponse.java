package org.ptit.okrs.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ptit.okrs.core.constant.DailyPlanStatus;

@Data
@NoArgsConstructor
public class ListDailyPlanResponse {
  private String id;
  private String title;
  private DailyPlanStatus status;
  private KeyResultResponse keyResult;

  @AllArgsConstructor(staticName = "of")
  @Data
  @NoArgsConstructor
  protected static class KeyResultResponse {
    private String keyResultId;
    private String keyResultName;
  }
}

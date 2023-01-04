package org.ptit.okrs.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
public class GetDetailDailyPlanResponse extends ListDailyPlanResponse {
  private String description;
  private Integer date;
  private String note;
}

package org.ptit.okrs.api.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
public class DailyPlanUpdateRequest extends DailyPlanCreateRequest {
  private String note;
}

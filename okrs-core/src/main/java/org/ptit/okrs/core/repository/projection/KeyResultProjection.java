package org.ptit.okrs.core.repository.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyResultProjection {

  private String id;
  private String objectiveId;
  private String title;
  private String description;
  private Integer startDate;
  private Integer endDate;
  private Integer progress;
  private String userId;
}

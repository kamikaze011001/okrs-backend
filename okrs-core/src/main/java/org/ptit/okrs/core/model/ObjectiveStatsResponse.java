package org.ptit.okrs.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class ObjectiveStatsResponse {
  private String objectiveId;
  private Float progress;
  private Integer totalOfKRs;
  private Integer totalOfIncompleteKRs;
  private String objectType = "ObjectiveStats";

  public static ObjectiveStatsResponse of(
      String objectiveId, Float progress, Integer totalOfKRs, Integer totalOfIncompleteKRs) {
    ObjectiveStatsResponse response = new ObjectiveStatsResponse();
    response.setObjectiveId(objectiveId);
    response.setProgress(progress);
    response.setTotalOfKRs(totalOfKRs);
    response.setTotalOfIncompleteKRs(totalOfIncompleteKRs);
    return response;
  }
}

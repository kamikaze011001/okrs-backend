package org.ptit.okrs.core.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.ptit.okrs.core.entity.KeyResult;
import org.ptit.okrs.core.repository.projection.KeyResultProjection;

@Data
@NoArgsConstructor
public class KeyResultResponse {
  private String id;
  private String objectiveId;
  private String title;
  private String description;
  private int startDate;
  private int endDate;
  private int progress;
  private String objectType;

  public static KeyResultResponse from(KeyResult keyResult) {
    return getKeyResultResponse(
        keyResult.getId(),
        keyResult.getObjectiveId(),
        keyResult.getTitle(),
        keyResult.getDescription(),
        keyResult.getStartDate(),
        keyResult.getEndDate(),
        keyResult.getProgress());
  }

  public static KeyResultResponse from(KeyResultProjection keyResult) {
    return getKeyResultResponse(
        keyResult.getId(),
        keyResult.getObjectiveId(),
        keyResult.getTitle(),
        keyResult.getDescription(),
        keyResult.getStartDate(),
        keyResult.getEndDate(),
        keyResult.getProgress());
  }

  private static KeyResultResponse getKeyResultResponse(
      String id,
      String objectiveId,
      String title,
      String description,
      Integer startDate,
      Integer endDate,
      Integer progress) {
    KeyResultResponse response = new KeyResultResponse();
    response.setId(id);
    response.setObjectiveId(objectiveId);
    response.setTitle(title);
    response.setDescription(description);
    response.setStartDate(startDate);
    response.setEndDate(endDate);
    response.setProgress(progress);
    response.setObjectType(KeyResult.class.getSimpleName());
    return response;
  }
}

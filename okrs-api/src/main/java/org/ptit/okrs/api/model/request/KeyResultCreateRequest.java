package org.ptit.okrs.api.model.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.api.exception.PathVariableException;
import org.ptit.okrs.core.entity.KeyResult;
import org.ptit.okrs.core.exception.OkrsDateInvalidException;
import org.ptit.okrs.core.validation.ValidDateInteger;
import org.ptit.okrs.core_util.ValidationUtils;

@Data
@NoArgsConstructor
@Slf4j
public class KeyResultCreateRequest {
  @NotBlank private String objectiveId;
  @NotBlank private String title;
  private String description;
  @NotNull @ValidDateInteger private Integer startDate;
  @NotNull @ValidDateInteger private Integer endDate;

  @Min(0)
  @Max(100)
  private Integer progress;

  public void validate(String pathObjectiveId) {
    if (ValidationUtils.validateStartDateAndEndDate(startDate, endDate)) {
      log.error(
          "(validate)startDate : {}, endDate : {} --> OkrsDateInvalidException",
          startDate,
          endDate);
      throw new OkrsDateInvalidException(
          KeyResult.class.getSimpleName(), String.valueOf(startDate), String.valueOf(endDate));
    }
    validateObjectiveIdPathVariable(pathObjectiveId);
  }

  private void validateObjectiveIdPathVariable(String pathObjectiveId) {
    if (!this.objectiveId.equals(pathObjectiveId)) {
      log.error("(validatePathVariable)objectiveId : {} --> PathVariableException", pathObjectiveId);
      throw new PathVariableException(this.objectiveId, pathObjectiveId);
    }
  }
}

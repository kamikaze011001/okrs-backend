package org.ptit.okrs.api.model.request;

import java.time.temporal.ChronoUnit;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core.constant.OkrsTimeType;
import org.ptit.okrs.core.constant.OkrsType;
import org.ptit.okrs.core.entity.Objective;
import org.ptit.okrs.core.exception.OkrsDateInvalidException;
import org.ptit.okrs.core.exception.OkrsTimePeriodTypeException;
import org.ptit.okrs.core.validation.ValidDateInteger;
import org.ptit.okrs.core_util.DateUtils;
import org.ptit.okrs.core_util.ValidationUtils;

@Data
@NoArgsConstructor
@Slf4j
public class ObjectiveCreateRequest {
  @NotBlank private String title;
  private String description;
  @NotNull @ValidDateInteger private Integer startDate;
  @NotNull @ValidDateInteger private Integer endDate;
  private OkrsType type;
  private OkrsTimeType timePeriodType;

  public void validate() {
    //TODO: AnhNHS check startDate and EndDat in the past
    if (ValidationUtils.validateStartDateAndEndDate(startDate, endDate)) {
      log.error(
          "(validate)startDate : {}, endDate : {} --> OkrsDateInvalidException",
          startDate, endDate);
      throw new OkrsDateInvalidException(
          Objective.class.getSimpleName(), String.valueOf(startDate), String.valueOf(endDate));
    }
    if (checkTimePeriod()) {
      log.error(
          "(validate) startDate : {}, endDate : {}, timePeriodType : {} --> OkrsTimePeriodTypeException",
          startDate, endDate, timePeriodType);
      throw new OkrsTimePeriodTypeException(startDate, endDate, timePeriodType.name());
    }
  }

  private boolean checkTimePeriod() {
    switch (timePeriodType) {
      case A_MONTH:
      case THREE_MONTH:
      case SIX_MONTH: {
        return ChronoUnit.MONTHS.between(
                DateUtils.getLocalDate(startDate), DateUtils.getLocalDate(endDate))
            != timePeriodType.getValue();
      }
      case A_YEAR:
      case THREE_YEAR: {
        return ChronoUnit.YEARS.between(
                DateUtils.getLocalDate(startDate), DateUtils.getLocalDate(endDate))
            != timePeriodType.getValue();
      }
      default: {
        return false;
      }
    }
  }
}

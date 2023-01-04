package org.ptit.okrs.core.exception;

import org.ptit.okrs.core_exception.BadRequestException;

public class OkrsTimePeriodTypeException extends BadRequestException {

  public OkrsTimePeriodTypeException(Integer startDate, Integer endDate, String timePeriodType) {
    setCode("org.ptit.okrs.core.exception.OkrsTimePeriodTypeException");
    addParams("startDate", String.valueOf(startDate));
    addParams("endDate", String.valueOf(endDate));
    addParams("timePeriodType", timePeriodType);
  }
}

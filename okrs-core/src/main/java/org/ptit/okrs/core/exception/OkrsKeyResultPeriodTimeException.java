package org.ptit.okrs.core.exception;

import org.ptit.okrs.core_exception.BadRequestException;

public class OkrsKeyResultPeriodTimeException extends BadRequestException {

  public OkrsKeyResultPeriodTimeException(String startDate, String endDate, String objectiveId) {
    setCode("org.ptit.okrs.core.exception.OkrsKeyResultPeriodTimeException");
    addParams("startDate", startDate);
    addParams("endDate", endDate);
    addParams("objectiveId", objectiveId);
  }
}

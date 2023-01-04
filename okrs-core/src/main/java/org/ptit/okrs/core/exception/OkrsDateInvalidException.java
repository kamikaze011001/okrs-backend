package org.ptit.okrs.core.exception;

import org.ptit.okrs.core_exception.BadRequestException;

public class OkrsDateInvalidException extends BadRequestException {

  public OkrsDateInvalidException(String type, String startDate, String endDate) {
    setCode("org.ptit.okrs.api.exception.OkrsDateInvalidException");
    addParams("startDate", startDate);
    addParams("endDate", endDate);
    addParams("type", type);
  }
}

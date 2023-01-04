package org.ptit.okrs.core_exception;

public class DateInvalidException extends BadRequestException{

  public DateInvalidException(Integer date) {
    setCode("org.ptit.okrs.core_exception.DateInvalidException");
    addParams("date", String.valueOf(date));
  }
}

package org.ptit.okrs.core_exception;

public class BadRequestException extends BaseException {
  public BadRequestException() {
    setCode("org.ptit.okrs.core_exception.BadRequestException");
    setStatus(400);
  }
}

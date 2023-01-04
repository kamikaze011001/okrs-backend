package org.ptit.okrs.core_exception;

public class NotSupportException extends BaseException {
  public NotSupportException() {
    setStatus(400);
    setCode("org.ptit.okrs.core_exception.NotSupportException");
  }
}

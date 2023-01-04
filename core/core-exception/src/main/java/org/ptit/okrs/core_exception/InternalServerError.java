package org.ptit.okrs.core_exception;

public class InternalServerError extends BaseException {
  public InternalServerError() {
    setStatus(500);
    setCode("org.ptit.okrs.core_exception.InternalServerError");
  }

  public InternalServerError(String message) {
    setStatus(500);
    setMessage(message);
  }
}

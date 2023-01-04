package org.ptit.okrs.core_exception;

public class ForbiddenException extends BaseException {

  public ForbiddenException(String userId) {
    setStatus(401);
    setCode("org.ptit.okrs.core_exception.ForbiddenException");
    addParams("userId", userId);
  }
}

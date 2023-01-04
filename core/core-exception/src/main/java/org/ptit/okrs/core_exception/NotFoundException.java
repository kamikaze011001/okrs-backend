package org.ptit.okrs.core_exception;

public class NotFoundException extends BaseException {

  public NotFoundException() {
    setStatus(404);
  }

  public NotFoundException(String objectId, String objectType) {
    setStatus(404);
    setCode("org.ptit.okrs.core_exception.NotFoundException");
    addParams("id", objectId);
    addParams("type", objectType);
  }
}

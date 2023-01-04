package org.ptit.okrs.core.exception;

import org.ptit.okrs.core_exception.ConflictException;

public class TitleAlreadyExitException extends ConflictException {

  public TitleAlreadyExitException(String objectType, String objectDate) {
    setStatus(409);
    setCode("org.ptit.okrs.core.exception.TitleAlreadyExitException");
    addParams("objectType", objectType);
    addParams("objectDate", objectDate);
  }
}

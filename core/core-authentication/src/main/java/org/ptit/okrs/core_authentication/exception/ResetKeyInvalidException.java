package org.ptit.okrs.core_authentication.exception;

import org.ptit.okrs.core_exception.BadRequestException;

public class ResetKeyInvalidException extends BadRequestException {

  public ResetKeyInvalidException() {
    setCode("org.ptit.okrs.core_authentication.exception.ResetKeyInvalidException");
  }
}

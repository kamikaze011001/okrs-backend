package org.ptit.okrs.core_authentication.exception;

import org.ptit.okrs.core_exception.BadRequestException;

public class PasswordInvalidException extends BadRequestException {

  public PasswordInvalidException() {
    setCode("org.ptit.okrs.core_authentication.exception.PasswordInvalidException");
  }
}

package org.ptit.okrs.core_authentication.exception;

import org.ptit.okrs.core_exception.BadRequestException;

public class PasswordConfirmNotMatchException extends BadRequestException {
  public PasswordConfirmNotMatchException() {
    setCode("org.ptit.okrs.core_authentication.exception.PasswordConfirmNotMatchException");
  }
}

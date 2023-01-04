package org.ptit.okrs.core_authentication.exception;

import org.ptit.okrs.core_exception.ConflictException;

public class UserAlreadyHasAccountException extends ConflictException {
  public UserAlreadyHasAccountException() {
    setCode("org.ptit.okrs.core_authentication.exception.UserAlreadyHasAccountException");
  }
}

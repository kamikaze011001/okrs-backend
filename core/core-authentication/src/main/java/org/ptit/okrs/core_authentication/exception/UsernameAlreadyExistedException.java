package org.ptit.okrs.core_authentication.exception;

import org.ptit.okrs.core_exception.ConflictException;

public class UsernameAlreadyExistedException extends ConflictException {
  public UsernameAlreadyExistedException(String username) {
    addParams("username", username);
    setCode("org.ptit.okrs.core_authentication.exception.UsernameAlreadyExistedException");
  }
}

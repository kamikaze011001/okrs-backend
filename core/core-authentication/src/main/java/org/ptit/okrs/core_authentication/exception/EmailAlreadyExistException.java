package org.ptit.okrs.core_authentication.exception;

import org.ptit.okrs.core_exception.ConflictException;

public class EmailAlreadyExistException extends ConflictException {
  public EmailAlreadyExistException(String email) {
    addParams("email", email);
    setCode("org.ptit.okrs.core_authentication.exception.EmailAlreadyExistException");
  }
}

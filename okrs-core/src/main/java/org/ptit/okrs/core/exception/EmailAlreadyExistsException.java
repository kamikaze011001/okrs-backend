package org.ptit.okrs.core.exception;

import org.ptit.okrs.core_exception.ConflictException;

public class EmailAlreadyExistsException extends ConflictException {

  public EmailAlreadyExistsException(String email) {
    setStatus(409);
    setCode("org.ptit.okrs.core.exception.EmailAlreadyExistsException");
    addParams("email", email);
  }
}

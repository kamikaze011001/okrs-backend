package org.ptit.okrs.core_authentication.exception;

import org.ptit.okrs.core_exception.BadRequestException;

public class EmailNotRegisteredException extends BadRequestException {

  public EmailNotRegisteredException(String email) {
    setCode("org.ptit.okrs.core_authentication.exception.ConflictDataException");
    addParams("email", email);
  }
}

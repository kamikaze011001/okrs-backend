package org.ptit.okrs.core.exception;

import org.ptit.okrs.core_exception.BadRequestException;

public class EmailInvalidException extends BadRequestException {

  public EmailInvalidException(String email) {
    setCode("org.ptit.okrs.core.exception.EmailInvalidException");
    setStatus(400);
    addParams("email", email);
  }
}

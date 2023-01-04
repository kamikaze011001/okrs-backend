package org.ptit.okrs.core_authentication.exception;

import org.ptit.okrs.core_exception.NotFoundException;

public class EmailNotExistsException extends NotFoundException {

  public EmailNotExistsException(String email) {
    setCode("org.ptit.okrs.core_authentication.exception.EmailNotExistsException");
    addParams("email", email);
  }
}

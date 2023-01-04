package org.ptit.okrs.core_authentication.exception;

import org.ptit.okrs.core_exception.NotFoundException;

public class AuthUserNotFoundWithEmail extends NotFoundException {
  public AuthUserNotFoundWithEmail(String email) {
    addParams("email", email);
    setCode("org.ptit.okrs.core_authentication.exception.AuthUserNotFoundWithEmail");
  }
}

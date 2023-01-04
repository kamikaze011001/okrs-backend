package org.ptit.okrs.core_authentication.exception;

import org.ptit.okrs.core_exception.BadRequestException;

public class UsernameNotFoundException extends BadRequestException {

  public UsernameNotFoundException(String username) {
    addParams("username", username);
    setCode("org.ptit.okrs.core_authentication.exception.UsernameNotFoundException");
  }
}

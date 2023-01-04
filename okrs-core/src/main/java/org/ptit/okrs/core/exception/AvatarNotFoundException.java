package org.ptit.okrs.core.exception;

import org.ptit.okrs.core_exception.NotFoundException;

public class AvatarNotFoundException extends NotFoundException {

  public AvatarNotFoundException(String userId) {
    setStatus(404);
    setCode("org.ptit.okrs.core.exception.AvatarNotFoundException");
    addParams("userId", userId);
  }
}

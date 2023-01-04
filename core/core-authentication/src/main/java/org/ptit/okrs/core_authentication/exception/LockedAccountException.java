package org.ptit.okrs.core_authentication.exception;

import org.ptit.okrs.core_exception.ForbiddenException;

public class LockedAccountException extends ForbiddenException {

  public LockedAccountException(String userId) {
    super(userId);
  }
}

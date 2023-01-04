package org.ptit.okrs.core_authentication.exception;

public class PermanentLockException extends LockedAccountException {

  public PermanentLockException(String userId, Long failAttempts) {
    super(userId);
    setCode("org.ptit.okrs.core_authentication.exception.PermanentLockException");
    addParams("failAttempts", String.valueOf(failAttempts));
  }
}

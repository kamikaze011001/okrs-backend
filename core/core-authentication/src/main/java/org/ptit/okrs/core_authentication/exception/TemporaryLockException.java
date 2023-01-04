package org.ptit.okrs.core_authentication.exception;

public class TemporaryLockException extends LockedAccountException {

  public TemporaryLockException(String userId, Long failAttempts, Long unlockTime) {
    super(userId);
    setCode("org.ptit.okrs.core_authentication.exception.TemporaryLockException");
    addParams("failAttempts", String.valueOf(failAttempts));
    addParams("unlockTime", String.valueOf(unlockTime));
  }
}

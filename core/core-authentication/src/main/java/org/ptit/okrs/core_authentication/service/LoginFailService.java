package org.ptit.okrs.core_authentication.service;

import org.ptit.okrs.core_redis.BaseRedisHashService;

public interface LoginFailService extends BaseRedisHashService<Long> {

  /**
   * get failed attempts by email
   *
   * @param email - email of user
   * @return failed attempts of account user
   */
  Long getFailAttempts(String email);

  /**
   * get unlock time by email
   *
   * @param email - email of user
   * @return unlock time of account user
   */
  Long getUnlockTime(String email);

  /**
   * increase after login fail use when logging password don't match with account password
   *
   * @param email - email of user account
   */
  void increaseFailAttempts(String email);

  /**
   * check account lock or not
   *
   * @param email - email of user account
   * @return boolean
   */
  Boolean isTemporaryLock(String email);

  /**
   * reset fail attempt and unlock after login success
   *
   * @param email - email of user account
   */
  void resetFailAttempts(String email);

  /**
   * lock account when login fail reach to limit use when logging password don't match with account
   * password
   *
   * @param email - email of user account
   */
  void setLock(String email);

  /**
   * check user is lock or not
   *
   * @param email - email of user
   * @param userId - id of user
   * @param isLockPermanent - status of lock permanent
   */
  void checkLock(String email, String userId, Boolean isLockPermanent);
}

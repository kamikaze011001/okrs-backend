package org.ptit.okrs.core_authentication.service.impl;

import static org.ptit.okrs.core_authentication.constant.CacheConstant.LoginFail.KEY_CACHE_FAIL_ATTEMPTS;
import static org.ptit.okrs.core_authentication.constant.CacheConstant.LoginFail.KEY_CACHE_UNLOCK_TIME;
import static org.ptit.okrs.core_authentication.constant.LoginFailConstant.FIRST_LOCK_LIMIT;
import static org.ptit.okrs.core_authentication.constant.LoginFailConstant.FIRST_LOCK_TIME;
import static org.ptit.okrs.core_authentication.constant.LoginFailConstant.INIT_FAIL_ATTEMPTS;
import static org.ptit.okrs.core_authentication.constant.LoginFailConstant.SECOND_LOCK_LIMIT;
import static org.ptit.okrs.core_authentication.constant.LoginFailConstant.SECOND_LOCK_TIME;
import static org.ptit.okrs.core_authentication.constant.LoginFailConstant.THIRD_LOCK_LIMIT;

import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core_authentication.exception.PermanentLockException;
import org.ptit.okrs.core_authentication.exception.TemporaryLockException;
import org.ptit.okrs.core_authentication.service.AuthAccountService;
import org.ptit.okrs.core_authentication.service.LoginFailService;
import org.ptit.okrs.core_redis.BaseRedisHashServiceImpl;
import org.ptit.okrs.core_util.DateUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public class LoginFailServiceImpl extends BaseRedisHashServiceImpl<Long>
    implements LoginFailService {

  private final AuthAccountService authAccountService;

  public LoginFailServiceImpl(
      RedisTemplate<String, Object> redisTemplate, AuthAccountService authAccountService) {
    super(redisTemplate);
    this.authAccountService = authAccountService;
  }

  @Override
  public Long getFailAttempts(String email) {
    log.info("(getFailAttempts)email : {}", email);
    return (Long) get(KEY_CACHE_FAIL_ATTEMPTS, email);
  }

  @Override
  public Long getUnlockTime(String email) {
    log.info("(getUnlockTime)email : {}", email);
    return (Long) get(KEY_CACHE_UNLOCK_TIME, email);
  }

  @Override
  public void increaseFailAttempts(String email) {
    log.info("(increaseFailAttempts)email : {}", email);
    Long failAttempts = (Long) get(KEY_CACHE_FAIL_ATTEMPTS, email);
    if (failAttempts == null) {
      failAttempts = INIT_FAIL_ATTEMPTS;
    }
    failAttempts++;
    set(KEY_CACHE_FAIL_ATTEMPTS, email, failAttempts);
  }

  @Override
  public Boolean isTemporaryLock(String email) {
    log.info("(isAccountLock)email : {}", email);
    Long unlockTime = (Long) get(KEY_CACHE_UNLOCK_TIME, email);
    if (unlockTime == null) {
      return false;
    }
    return DateUtils.getCurrentEpoch() < unlockTime;
  }

  @Override
  public void resetFailAttempts(String email) {
    log.info("(resetFailAttempts)email : {}", email);
    delete(KEY_CACHE_FAIL_ATTEMPTS, email);
    delete(KEY_CACHE_UNLOCK_TIME, email);
  }

  @Override
  @Transactional
  public void setLock(String email) {
    log.info("(setLockAccount)email : {}", email);
    Long failAttempts = (Long) get(KEY_CACHE_FAIL_ATTEMPTS, email);
    log.info("(setLockAccount)failAttempts : {}", failAttempts);
    if (failAttempts.equals(THIRD_LOCK_LIMIT)) {
      authAccountService.enableLockPermanent(email);
    }
    if (failAttempts.equals(SECOND_LOCK_LIMIT)) {
      set(KEY_CACHE_UNLOCK_TIME, email, DateUtils.getCurrentEpoch() + SECOND_LOCK_TIME);
    }
    if (failAttempts.equals(FIRST_LOCK_LIMIT)) {
      set(KEY_CACHE_UNLOCK_TIME, email, DateUtils.getCurrentEpoch() + FIRST_LOCK_TIME);
    }
  }

  @Override
  public void checkLock(String email, String userId, Boolean isLockPermanent) {

    if (isLockPermanent) {
      throw new PermanentLockException(userId, getFailAttempts(email));
    }
    if (isTemporaryLock(email)) {
      throw new TemporaryLockException(userId, getFailAttempts(email), getUnlockTime(email));
    }
  }
}

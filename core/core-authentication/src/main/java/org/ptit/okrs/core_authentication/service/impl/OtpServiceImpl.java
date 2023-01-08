package org.ptit.okrs.core_authentication.service.impl;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core_authentication.exception.OTPInvalidException;
import org.ptit.okrs.core_authentication.exception.OtpBadRequestException;
import org.ptit.okrs.core_authentication.exception.OtpNotFoundException;
import org.ptit.okrs.core_authentication.service.AuthAccountService;
import org.ptit.okrs.core_authentication.service.OtpService;
import org.ptit.okrs.core_redis.BaseRedisServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
public class OtpServiceImpl extends BaseRedisServiceImpl<String> implements OtpService {


  public OtpServiceImpl(RedisTemplate<String, Object> redisTemplate, long timeOut, TimeUnit unitTimeOut) {
    super(redisTemplate, timeOut, unitTimeOut);
  }

  @Override
  protected boolean isSavePersistent() {
    return false;
  }

  @Override
  public void validateOtp(String email, String otpRequest) {
    log.info("(validateOtp)email: {}, otpRequest: {}", email, otpRequest);
    var otpCache = get(email);
    if (!Objects.equals(otpCache, otpRequest)) {
      log.error("(verifyOtpForgotPassword)OTP: {} invalid", otpRequest);
      throw new OTPInvalidException();
    }
  }
}

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

  private final AuthAccountService accountService;

  public OtpServiceImpl(RedisTemplate<String, Object> redisTemplate, long timeOut, TimeUnit unitTimeOut,
      AuthAccountService accountService) {
    super(redisTemplate, timeOut, unitTimeOut);
    this.accountService = accountService;
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

  @Override
  public void checkOtpRedis(String email, String otp) {
    log.info("(checkOtpRedis)email: {}, otp: {}", email, otp);
    var otpRedis = get(email);
    if (Objects.isNull(otpRedis)) {
      throw new OtpNotFoundException(otp, "Otp code has expired!");
    }
    if (Objects.equals(otpRedis, otp)) {
      accountService.activeByEmail(email);
    } else {
      throw new OtpBadRequestException(otp, "Otp code is not correct!");
    }
  }
}

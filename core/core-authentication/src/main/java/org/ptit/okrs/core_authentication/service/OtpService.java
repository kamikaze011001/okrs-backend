package org.ptit.okrs.core_authentication.service;

import org.ptit.okrs.core_redis.BaseRedisService;

public interface OtpService extends BaseRedisService<String> {

  /**
   * compare otp with otp save in cache
   * @param email - the email is key to save otp in cache
   * @param otpRequest - the otpReuqest is otp can compare
   */
  void validateOtp(String email, String otpRequest);

  void checkOtpRedis(String email, String otp);
}

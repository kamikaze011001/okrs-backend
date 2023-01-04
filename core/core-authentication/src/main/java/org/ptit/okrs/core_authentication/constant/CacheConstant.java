package org.ptit.okrs.core_authentication.constant;

public class CacheConstant {

  public static class CacheResetPassword {
    public static final String KEY_CACHE_RESET_PASSWORD = "reset-password-key";
  }
  public static class CacheVerifyOtpForgotPassword {
    public static final String KEY_CACHE_RESET_PASSWORD = "reset-password-key";
  }
  public static class LoginFail {
    public static final String KEY_CACHE_FAIL_ATTEMPTS = "fail-attempts";
    public static final String KEY_CACHE_UNLOCK_TIME = "unlock-time";
  }

  public static class CacheToken {
    public static final String KEY_CACHE_ACCESS_TOKEN = "access-token";
    public static final String KEY_CACHE_REFRESH_TOKEN = "refresh-token";
  }
}

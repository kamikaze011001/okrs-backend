package org.ptit.okrs.core_authentication.exception;

import org.ptit.okrs.core_exception.NotFoundException;

public class OtpNotFoundException extends NotFoundException {

  public OtpNotFoundException(String objectOtp, String message) {
    setCode("org.ptit.okrs.core_authentication.exception.OtpNotFoundException");
    addParams("otp", objectOtp);
    setMessage(message);
  }
}

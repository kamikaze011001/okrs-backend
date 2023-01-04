package org.ptit.okrs.core_authentication.exception;

import org.ptit.okrs.core_exception.BadRequestException;

public class OtpBadRequestException extends BadRequestException {

  public OtpBadRequestException(String objectOtp, String message) {
    setCode("org.ptit.okrs.core_authentication.exception.OtpBadRequestException");
    addParams("otp", objectOtp);
    setMessage(message);
  }
}

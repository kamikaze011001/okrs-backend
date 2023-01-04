package org.ptit.okrs.core_authentication.exception;

import org.ptit.okrs.core_exception.BadRequestException;

public class OTPInvalidException extends BadRequestException {

  public OTPInvalidException() {
    setCode("org.ptit.okrs.core_authentication.exception.OTPInvalidException");
    setStatus(400);
  }
}

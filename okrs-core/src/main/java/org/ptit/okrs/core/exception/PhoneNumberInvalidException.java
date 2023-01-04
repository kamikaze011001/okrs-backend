package org.ptit.okrs.core.exception;

import org.ptit.okrs.core_exception.BadRequestException;

public class PhoneNumberInvalidException extends BadRequestException {

  public PhoneNumberInvalidException(String phoneNumber) {
    setCode("org.ptit.okrs.core.exception.PhoneNumberInvalidException");
    setStatus(400);
    addParams("phoneNumber", phoneNumber);
  }
}

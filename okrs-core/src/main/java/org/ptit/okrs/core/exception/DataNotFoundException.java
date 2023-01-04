package org.ptit.okrs.core.exception;

import org.ptit.okrs.core_exception.NotFoundException;

public class DataNotFoundException extends NotFoundException {

  public DataNotFoundException(String fieldName, String objectName) {
    setStatus(404);
    setCode("org.ptit.okrs.core.exception.DataNotFoundException");
    addParams("fieldName", fieldName);
    addParams("objectName", objectName);
  }
}

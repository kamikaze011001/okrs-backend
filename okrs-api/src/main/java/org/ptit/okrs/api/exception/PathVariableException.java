package org.ptit.okrs.api.exception;

import org.ptit.okrs.core_exception.BadRequestException;

public class PathVariableException extends BadRequestException {

  public PathVariableException(String requestId, String pathId) {
    setCode("org.ptit.okrs.api.exception.PathVariableException");
    addParams("requestId", requestId);
    addParams("pathId", pathId);
  }
}

package org.ptit.okrs.core.exception;

import org.ptit.okrs.core_exception.BadRequestException;

public class ExistedKeyResultTitleException extends BadRequestException {

  public ExistedKeyResultTitleException(String title, String objectiveId) {
    setCode("org.ptit.okrs.core.exception.ExistedKeyResultTitleException");
    addParams("title", title);
    addParams("objectiveId", objectiveId);
  }
}

package org.ptit.okrs.core_upload.exception;

import org.ptit.okrs.core_exception.BadRequestException;

public class FileNameInvalidException extends BadRequestException {

  public FileNameInvalidException(String fileName) {
    setCode("org.ptit.okrs.core_upload.exception.FileNameInvalidException");
    setStatus(400);
    addParams("fileName", fileName);
  }
}

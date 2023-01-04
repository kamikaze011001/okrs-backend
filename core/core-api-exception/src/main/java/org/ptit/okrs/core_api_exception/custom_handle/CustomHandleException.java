package org.ptit.okrs.core_api_exception.custom_handle;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.ptit.okrs.core_api_exception.model.ErrorResponse;
import org.ptit.okrs.core_exception.BaseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@Slf4j
public class CustomHandleException {
  private static final String ERROR_CODE_FILE_SIZE_LIMIT_EXCEPTION = "org.ptit.okrs.core_api_exception.custom_handle.CustomHandleException.FileUploadException";
  private final MessageSource messageSource;

  @Value("${spring.servlet.multipart.max-file-size: 2}")
  private String maxSize;

  public CustomHandleException(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @ExceptionHandler(BaseException.class)
  public ResponseEntity<ErrorResponse> handleException(BaseException ex, WebRequest webRequest) {
    log.info("(handleException)ex: {}, locale: {}", ex.getCode(), webRequest.getLocale());

    return new ResponseEntity<>(
        getError(ex.getStatus(), ex.getCode(), webRequest.getLocale(), ex.getParams()),
        HttpStatus.valueOf(ex.getStatus())
    );
  }

  @ExceptionHandler(FileSizeLimitExceededException.class)
  public ResponseEntity<ErrorResponse> fileSizeLimitExceededHandleException(
      FileSizeLimitExceededException ex, WebRequest webRequest) {
    log.info(
        "(fileSizeLimitExceededHandleException)fileName: {}, locale: {}",
        ex.getFileName(),
        webRequest.getLocale());
    var params = new HashMap<String, String>();
    params.put("fileName", ex.getFileName());
    params.put("size", maxSize);

    return new ResponseEntity<>(
        getError(
            HttpStatus.BAD_REQUEST.value(),
            ERROR_CODE_FILE_SIZE_LIMIT_EXCEPTION,
            webRequest.getLocale(),
            params),
        HttpStatus.BAD_REQUEST);
  }

  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException exception) {
    log.info("(handleValidationExceptions)exception: {}", exception.getMessage());
    Map<String, String> errors = new HashMap<>();
    exception
        .getBindingResult()
        .getAllErrors()
        .forEach(
            error -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            });
    log.info("(handleValidationExceptions) {}", errors);
    return new ResponseEntity<>(
        ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "org.ptit.okrs.core_api_exception.custom_handle.CustomHandleException",
            errors),
        HttpStatus.BAD_REQUEST);
  }

  private ErrorResponse getError(int status, String code, Locale locale, Map<String, String > params) {
    return ErrorResponse.of(status, code, getMessage(code, locale, params));
  }

  private String getMessage(String code, Locale locale, Map<String, String> params) {
    var message = getMessage(code, locale);
    if (params != null && !params.isEmpty()) {
      for (var param : params.entrySet()) {
        message = message.replace(getMessageParamsKey(param.getKey()), param.getValue());
      }
    }
    return message;
  }

  private String getMessage(String code, Locale locale) {
    try {
      return messageSource.getMessage(code, null, locale);
    } catch (Exception ex) {
      return code;
    }
  }

  private String getMessageParamsKey(String key) {
    return "%" + key + "%";
  }
}

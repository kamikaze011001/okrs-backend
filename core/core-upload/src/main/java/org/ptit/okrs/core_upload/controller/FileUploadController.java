package org.ptit.okrs.core_upload.controller;

import static org.ptit.okrs.core_upload.constant.CoreUploadConstant.BaseUrl.UPLOAD_BASE_URL;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core_upload.response.BaseResponse;
import org.ptit.okrs.core_upload.service.CoreUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(UPLOAD_BASE_URL)
public class FileUploadController {
  private final CoreUploadService service;

  /**
   * maxSize is variable save limit size of the file upload,
   * max size of the file have unit is kb
   */
  @Value("${spring.servlet.multipart.max-file-size: 2}")
  private String maxSize;

  @ApiOperation("Upload avatar to server")
  @ApiResponse(code = 200, response = BaseResponse.class, message = "Successfully response.")
  @PostMapping(consumes = "multipart/form-data")
  @ResponseStatus(HttpStatus.OK)
  public BaseResponse uploadFile(
      @RequestParam(name = "file", required = false) MultipartFile file) {
    log.info("(uploadFile)fileName: {}", file.getOriginalFilename());
    return BaseResponse.of(HttpStatus.OK.value(), service.uploadFile(file));
  }
}

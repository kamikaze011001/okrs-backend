package org.ptit.okrs.core_upload.service;

import org.springframework.web.multipart.MultipartFile;

public interface CoreUploadService {

  /**
   * upload file from server
   * @param file - file select from device of client
   * @return path of file uploaded
   */
  String uploadFile(MultipartFile file);
}

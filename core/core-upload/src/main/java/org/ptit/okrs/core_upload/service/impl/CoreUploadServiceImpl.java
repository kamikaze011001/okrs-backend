package org.ptit.okrs.core_upload.service.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core_exception.InternalServerError;
import org.ptit.okrs.core_upload.exception.FileNameInvalidException;
import org.ptit.okrs.core_upload.service.CoreUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
public class CoreUploadServiceImpl implements CoreUploadService {

  private String pathStorageFile;
  private final Path fileStorageLocation;

  public CoreUploadServiceImpl(String pathStorageFile) {
    this.pathStorageFile = pathStorageFile;
    this.fileStorageLocation = Paths.get(pathStorageFile).normalize();
    createFolderStorage(fileStorageLocation);
  }

  /**
   * upload file from server
   * @param file - file select from device of client
   * @return path of file uploaded
   */
  @Override
  public String uploadFile(MultipartFile file) {
    log.info("(uploadFile)fileName: {}", file.getOriginalFilename());
    var fileName = file.getOriginalFilename();
    validateFileName(fileName);

    try {
      fileName = UUID.randomUUID() + "_" + new Date().getTime() + "." + getFileExtension(file.getOriginalFilename());
      Path targetLocation = fileStorageLocation.resolve(fileName);
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

      return buildPathAvatar(fileName, pathStorageFile);
    } catch (Exception ex) {
      log.error("(uploadFile)exception: {}", ex.toString());
      throw new InternalServerError(
          "Could not upload file " + file.getOriginalFilename() + ". Please try again!");
    }
  }

  /**
   * get File Extension of the file uploaded to the server
   * @param fileName - the name of the file uploaded to the server
   * @return the file name extensions of the file uploaded to the server
   */
  private String getFileExtension(String fileName) {
    log.info("(getFileExtension)fileName: {}", fileName);
    String[] fileNameParts = fileName.split("\\.");
    return fileNameParts[fileNameParts.length - 1];
  }

  /**
   * create folder follow path is nominated : is absolute path
   *
   * @param fileStorageLocation - the path to save file is uploaded up server
   */
  private void createFolderStorage(Path fileStorageLocation) {
    log.info("(createFolderStorage)fileStorageLocation: {}", fileStorageLocation);
    try {
      Files.createDirectories(fileStorageLocation);
    } catch (Exception ex) {
      log.error("(createFolderStorage)exception: {}", ex.getMessage());
      throw new InternalServerError("Unable to create folder containing uploaded files.");
    }
  }

  private void validateFileName(String fileName) {
    log.info("(validateFileName)fileName: {}", fileName);
    if (!fileName.contains(".")) {
      log.error("(uploadFile)fileName: {} invalid", fileName);
      throw new FileNameInvalidException(fileName);
    }
  }

  private String buildPathAvatar(String fileName, String pathStorageFile) {
    log.info("(buildPathAvatar)fileName: {}, pathStorageFile: {}", fileName, pathStorageFile);
    var replacePathStorageFile = pathStorageFile.replace("\\", "/");
    return replacePathStorageFile + "/" + fileName;
  }
}

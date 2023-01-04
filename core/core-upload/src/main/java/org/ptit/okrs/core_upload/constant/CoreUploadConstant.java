package org.ptit.okrs.core_upload.constant;

import static org.ptit.okrs.core_upload.constant.CoreUploadConstant.ApiConstant.API_PREFIX;
import static org.ptit.okrs.core_upload.constant.CoreUploadConstant.ApiConstant.API_VERSION;
import static org.ptit.okrs.core_upload.constant.CoreUploadConstant.ResourceConstant.UPLOAD;

public class CoreUploadConstant {
  public static class ApiConstant {
    public static final String API_PREFIX = "/api";
    public static final String API_VERSION = "/v1";
  }

  public static class ResourceConstant {
    public static final String UPLOAD = "/uploads";

  }

  public static class BaseUrl {
    public static final String UPLOAD_BASE_URL = API_PREFIX + API_VERSION + UPLOAD;
  }
}

package org.ptit.okrs.core_upload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ptit.okrs.core_util.DateUtils;

@AllArgsConstructor(staticName = "of")
@Data
@NoArgsConstructor
public class BaseResponse {
  private int status;
  private String timestamp;
  private Object data;

  public static BaseResponse of(int status, Object data) {
    return BaseResponse.of(status, DateUtils.getCurrentDateTimeStr(), data);
  }

  public static BaseResponse of(int status) {
    return BaseResponse.of(status, DateUtils.getCurrentDateTimeStr());
  }
}

package org.ptit.okrs.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ptit.okrs.core_util.DateUtils;

@AllArgsConstructor(staticName = "of")
@Data
@NoArgsConstructor
public class OkrsResponse {
  private int status;
  private String timestamp;
  private Object data;

  public static OkrsResponse of(int status, Object data) {
    return OkrsResponse.of(status, DateUtils.getCurrentDateTimeStr(), data);
  }

  public static OkrsResponse of(int status) {
    return OkrsResponse.of(status, DateUtils.getCurrentDateTimeStr(), null);
  }
}

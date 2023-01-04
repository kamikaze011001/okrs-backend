package org.ptit.okrs.api.model.request;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.api.exception.PathVariableException;

@Data
@NoArgsConstructor
@Slf4j
public class KeyResultUpdateProgressRequest {
  @NotBlank private String id;

  @Min(0)
  @Max(100)
  private Integer progress;

  public void validate(String pathVariableId) {
    if (!this.id.equals(pathVariableId)) {
      log.error("(validate)pathVariableId : {} --> PathVariableException", pathVariableId);
      throw new PathVariableException(this.id, pathVariableId);
    }
  }
}

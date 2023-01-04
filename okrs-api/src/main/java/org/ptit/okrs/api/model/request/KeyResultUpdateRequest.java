package org.ptit.okrs.api.model.request;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.api.exception.PathVariableException;

@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class KeyResultUpdateRequest extends KeyResultCreateRequest {
  @NotBlank private String id;

  public void validate(String pathVariableId, String objectiveId) {
    super.validate(objectiveId);
    if (!this.id.equals(pathVariableId)) {
      log.error("id : {} --> PathVariableException", pathVariableId);
      throw new PathVariableException(this.id, pathVariableId);
    }
  }
}

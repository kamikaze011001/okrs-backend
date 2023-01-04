package org.ptit.okrs.api.model.request;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.api.exception.PathVariableException;

@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class ObjectiveUpdateRequest extends ObjectiveCreateRequest {
  @NotBlank private String id;

  public void validate(String pathVariableId) {
    super.validate();
    if (!id.equals(pathVariableId)) {
      log.error("(validate)pathVariableId : {}", pathVariableId);
      throw new PathVariableException(id, pathVariableId);
    }
  }
}

package org.ptit.okrs.core.entity.base;

import lombok.Data;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.MappedSuperclass;

@Data
@MappedSuperclass
public class BaseEntityWithUpdater extends BaseEntity {

  @LastModifiedBy
  private String lastUpdatedBy;

  @LastModifiedDate
  private Long lastUpdatedAt;
}

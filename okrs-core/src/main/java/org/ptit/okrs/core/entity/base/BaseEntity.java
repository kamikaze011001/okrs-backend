package org.ptit.okrs.core.entity.base;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import java.util.Objects;
import java.util.UUID;

@Data
@MappedSuperclass
public class BaseEntity {
  @Id
  private String id;

  @CreatedBy
  private String createdBy;

  @CreatedDate
  private Long createdAt;

  @PrePersist
  public void ensureId() {
    this.id = Objects.isNull(this.id) ? UUID.randomUUID().toString() : this.id;
  }
}

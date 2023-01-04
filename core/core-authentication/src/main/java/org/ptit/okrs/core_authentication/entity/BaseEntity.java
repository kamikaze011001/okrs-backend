package org.ptit.okrs.core_authentication.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

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

  @LastModifiedBy
  private String lastUpdatedBy;

  @LastModifiedDate
  private Long lastUpdatedAt;

  @PrePersist
  public void ensureId() {
    this.id = Objects.isNull(this.id) ? UUID.randomUUID().toString() : this.id;
  }
}

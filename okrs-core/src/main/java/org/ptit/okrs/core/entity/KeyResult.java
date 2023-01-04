package org.ptit.okrs.core.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ptit.okrs.core.entity.base.BaseEntityWithUpdater;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@Table(name = "key_result")
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@EntityListeners(AuditingEntityListener.class)
public class KeyResult extends BaseEntityWithUpdater {
  @Column(nullable = false)
  private String objectiveId;

  @Column(nullable = false)
  private String title;

  private String description;
  private Integer startDate;
  private Integer endDate;
  private Integer progress;

  @Column(nullable = false)
  private String userId;
}

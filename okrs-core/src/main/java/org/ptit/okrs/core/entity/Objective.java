package org.ptit.okrs.core.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ptit.okrs.core.constant.OkrsTimeType;
import org.ptit.okrs.core.constant.OkrsType;
import org.ptit.okrs.core.entity.base.BaseEntityWithUpdater;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Data
@Table(name = "objective")
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@EntityListeners(AuditingEntityListener.class)
public class Objective extends BaseEntityWithUpdater {

  @Column(nullable = false)
  private String title;

  private String description;

  @Column(nullable = false)
  private Integer startDate;

  private Integer endDate;

  @Enumerated(EnumType.STRING)
  private OkrsType type;

  @Enumerated(EnumType.STRING)
  private OkrsTimeType timePeriodType;

  @Column(nullable = false)
  private String userId;
}

package org.ptit.okrs.core.repository;

import java.util.List;
import org.ptit.okrs.core.entity.Objective;
import org.ptit.okrs.core.repository.base.BaseRepository;
import org.ptit.okrs.core.repository.projection.NotificationSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ObjectiveRepository extends BaseRepository<Objective> {
  List<Objective> findByUserId(String userId);

  Optional<Objective> findByIdAndUserId(String id, String userId);

  @Query(value = "select new org.ptit.okrs.core.repository.projection.NotificationSchedule(o.userId, o.title, o.endDate) from Objective o where :date - o.endDate >= 0")
  Page<NotificationSchedule> findByEndDate(Integer date, Pageable pageable);
}
package org.ptit.okrs.core.repository;

import org.ptit.okrs.core.entity.Notification;
import org.ptit.okrs.core.repository.base.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends BaseRepository<Notification> {

  Page<Notification> findByUserId(String userId, Pageable pageable);

  @Query("select n from Notification n where :date - n.lastUpdatedAt > (86400*1000*7)")
  Page<Notification> findByCreatedAtLessThan(long date, Pageable pageable);
}

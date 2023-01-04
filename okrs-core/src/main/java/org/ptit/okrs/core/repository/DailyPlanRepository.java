package org.ptit.okrs.core.repository;

import java.util.List;
import org.ptit.okrs.core.constant.DailyPlanStatus;
import org.ptit.okrs.core.entity.DailyPlan;
import org.ptit.okrs.core.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DailyPlanRepository extends BaseRepository<DailyPlan> {

  List<DailyPlan> findByDateAndUserId(Integer date, String userId);

  List<DailyPlan> findByKeyResultIdAndUserId(String keyResultId, String userId);

  Boolean existsByTitleAndDate(String title, Integer date);

  @Query("update DailyPlan d set d.status = :status where d.id = :id")
  @Modifying
  void updateStatus(@Param("id")String id, @Param("status")DailyPlanStatus status);

  @Query("update DailyPlan d set d.keyResultId = :keyResultId where d.id = :id")
  @Modifying
  DailyPlan updateKeyResultId(@Param("id")String id, @Param("keyResultId")String keyResultId);
}

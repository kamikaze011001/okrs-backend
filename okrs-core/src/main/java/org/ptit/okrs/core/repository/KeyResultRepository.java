package org.ptit.okrs.core.repository;

import java.util.List;
import java.util.Optional;
import org.ptit.okrs.core.entity.KeyResult;
import org.ptit.okrs.core.repository.base.BaseRepository;
import org.ptit.okrs.core.repository.projection.KeyResultProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KeyResultRepository extends BaseRepository<KeyResult> {

  @Query("select count(kr) from KeyResult kr where kr.objectiveId = :objectiveId")
  Optional<Integer> countAllKeyResults(String objectiveId);

  @Query(
          "select count(kr) from KeyResult kr where kr.objectiveId = :objectiveId and kr.progress < 100")
  Optional<Integer> countIncompleteKeyResults(String objectiveId);

  @Query("select avg(kr.progress) from KeyResult kr where kr.objectiveId = :objectiveId")
  Optional<Float> findAvgProgress(String objectiveId);

  void deleteAllByObjectiveId(String objectiveId);

  @Query("select kr.userId from KeyResult kr where kr.id = :id")
  String findUserIdById(String id);

  Boolean existsByTitleAndObjectiveId(String title, String objectiveId);

  @Query(
      "select new org.ptit.okrs.core.repository.projection.KeyResultProjection(kr.id,"
          + " kr.objectiveId,"
          + " kr.title,"
          + " kr.description,"
          + " kr.startDate,"
          + " kr.endDate,"
          + " kr.progress,"
          + " kr.userId) from KeyResult kr where kr.objectiveId = :objectiveId")
  List<KeyResultProjection> find(@Param("objectiveId") String objectiveId);

  @Modifying
  @Query("update KeyResult kr set kr.progress = :progress where kr.id = :id")
  void updateProgress(String id, Integer progress);

  @Query("select k from KeyResult k where :date - k.endDate >= 0")
  Page<KeyResult> findByEndDate(Integer date, Pageable pageable);
}

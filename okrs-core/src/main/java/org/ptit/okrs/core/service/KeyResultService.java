package org.ptit.okrs.core.service;

import java.util.List;
import org.ptit.okrs.core.entity.KeyResult;
import org.ptit.okrs.core.model.KeyResultResponse;
import org.ptit.okrs.core.service.base.BaseService;

public interface KeyResultService extends BaseService<KeyResult> {

  /**
   * Create new key result
   *
   * @param objectiveId - id of the objective that this key result belongs to
   * @param title - title of key result
   * @param description - description of key result
   * @param startDate - the day that start key result
   * @param endDate - the day that end key result
   * @param progress - progress of key result
   * @param userId - id of user that this key result belongs to
   * @return the created key result
   */
  KeyResultResponse create(
      String objectiveId,
      String title,
      String description,
      Integer startDate,
      Integer endDate,
      Integer progress,
      String userId);

  /**
   * count KRs of an objective
   * @param objectiveId - id of an objective
   * @return quantity of all KRs of an objective
   */
  Integer countAllByObjectiveId(String objectiveId);

  /**
   * count incomplete KRs of an objective
   * @param objectiveId - id of an objective
   * @return quantity of incomplete KRs of an objective
   */
  Integer countIncompleteByObjectiveId(String objectiveId);

  /**
   * find average KRs progress of an objective
   * @param objectiveId - id of an objective
   * @return average progress of KRs of an objective
   */
  Float findAvgProgressByObjectiveId(String objectiveId);
  /**
   * delete all key results of an objective
   * @param objectiveId - id of an objective
   */
  void deleteAllByObjectiveId(String objectiveId);
  /**
   * Delete key result by id
   * @param id - id of key result
   * @param objectiveId - id of the objective that this key result belongs to
   */
  void deleteById(String id, String objectiveId);

  /**
   * find key results by objective id
   * @param objectiveId - id of a objective
   * @return a list of key results by objective id
   */
  List<KeyResultResponse> findByObjectiveId(String objectiveId);
  /**
   * update a key result
   * @param id - id of a key result
   * @param objectiveId - id of the objective that this key result belongs to
   * @param title - title of key result
   * @param description - description of key result
   * @param startDate - the day that start key result
   * @param endDate - the day that end key result
   * @param progress - progress of key result
   * @param userId - id of user that this key result belongs to
   * @return the updated key result
   */
  KeyResultResponse update(
      String id,
      String objectiveId,
      String title,
      String description,
      Integer startDate,
      Integer endDate,
      Integer progress,
      String userId);

  /**
   * update the progress of a key result by id
   * @param id - id of key result
   * @param userId - id of user that own this key result
   * @param progress - progress of key result
   */
  void updateProgress(String id, String userId, Integer progress);

  /**
   * validate key result exist or not by id
   * @param keyResultId - id of key result
   */
  void validateExist(String keyResultId);

  List<KeyResult> searchByEndDate(Integer date, int page, int size);
}

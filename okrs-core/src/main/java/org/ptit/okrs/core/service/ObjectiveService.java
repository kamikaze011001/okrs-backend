package org.ptit.okrs.core.service;

import java.util.List;
import org.ptit.okrs.core.constant.OkrsTimeType;
import org.ptit.okrs.core.constant.OkrsType;
import org.ptit.okrs.core.entity.Objective;
import org.ptit.okrs.core.model.ObjectiveDetailResponse;
import org.ptit.okrs.core.model.ObjectiveResponse;
import org.ptit.okrs.core.service.base.BaseService;

public interface ObjectiveService extends BaseService<Objective> {

  /**
   * Create new objective
   *
   * @param title - title of objective
   * @param description - description of objective
   * @param startDate - the day that will start the objective
   * @param endDate - the day that will end the objective
   * @param type - type of objective
   * @param timePeriodType - length of time for this objective
   * @param userId - id of the user that own this objective
   * @return the created objective
   */
  ObjectiveResponse create(
      String title,
      String description,
      Integer startDate,
      Integer endDate,
      OkrsType type,
      OkrsTimeType timePeriodType,
      String userId);

  /**
   * Delete objective and all key results of this objective by its id
   *
   * @param id - id of objective
   */
  void deleteById(String id);

  /**
   * get detail of an objective by its id
   * @param id - id of an objective
   * @return the objective and all key results of its
   */
  ObjectiveDetailResponse getByIdAndUserId(String id, String userId);

  /**
   * list all objectives of user
   * @param userId - id of user is logging
   * @return a list of objectives of user is logging
   */
  List<ObjectiveResponse> list(String userId);

  /**
   * update an objective
   * @param id - id of this objective
   * @param title - title of objective
   * @param description - description of objective
   * @param startDate - the day that will start the objective
   * @param endDate - the day that will end the objective
   * @param type - type of objective
   * @param timePeriodType - length of time for this objective
   * @param userId - id of the user that own this objective
   * @return the updated objective
   */
  ObjectiveResponse update(
      String id,
      String title,
      String description,
      Integer startDate,
      Integer endDate,
      OkrsType type,
      OkrsTimeType timePeriodType,
      String userId);

  /**
   *
   * @param id - the id of the objective
   * @param keyResultStartDate - startDate of key result
   * @param keyResultEndDate - endDate of key result
   */
  void validateKeyResultPeriodTime(String id, Integer keyResultStartDate, Integer keyResultEndDate);

  /**
   * validate objective exist or not by id
   * @param objectiveId - id of objective need to check
   */
  void validateExist(String objectiveId);

  List<Objective> searchByEndDate(Integer date, int page, int size);

}

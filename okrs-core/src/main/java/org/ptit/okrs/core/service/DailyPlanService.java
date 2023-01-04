package org.ptit.okrs.core.service;

import java.util.List;
import org.ptit.okrs.core.constant.DailyPlanStatus;
import org.ptit.okrs.core.entity.DailyPlan;
import org.ptit.okrs.core.model.DailyPlanResponse;
import org.ptit.okrs.core.service.base.BaseService;

public interface DailyPlanService extends BaseService<DailyPlan> {
  /**
   * Create new task in daily plan
   * @param title       - title of the task
   * @param description - description of the task
   * @param userId      - id of the current user who logged in our system
   * @param keyResultId - the id of the key-result that the task will be linked.
   * @return task of the daily plan
   */
  DailyPlanResponse create(
      String title,
      String description,
      String userId,
      String keyResultId);

  /**
   * Delete daily plan
   * @param id - id of the daily plan you want to delete
   */
  void delete(String id);

  /**
   * Get all daily plan by key result id
   * @param keyResultId - id of the key result you want to get daily plan
   * @return - information of daily plan that you get from id key result
   */
  List<DailyPlanResponse> getByKeyResultId(String keyResultId, String userId);

  /**
   * get all daily plan
   * @return - all daily plan available in data warehouse
   */
  List<DailyPlanResponse> getByDate(Integer date, String userId);

  /**
   * Link daily plan to key results
   * @param id - the id of the daily plan
   * @param keyResultId - id of the key results want to link
   * @param userId - userId of the task check with id for user
   * @return - The information of the daily plan has been linked
   */
  DailyPlanResponse linkDailyPlanToKeyResults(String id, String keyResultId, String userId);

  /**
   * Update daily plan
   *
   * @param id          - id of the daily plan want to update
   * @param title       - title of the task
   * @param description - description of the task
   * @param note        - note of the task
   * @param keyResultId - keyResultId of the task
   * @param userId      - userId of the task check with id for user
   * @return - information of daily plan after update
   */
  DailyPlanResponse update(
      String id,
      String title,
      String description,
      String note,
      String keyResultId,
      String userId);

  /**
   * Update status daily plan
   * @param id      - id of the dail plan want to update status
   * @param status  - status of the task
   */
  void updateStatusDailyPlan(String id, DailyPlanStatus status);

}

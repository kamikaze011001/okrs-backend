package org.ptit.okrs.core.service.impl;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core.constant.OkrsTimeType;
import org.ptit.okrs.core.constant.OkrsType;
import org.ptit.okrs.core.entity.Objective;
import org.ptit.okrs.core.exception.OkrsKeyResultPeriodTimeException;
import org.ptit.okrs.core.model.ObjectiveDetailResponse;
import org.ptit.okrs.core.model.ObjectiveResponse;
import org.ptit.okrs.core.repository.ObjectiveRepository;
import org.ptit.okrs.core.service.CacheObjectiveService;
import org.ptit.okrs.core.service.KeyResultService;
import org.ptit.okrs.core.service.ObjectiveService;
import org.ptit.okrs.core.service.base.impl.BaseServiceImpl;
import org.ptit.okrs.core_exception.NotFoundException;
import org.ptit.okrs.core_util.ValidationUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public class ObjectiveServiceImpl extends BaseServiceImpl<Objective> implements ObjectiveService {

  private final ObjectiveRepository repository;
  private final KeyResultService keyResultService;
  private final CacheObjectiveService cacheObjectiveService;

  public ObjectiveServiceImpl(ObjectiveRepository repository, KeyResultService keyResultService,
      CacheObjectiveService cacheObjectiveService) {
    super(repository);
    this.repository = repository;
    this.keyResultService = keyResultService;
    this.cacheObjectiveService = cacheObjectiveService;
  }

  @Override
  @Transactional
  public ObjectiveResponse create(
      String title,
      String description,
      Integer startDate,
      Integer endDate,
      OkrsType type,
      OkrsTimeType timePeriodType,
      String userId) {
    log.info(
        "(create)title : {}, type : {}, timePeriodType : {}, userId : {}",
        title,
        type,
        timePeriodType,
        userId);

    return ObjectiveResponse.from(
        create(Objective.of(title, description, startDate, endDate, type, timePeriodType, userId)));
  }

  @Override
  @Transactional
  public void deleteById(String id) {
    log.info("(deleteById)id: {}", id);
    if (!isExist(id)) {
      log.error("(deleteById)id: {} not found", id);
      throw new NotFoundException(id, Objective.class.getSimpleName());
    }
    keyResultService.deleteAllByObjectiveId(id);
    delete(id);
    cacheObjectiveService.delete(id);
  }

  @Override
  @Transactional(readOnly = true)
  public ObjectiveDetailResponse getByIdAndUserId(String id, String userId) {
    log.debug("(getById)id : {}, userId : {}", id, userId);
    var objective =
        repository
            .findByIdAndUserId(id, userId)
            .orElseGet(
                () -> {
                  log.error("(getByIdAndUserId)id : {}, userId : {} --> not found", id, userId);
                  throw new NotFoundException(id, Objective.class.getSimpleName());
                });
    cacheObjectiveService.set(id, objective);
    return ObjectiveDetailResponse.from(objective, keyResultService.findByObjectiveId(id));
  }

  @Override
  @Transactional(readOnly = true)
  public List<ObjectiveResponse> list(String userId) {
    log.info("(list)userId : {}", userId);
    return repository.findByUserId(userId).stream()
        .map(ObjectiveResponse::from)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public ObjectiveResponse update(
      String id,
      String title,
      String description,
      Integer startDate,
      Integer endDate,
      OkrsType type,
      OkrsTimeType timePeriodType,
      String userId) {
    log.info("(update)id : {}, title : {}, userId : {}", id, title, userId);

    var objective = find(id);
    if (Objects.isNull(objective)) {
      log.error("(update)id : {} --> NOT FOUND EXCEPTION", id);
      throw new NotFoundException(id, Objective.class.getSimpleName());
    }
    ValidationUtils.validateForbiddenUser(userId);
    objective.setTitle(title);
    objective.setDescription(description);
    objective.setStartDate(startDate);
    objective.setEndDate(endDate);
    objective.setType(type);
    objective.setTimePeriodType(timePeriodType);

    var objectiveCache = cacheObjectiveService.get(id);
    if (Objects.nonNull(objectiveCache)) {
        cacheObjectiveService.set(id, objective);
    }
    return ObjectiveResponse.from(update(objective));
  }

  @Override
  @Transactional(readOnly = true)
  public void validateKeyResultPeriodTime(
      String id, Integer keyResultStartDate, Integer keyResultEndDate) {
    log.info(
        "(validateKeyResultPeriodTime)id : {}, keyResultStartDate : {}, keyResultEndDate : {}",
        id,
        keyResultStartDate,
        keyResultEndDate);


    var objective = find(id);
    if (Objects.isNull(objective)) {
      log.error("(validateKeyResultPeriodTime)objectiveId : {} --> NOT FOUND EXCEPTION", id);
      throw new NotFoundException(id, Objective.class.getSimpleName());
    }
    var objectiveStartDate = objective.getStartDate();
    var objectiveEndDate = objective.getEndDate();

    if (keyResultStartDate < objectiveStartDate
        || keyResultStartDate > objectiveEndDate
        || keyResultEndDate < objectiveStartDate
        || keyResultEndDate > objectiveEndDate) {
      log.error(
          "(validateKeyResultPeriodTime)keyResultStartDate : {}, keyResultStartDate : {}, id : {} --> OkrsKeyResultPeriodTimeException",
          keyResultStartDate,
          keyResultEndDate,
          id);
      throw new OkrsKeyResultPeriodTimeException(
          String.valueOf(keyResultStartDate), String.valueOf(keyResultEndDate), id);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public void validateExist(String objectiveId) {
    log.info("(validateExist)objectiveId : {}", objectiveId);
    if (!isExist(objectiveId)) {
      log.error("(validateExist)objectiveId : {} --> NOT FOUND EXCEPTION", objectiveId);
      throw new NotFoundException(objectiveId, Objective.class.getSimpleName());
    }
  }

  @Override
  public List<Objective> searchByEndDate(Integer date, int page, int size) {
    log.debug("(searchByEndDate)date: {}, page: {}, size: {}", date, page, size);
    return repository.findByEndDate(date, PageRequest.of(page, size)).getContent();
  }
}

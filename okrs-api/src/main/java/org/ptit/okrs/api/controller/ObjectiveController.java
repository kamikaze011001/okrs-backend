package org.ptit.okrs.api.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.api.constant.OkrsApiConstant.BaseUrl;
import org.ptit.okrs.api.model.request.*;
import org.ptit.okrs.api.model.response.OkrsResponse;
import org.ptit.okrs.core.facade.OkrsFacadeService;
import org.ptit.okrs.core.service.KeyResultService;
import org.ptit.okrs.core.service.ObjectiveService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.ptit.okrs.api.constant.OkrsApiConstant.ResourceConstant.KEY_RESULT;
import static org.ptit.orks.core_audit.SecurityService.getUserId;

@RestController
@RequestMapping(BaseUrl.OBJECTIVE_BASE_URL)
@RequiredArgsConstructor
@Slf4j
public class ObjectiveController {

  private final ObjectiveService service;
  private final KeyResultService keyResultService;
  private final OkrsFacadeService okrsFacadeService;

  @ApiOperation("Create new objective")
  @ApiResponse(code = 201, response = OkrsResponse.class, message = "Successfully response.")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public OkrsResponse create(@Validated @RequestBody ObjectiveCreateRequest request) {
    log.info("(create)request : {}", request);
    request.validate();
    return OkrsResponse.of(
        HttpStatus.CREATED.value(),
        service.create(
            request.getTitle(),
            request.getDescription(),
            request.getStartDate(),
            request.getEndDate(),
            request.getType(),
            request.getTimePeriodType(),
            getUserId()));
  }

  @ApiOperation("Delete an objective")
  @ApiResponse(code = 200, message = "Successfully response.")
  @DeleteMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  public OkrsResponse deleteById(@PathVariable("id") String id) {
    log.info("(deleteById)id : {}", id);
    service.deleteById(id);
    return OkrsResponse.of(HttpStatus.OK.value());
  }

  @ApiOperation("Get a detail objective")
  @ApiResponse(code = 200, response = OkrsResponse.class, message = "Successfully response.")
  @GetMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  public OkrsResponse getById(@PathVariable("id") String id) {
    log.info("(getById)id : {}", id);
    return OkrsResponse.of(HttpStatus.OK.value(), service.getByIdAndUserId(id, getUserId()));
  }

  @ApiOperation("Get list of all objectives")
  @ApiResponse(code = 200, response = OkrsResponse.class, message = "Successfully response.")
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public OkrsResponse list() {
    log.info("(list)");
    return OkrsResponse.of(HttpStatus.OK.value(), service.list(getUserId()));
  }

  @ApiOperation("Get stats of an objective by id")
  @ApiResponse(code = 200, response = OkrsResponse.class, message = "Successfully response.")
  @GetMapping("/{id}/stats")
  @ResponseStatus(HttpStatus.OK)
  public OkrsResponse statistic(@PathVariable("id")String id) {
    log.info("(statistic)");
    return OkrsResponse.of(HttpStatus.OK.value(), okrsFacadeService.statistic(id));
  }

  @ApiOperation("Update an objective")
  @ApiResponse(code = 200, response = OkrsResponse.class, message = "Successfully response.")
  @PutMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  public OkrsResponse update(
      @PathVariable("id") String id, @Validated @RequestBody ObjectiveUpdateRequest request) {
    log.info("(update)id : {}, request : {}", id, request);
    request.validate(id);
    return OkrsResponse.of(
        HttpStatus.OK.value(),
        service.update(
            request.getId(),
            request.getTitle(),
            request.getDescription(),
            request.getStartDate(),
            request.getEndDate(),
            request.getType(),
            request.getTimePeriodType(),
            getUserId()));
  }

  @ApiOperation("Create new key result")
  @ApiResponse(code = 201, response = OkrsResponse.class, message = "Successfully response.")
  @PostMapping(value = "/{objective_id}/" + KEY_RESULT)
  @ResponseStatus(HttpStatus.CREATED)
  public OkrsResponse createKeyResult(
      @PathVariable("objective_id") String objectiveId,
      @Validated @RequestBody KeyResultCreateRequest request) {
    log.info("(createKeyResult)objectiveId : {}, request : {}", objectiveId, request);
    request.validate(objectiveId);
    service.validateExist(request.getObjectiveId());
    service.validateKeyResultPeriodTime(
        request.getObjectiveId(), request.getStartDate(), request.getEndDate());
    return OkrsResponse.of(
        HttpStatus.CREATED.value(),
        keyResultService.create(
            request.getObjectiveId(),
            request.getTitle(),
            request.getDescription(),
            request.getStartDate(),
            request.getEndDate(),
            request.getProgress(),
            getUserId()));
  }

  @ApiOperation("Delete a key result")
  @ApiResponse(code = 200, message = "Successfully response.")
  @DeleteMapping(value = "/{objective_id}/" + KEY_RESULT + "/{key_result_id}")
  @ResponseStatus(HttpStatus.OK)
  public OkrsResponse deleteKeyResultById(
      @PathVariable("objective_id") String objectiveId,
      @PathVariable("key_result_id") String keyResultId
  ) {
    log.info("(deleteById)objectiveId : {}, keyResultId : {}", objectiveId, keyResultId);
    service.validateExist(objectiveId);
    keyResultService.deleteById(keyResultId, objectiveId);
    return OkrsResponse.of(HttpStatus.OK.value());
  }

  @ApiOperation("Update a key result")
  @ApiResponse(code = 200, response = OkrsResponse.class, message = "Successfully response.")
  @PutMapping(value = "/{objective_id}/" + KEY_RESULT + "/{key_result_id}")
  @ResponseStatus(HttpStatus.OK)
  public OkrsResponse updateKeyResult(
      @PathVariable("objective_id") String objectiveId,
      @PathVariable("key_result_id") String keyResultId,
      @Validated @RequestBody KeyResultUpdateRequest request
  ) {
    log.info("(update)objectiveId : {}, keyResultId : {}, request : {}",
        objectiveId, keyResultId, request);
    request.validate(keyResultId, objectiveId);
    service.validateExist(request.getObjectiveId());
    service.validateKeyResultPeriodTime(
        request.getObjectiveId(), request.getStartDate(), request.getEndDate());
    return OkrsResponse.of(
        HttpStatus.OK.value(),
        keyResultService.update(
            request.getId(),
            request.getObjectiveId(),
            request.getTitle(),
            request.getDescription(),
            request.getStartDate(),
            request.getEndDate(),
            request.getProgress(),
            getUserId()));
  }

  @ApiOperation("Update the progress of a key result")
  @ApiResponse(code = 200, response = OkrsResponse.class, message = "Successfully response.")
  @PatchMapping(value = "/{objective_id}/" + KEY_RESULT + "/{key_result_id}")
  @ResponseStatus(HttpStatus.OK)
  public OkrsResponse updateKeyResultProgress(
      @PathVariable("objective_id") String objectiveId,
      @PathVariable("key_result_id") String keyResultId,
      @Validated @RequestBody KeyResultUpdateProgressRequest request
  ) {
    log.info(
        "(updateProgress)objectiveId : {}, keyResultId : {}, request : {}",
        objectiveId, keyResultId, request);
    request.validate(keyResultId);
    service.validateExist(objectiveId);
    keyResultService.updateProgress(keyResultId, getUserId(), request.getProgress());
    return OkrsResponse.of(HttpStatus.OK.value());
  }
}

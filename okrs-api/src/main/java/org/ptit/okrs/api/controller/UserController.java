package org.ptit.okrs.api.controller;

import static org.ptit.okrs.api.constant.OkrsApiConstant.BaseUrl.USER_BASE_URL;
import static org.ptit.orks.core_audit.SecurityService.getUserId;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.api.model.request.UserCreateRequest;
import org.ptit.okrs.api.model.request.UserUpdateAvatarRequest;
import org.ptit.okrs.api.model.request.UserUpdateRequest;
import org.ptit.okrs.api.model.response.OkrsResponse;
import org.ptit.okrs.core.service.UserService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping(USER_BASE_URL)
@RequiredArgsConstructor
@RestController
@Slf4j
public class UserController {
  private final UserService service;

  @ApiOperation("Create a new user")
  @ApiResponse(code = 201, response = OkrsResponse.class, message = "Successfully response.")
  @PostMapping(consumes = "application/json")
  @ResponseStatus(HttpStatus.CREATED)
  public OkrsResponse create(@RequestBody @Validated UserCreateRequest userCreateRequest) {
    log.info("(create)userCreateRequest: {}", userCreateRequest);
    return OkrsResponse.of(
        HttpStatus.CREATED.value(),
        service.create(
            userCreateRequest.getName(),
            userCreateRequest.getEmail()));
  }

  @ApiOperation("Update a user")
  @ApiResponse(code = 200, response = OkrsResponse.class, message = "Successfully response.")
  @PutMapping(consumes = "application/json")
  @ResponseStatus(HttpStatus.OK)
  public OkrsResponse update(@RequestBody @Validated UserUpdateRequest userUpdateRequest) {
    log.info("(update)userUpdateRequest: {}", userUpdateRequest);
    return OkrsResponse.of(
        HttpStatus.OK.value(),
        service.update(
            getUserId(),
            userUpdateRequest.getName(),
            userUpdateRequest.getPhone(),
            userUpdateRequest.getDateOfBirth(),
            userUpdateRequest.getGender(),
            userUpdateRequest.getAddress()));
  }

  @ApiOperation("Get details a user")
  @ApiResponse(code = 200, response = OkrsResponse.class, message = "Successfully response.")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping("/self")
  public OkrsResponse get() {
    log.debug("(get)userId: {}", getUserId());
    return OkrsResponse.of(HttpStatus.OK.value(), service.get(getUserId()));
  }

  @ApiOperation("Update a user's avatar")
  @ApiResponse(code = 200, response = OkrsResponse.class, message = "Successfully response.")
  @PatchMapping("/self/avatar")
  @ResponseStatus(HttpStatus.OK)
  public OkrsResponse updateAvatar(@RequestBody @Validated UserUpdateAvatarRequest userUpdateAvatarRequest) {
    log.info("(updateAvatar)userUpdateAvatarRequest: {}", userUpdateAvatarRequest);
    service.changePathAvatar(getUserId(), userUpdateAvatarRequest.getPathAvatar());
    return OkrsResponse.of(HttpStatus.OK.value());
  }

  @ApiOperation("Get a user's avatar")
  @ApiResponse(code = 200, response = OkrsResponse.class, message = "Successfully response.")
  @ResponseStatus(HttpStatus.OK)
  @GetMapping(value = "/self/avatar", produces = MediaType.IMAGE_JPEG_VALUE)
  public InputStreamResource getAvatar() {
    log.debug("(getAvatar)userId: {}", getUserId());
    return service.getAvatar(getUserId());
  }
}

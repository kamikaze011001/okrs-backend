package org.ptit.okrs.core_authentication.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import java.util.Locale;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core_authentication.dto.request.*;
import org.ptit.okrs.core_authentication.dto.response.AuthResponse;
import org.ptit.okrs.core_authentication.dto.response.AuthUserForgotPasswordOtpVerifyResponse;
import org.ptit.okrs.core_authentication.dto.response.AuthUserLoginResponse;
import org.ptit.okrs.core_authentication.dto.response.AuthUserRegisterResponse;
import org.ptit.okrs.core_authentication.facade.AuthFacadeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/auth/users")
@RestController
@Slf4j
public class AuthUserController {

  private final AuthFacadeService authFacadeService;

  public AuthUserController(AuthFacadeService authFacadeService) {
    this.authFacadeService = authFacadeService;
  }

  @ApiOperation("Step 2: User active account by otp")
  @ApiResponse(code = 200, message = "Successfully response.")
  @PostMapping("/active")
  @ResponseStatus(HttpStatus.OK)
  public AuthResponse activeAccount(@Valid @RequestBody AuthUserActiveAccountRequest request) {
    log.info("(activeAccount)request: {}", request);
    authFacadeService.activeAccount(request);
    return AuthResponse.of(HttpStatus.OK.value());
  }

  @ApiOperation("Step 1: Create new user for sign up account")
  @ApiResponse(code = 201, message = "Successfully response.")
  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public AuthResponse register(@Valid @RequestBody AuthUserRegisterRequest request) {
    log.info("(createUser)request: {}", request);
    return AuthResponse.of(HttpStatus.CREATED.value(), authFacadeService.register(request));
  }

  @ApiOperation("User login")
  @ApiResponse(code = 200, message = "Successfully response.")
  @PostMapping("/login")
  @ResponseStatus(HttpStatus.OK)
  public AuthResponse login(
      @Valid @RequestBody AuthUserLoginRequest request,
      @RequestHeader(value = "Accept-Language", required = false) Locale locale) {
    log.info("(login)request: {}, locale : {}", request, locale);
    return AuthResponse.of(HttpStatus.OK.value(), authFacadeService.login(request, locale));
  }

  @ApiOperation("User request forgot password")
  @ApiResponse(code = 200, message = "Successfully response.")
  @PostMapping("/forgot-password")
  @ResponseStatus(HttpStatus.OK)
  public AuthResponse forgotPassword(@Valid @RequestBody AuthUserResetPasswordRequest request) {
    log.info("(forgotPassword)request: {}", request);
    authFacadeService.forgotPassword(request);
    return AuthResponse.of(HttpStatus.OK.value());
  }

  @ApiOperation("User verify otp forgot password")
  @ApiResponse(code = 200, message = "Successfully response.")
  @PostMapping("/forgot-password/otp-verify")
  @ResponseStatus(HttpStatus.OK)
  public AuthResponse verifyOtpForgotPassword(
      @Valid @RequestBody AuthUserForgotPasswordOtpVerifyRequest request) {
    log.info("(verifyOtpForgotPassword)request: {}", request);
    return AuthResponse.of(HttpStatus.OK.value(), authFacadeService.verifyOtpForgotPassword(request));
  }

  @ApiOperation("User reset password")
  @ApiResponse(code = 200, message = "Successfully response.")
  @PostMapping("/forgot-password/reset")
  @ResponseStatus(HttpStatus.OK)
  public AuthResponse resetPassword(@Valid @RequestBody AuthUserForgotPasswordResetRequest request) {
    log.info("(resetPassword)request: {}", request);
    authFacadeService.resetPassword(request);
    return AuthResponse.of(HttpStatus.OK.value());
  }

  @ApiOperation("Send otp to mail unlock account")
  @ApiResponse(code = 200, message = "Successfully response.")
  @PostMapping("/unlock-account")
  @ResponseStatus(HttpStatus.OK)
  public AuthResponse unlockAccount(@Valid @RequestBody AuthUserSentOtpToMail request) {
    log.info("(sendOTPToMailUnlockAccount)request: {}", request);
    authFacadeService.unlockAccount(request);
    return AuthResponse.of(HttpStatus.OK.value());
  }

  @ApiOperation("User unlock account")
  @ApiResponse(code = 200, message = "Successfully response.")
  @PostMapping("/unlock-account/otp-verify")
  @ResponseStatus(HttpStatus.OK)
  public AuthResponse verifyOtpUnlockAccount(@Valid @RequestBody AuthUnlockAccountRequest request) {
    log.info("(verifyOtpUnlockAccount)request: {}", request);
    authFacadeService.verifyOtpUnlockAccount(request);
    return AuthResponse.of(HttpStatus.OK.value());
  }
}

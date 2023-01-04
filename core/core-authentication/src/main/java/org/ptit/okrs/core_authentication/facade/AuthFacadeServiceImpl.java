package org.ptit.okrs.core_authentication.facade;

import static org.ptit.okrs.core_authentication.constant.CacheConstant.CacheToken.KEY_CACHE_ACCESS_TOKEN;
import static org.ptit.okrs.core_authentication.constant.CacheConstant.CacheToken.KEY_CACHE_REFRESH_TOKEN;
import static org.ptit.okrs.core_authentication.constant.PropertiesConstant.INACTIVE_ACCOUNT_MESSAGE_CODE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core_authentication.constant.CacheConstant.CacheResetPassword;
import org.ptit.okrs.core_authentication.constant.CacheConstant.CacheVerifyOtpForgotPassword;
import org.ptit.okrs.core_authentication.constant.MailConstant;
import org.ptit.okrs.core_authentication.constant.MailConstant.MailForgotPassword;
import org.ptit.okrs.core_authentication.constant.MailConstant.MailRegister;
import org.ptit.okrs.core_authentication.constant.MailConstant.MailUnlockAccount;
import org.ptit.okrs.core_authentication.dto.request.AuthUnlockAccountRequest;
import org.ptit.okrs.core_authentication.dto.request.AuthUserActiveAccountRequest;
import org.ptit.okrs.core_authentication.dto.request.AuthUserForgotPasswordOtpVerifyRequest;
import org.ptit.okrs.core_authentication.dto.request.AuthUserForgotPasswordResetRequest;
import org.ptit.okrs.core_authentication.dto.request.AuthUserLoginRequest;
import org.ptit.okrs.core_authentication.dto.request.AuthUserRegisterRequest;
import org.ptit.okrs.core_authentication.dto.request.AuthUserResetPasswordRequest;
import org.ptit.okrs.core_authentication.dto.request.AuthUserSentOtpToMail;
import org.ptit.okrs.core_authentication.dto.response.AuthActiveUserResponse;
import org.ptit.okrs.core_authentication.dto.response.AuthInactiveUserResponse;
import org.ptit.okrs.core_authentication.dto.response.AuthUserForgotPasswordOtpVerifyResponse;
import org.ptit.okrs.core_authentication.dto.response.AuthUserLoginResponse;
import org.ptit.okrs.core_authentication.dto.response.AuthUserRegisterResponse;
import org.ptit.okrs.core_authentication.exception.OtpNotFoundException;
import org.ptit.okrs.core_authentication.exception.PasswordConfirmNotMatchException;
import org.ptit.okrs.core_authentication.exception.PasswordInvalidException;
import org.ptit.okrs.core_authentication.exception.ResetKeyInvalidException;
import org.ptit.okrs.core_authentication.service.AuthAccountService;
import org.ptit.okrs.core_authentication.service.AuthTokenService;
import org.ptit.okrs.core_authentication.service.AuthUserService;
import org.ptit.okrs.core_authentication.service.LoginFailService;
import org.ptit.okrs.core_authentication.service.MessageService;
import org.ptit.okrs.core_authentication.service.OtpService;
import org.ptit.okrs.core_authentication.service.ResetKeyService;
import org.ptit.okrs.core_authentication.service.TokenRedisService;
import org.ptit.okrs.core_authentication.util.CryptUtil;
import org.ptit.okrs.core_email.service.EmailService;
import org.ptit.okrs.core_util.GeneratorUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public class AuthFacadeServiceImpl implements AuthFacadeService {

  private final AuthAccountService authAccountService;
  private final AuthUserService authUserService;
  private final AuthTokenService authTokenService;
  private final OtpService otpService;
  private final TokenRedisService tokenRedisService;
  private final EmailService emailService;
  private final Long accessTokenLifeTime;
  private final Long refreshTokenLifeTime;
  private final ResetKeyService resetKeyService;
  private final PasswordEncoder passwordEncoder;
  private final MessageService messageService;

  private final LoginFailService loginFailService;

  @Value("${application.authentication.redis.otp_time_out}")
  private Integer otpTimeLife;

  public AuthFacadeServiceImpl(
      AuthAccountService authAccountService,
      AuthUserService authUserService,
      AuthTokenService authTokenService,
      OtpService otpService,
      TokenRedisService tokenRedisService,
      Long accessTokenLifeTime,
      Long refreshTokenLifeTime,
      EmailService emailService,
      ResetKeyService resetKeyService,
      PasswordEncoder passwordEncoder,
      MessageService messageService,
      LoginFailService loginFailService) {
    this.authAccountService = authAccountService;
    this.authUserService = authUserService;
    this.authTokenService = authTokenService;
    this.otpService = otpService;
    this.tokenRedisService = tokenRedisService;
    this.emailService = emailService;
    this.accessTokenLifeTime = accessTokenLifeTime;
    this.refreshTokenLifeTime = refreshTokenLifeTime;
    this.resetKeyService = resetKeyService;
    this.passwordEncoder = passwordEncoder;
    this.messageService = messageService;
    this.loginFailService = loginFailService;
  }

  @Override
  public void activeAccount(AuthUserActiveAccountRequest request) {
    log.info("(activeAccount)request: {}", request);
    authUserService.validateExistedWithEmail(request.getEmail());
    otpService.checkOtpRedis(request.getEmail(), request.getOtp());
  }

  @Override
  public void authenticate(String username, String userId) {
    var user = authUserService.findById(userId);
    var usernamePasswordAuthToken =
        new UsernamePasswordAuthenticationToken(username, userId, new ArrayList<>());
    usernamePasswordAuthToken.setDetails(user);
    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthToken);
  }

  @Override
  public AuthUserLoginResponse login(AuthUserLoginRequest request, Locale locale) {
    log.info("(login)request: {}, locale : {}", request, locale);
    var accountUser = authAccountService.findByUsername(request.getUsername());
    if (!accountUser.getIsActivated()) {
      return AuthInactiveUserResponse.from(
          messageService.getI18nMessage(INACTIVE_ACCOUNT_MESSAGE_CODE, locale, null));
    }

    loginFailService.checkLock(
        accountUser.getEmail(), accountUser.getUserId(), accountUser.getIsLockPermanent());

    if (!CryptUtil.getPasswordEncoder().matches(request.getPassword(), accountUser.getPassword())) {
      log.error("(login)password : {} --> PasswordInvalidException", request.getPassword());
      loginFailService.increaseFailAttempts(accountUser.getEmail());
      loginFailService.setLock(accountUser.getEmail());
      throw new PasswordInvalidException();
    }
    loginFailService.resetFailAttempts(accountUser.getEmail());
    String accessToken =
        authTokenService.generateAccessToken(
            accountUser.getUserId(), accountUser.getEmail(), accountUser.getUsername());
    String refreshToken =
        authTokenService.generateRefreshToken(
            accountUser.getUserId(), accountUser.getEmail(), accountUser.getUsername());
    tokenRedisService.set(KEY_CACHE_ACCESS_TOKEN, accountUser.getUserId(), accessToken);
    tokenRedisService.set(KEY_CACHE_REFRESH_TOKEN, accountUser.getUserId(), refreshToken);
    authenticate(accountUser.getUsername(), accountUser.getUserId());
    return AuthActiveUserResponse.from(
        accessToken, refreshToken, accessTokenLifeTime, refreshTokenLifeTime);
  }

  @Override
  public AuthUserRegisterResponse register(AuthUserRegisterRequest request) {
    log.info("(register)request: {}", request);

    request.validate();

    // create account
    var authUser = authUserService.create(request.getEmail());
    var authAccount =
        authAccountService.create(
            authUser.getId(),
            request.getUsername(),
            CryptUtil.getPasswordEncoder().encode(request.getPassword()));

    // generate otp and push it to redis
    var otpActiveAccount = GeneratorUtils.generateOtp();
    otpService.set(authUser.getEmail(), otpActiveAccount);
    // Send mail request active account
    sendMailOTPTemplate(
        request.getEmail(),
        otpActiveAccount,
        MailRegister.KEY_PARAM_OTP_TIME_LIFE,
        MailRegister.KEY_PARAM_OTP,
        MailRegister.SUBJECT);

    return AuthUserRegisterResponse.of(
        authUser.getId(),
        authUser.getEmail(),
        authAccount.getId(),
        authAccount.getUsername(),
        authAccount.getIsActivated());
  }

  @Override
  public void forgotPassword(AuthUserResetPasswordRequest request) {
    log.info("(forgotPassword)request: {}", request);
    // Step 1: validate exist identifier
    authUserService.validateExistedWithEmail(request.getEmail());

    // Step 2: if identifier found -> generate otp, push redis and send email verify
    // generate otp and push it to redis
    var otpForgotPassword = GeneratorUtils.generateOtp();
    otpService.set(request.getEmail(), otpForgotPassword);
    // send mail verify
    sendMailOTPTemplate(
        request.getEmail(),
        otpForgotPassword,
        MailForgotPassword.KEY_PARAM_OTP_TIME_LIFE,
        MailForgotPassword.KEY_PARAM_OTP,
        MailForgotPassword.SUBJECT);
  }

  @Override
  public AuthUserForgotPasswordOtpVerifyResponse verifyOtpForgotPassword(
      AuthUserForgotPasswordOtpVerifyRequest request) {
    log.info("(verifyOtpForgotPassword)request: {}", request);

    // chek email exist
    authUserService.validateExistedWithEmail(request.getEmail());

    // verify otp
    otpService.validateOtp(request.getEmail(), request.getOtp());

    // generate reset password key, push redis(key: email, value: resetKey), return to client
    var resetPasswordKey = CryptUtil.generateResetKey(request.getEmail());
    resetKeyService.set(
        CacheVerifyOtpForgotPassword.KEY_CACHE_RESET_PASSWORD,
        request.getEmail(),
        resetPasswordKey);

    return AuthUserForgotPasswordOtpVerifyResponse.of(resetPasswordKey);
  }

  @Override
  public void resetPassword(AuthUserForgotPasswordResetRequest request) {
    log.info("(resetPassword)request: {}", request);

    validatePassword(request.getNewPassword(), request.getNewPasswordConfirm());

    // validate email not found
    authUserService.validateExistedWithEmail(request.getEmail());

    // validate resetKey
    validateResetKey(request);

    // update new password
    authAccountService.updatePasswordByEmail(
        request.getEmail(), passwordEncoder.encode(request.getNewPassword()));
  }

  private void sendMailOTPTemplate(
      String email, String otp, String keyParamTimeLife, String keyParamOtp, String subject) {
    log.info(
        "(sendMailOTPTemplate)email: {}, otp: {}, keyParamTimeLife: {}, keyParamOtp: {}, subject: {}",
        email,
        otp,
        keyParamTimeLife,
        keyParamOtp,
        subject);
    var params = new HashMap<String, Object>();
    params.put(keyParamTimeLife, otpTimeLife);
    params.put(keyParamOtp, otp);
    emailService.send(subject, email, MailConstant.OTP_TEMPLATE_NAME, params);
  }

  @Override
  public void unlockAccount(AuthUserSentOtpToMail request) {
    log.info("(sentOtpToMailUnlockAccount)request: {}", request);
    authUserService.validateExistedWithEmail(request.getEmail());

    var otpUnlockAccount = GeneratorUtils.generateOtp();
    otpService.set(request.getEmail(), otpUnlockAccount);
    sendMailOTPTemplate(
        request.getEmail(),
        otpUnlockAccount,
        MailUnlockAccount.KEY_PARAM_OTP_TIME_LIFE,
        MailUnlockAccount.KEY_PARAM_OTP,
        MailUnlockAccount.SUBJECT);
  }

  @Override
  public void verifyOtpUnlockAccount(AuthUnlockAccountRequest request) {
    log.info("(verifyOtpUnlockAccount)request: {}", request);

    authUserService.validateExistedWithEmail(request.getEmail());

    otpService.validateOtp(request.getEmail(), request.getOtp());

    authAccountService.disableLockPermanent(request.getEmail());
    loginFailService.resetFailAttempts(request.getEmail());
  }

  private void validateResetKey(AuthUserForgotPasswordResetRequest request) {
    if (!Objects.equals(
        resetKeyService.get(CacheResetPassword.KEY_CACHE_RESET_PASSWORD, request.getEmail()),
        request.getResetPasswordKey())) {
      log.error("(resetPassword)resetKey: {} invalid", request.getResetPasswordKey());
      throw new ResetKeyInvalidException();
    }
  }

  private void validatePassword(String newPassword, String newPasswordConfirm) {
    if (!Objects.equals(newPassword, newPasswordConfirm)) {
      log.error(
          "(resetPassword)newPassword: {}, newPasswordConfirm:{}  don't match",
          newPassword,
          newPasswordConfirm);
      throw new PasswordConfirmNotMatchException();
    }
  }
}

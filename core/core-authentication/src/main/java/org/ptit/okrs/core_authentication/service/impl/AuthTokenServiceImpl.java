package org.ptit.okrs.core_authentication.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core_authentication.service.AuthTokenService;

@Slf4j
public class AuthTokenServiceImpl implements AuthTokenService {

  private final String accessTokenJwtSecret;
  private final Long accessTokenLifeTime;

  private final String refreshTokenJwtSecret;
  private final Long refreshTokenLifeTime;

  public AuthTokenServiceImpl(
      String accessTokenJwtSecret,
      Long accessTokenLifeTime,
      String refreshTokenJwtSecret,
      Long refreshTokenLifeTime) {
    this.accessTokenJwtSecret = accessTokenJwtSecret;
    this.accessTokenLifeTime = accessTokenLifeTime;
    this.refreshTokenJwtSecret = refreshTokenJwtSecret;
    this.refreshTokenLifeTime = refreshTokenLifeTime;
  }

  @Override
  public String generateAccessToken(String userId, String email, String username) {
    log.info("(generateAccessToken)userId: {}, email: {}, username: {}", userId, email, username);
    var claims = new HashMap<String, Object>();
    claims.put("email", email);
    claims.put("username", username);
    return generateToken(userId, claims, accessTokenLifeTime, accessTokenJwtSecret);
  }

  @Override
  public String getSubjectFromAccessToken(String accessToken) {
    log.debug("(getSubjectFromAccessToken)accessToken: {}", accessToken);
    return getClaim(accessToken, Claims::getSubject, accessTokenJwtSecret);
  }

  @Override
  public boolean validateAccessToken(String accessToken, String userId) {
    log.debug("(validateAccessToken)accessToken: {}, userId: {}", accessToken, userId);
    return getSubjectFromAccessToken(accessToken).equals(userId)
        && !isExpiredToken(accessToken, accessTokenJwtSecret);
  }

  @Override
  public String generateRefreshToken(String userId, String email, String username) {
    log.info("(generateRefreshToken)userId: {}, email: {}, username: {}", userId, email, username);
    var claims = new HashMap<String, Object>();
    claims.put("email", email);
    claims.put("username", username);
    return generateToken(userId, claims, refreshTokenLifeTime, refreshTokenJwtSecret);
  }

  @Override
  public String getSubjectFromRefreshToken(String refreshToken) {
    log.debug("(getSubjectFromRefreshToken)refreshToken: {}", refreshToken);
    return getClaim(refreshToken, Claims::getSubject, refreshTokenJwtSecret);
  }

  @Override
  public boolean validateRefreshToken(String refreshToken, String userId) {
    log.info("(validateRefreshToken)refreshToken: {}, userId: {}", refreshToken, userId);
    return getSubjectFromRefreshToken(refreshToken).equals(userId)
        && !isExpiredToken(refreshToken, refreshTokenJwtSecret);
  }

  private String generateToken(
      String subject, Map<String, Object> claims, long tokenLifeTime, String jwtSecret) {
    log.info(
        "(generateToken)subject : {}, claims : {}, tokenLifeTime : {}, jwtSecret : {}",
        subject,
        claims,
        tokenLifeTime,
        jwtSecret);
    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + tokenLifeTime))
        .signWith(SignatureAlgorithm.HS256, jwtSecret)
        .compact();
  }

  private boolean isExpiredToken(String token, String secretKey) {
    return getClaim(token, Claims::getExpiration, secretKey).before(new Date());
  }

  private <T> T getClaim(String token, Function<Claims, T> claimsResolve, String secretKey) {
    log.info("(getClaim)token : {}, claimsResolve : {}, secretKey : {}", token, claimsResolve, secretKey);
    return claimsResolve.apply(getClaims(token, secretKey));
  }

  private Claims getClaims(String token, String secretKey) {
    log.info("(getClaims)token : {}, secretKey : {}", token, secretKey);
    return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
  }
}

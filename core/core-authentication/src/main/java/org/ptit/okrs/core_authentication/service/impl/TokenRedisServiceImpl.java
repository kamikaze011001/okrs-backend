package org.ptit.okrs.core_authentication.service.impl;

import org.ptit.okrs.core_authentication.service.TokenRedisService;
import org.ptit.okrs.core_redis.BaseRedisHashServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;

public class TokenRedisServiceImpl extends BaseRedisHashServiceImpl<String> implements
    TokenRedisService {

  public TokenRedisServiceImpl(
      RedisTemplate<String, Object> redisTemplate) {
    super(redisTemplate);
  }
}

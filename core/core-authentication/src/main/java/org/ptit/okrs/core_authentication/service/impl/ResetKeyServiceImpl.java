package org.ptit.okrs.core_authentication.service.impl;

import org.ptit.okrs.core_authentication.service.ResetKeyService;
import org.ptit.okrs.core_redis.BaseRedisHashServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;

public class ResetKeyServiceImpl extends BaseRedisHashServiceImpl<String>
    implements ResetKeyService {

  public ResetKeyServiceImpl(RedisTemplate<String, Object> redisTemplate) {
    super(redisTemplate);
  }
}

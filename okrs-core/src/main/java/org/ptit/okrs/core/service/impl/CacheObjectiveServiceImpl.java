package org.ptit.okrs.core.service.impl;

import java.util.concurrent.TimeUnit;
import org.ptit.okrs.core.entity.Objective;
import org.ptit.okrs.core.service.CacheObjectiveService;
import org.ptit.okrs.core_redis.BaseRedisServiceImpl;
import org.springframework.data.redis.core.RedisTemplate;

public class CacheObjectiveServiceImpl extends BaseRedisServiceImpl<Object> implements CacheObjectiveService {

  public CacheObjectiveServiceImpl(
      RedisTemplate<String, Object> redisTemplate, long timeOut, TimeUnit unitTimeOut) {
    super(redisTemplate, timeOut, unitTimeOut);
  }
  @Override
  protected boolean isSavePersistent() {
    return false;
  }
}

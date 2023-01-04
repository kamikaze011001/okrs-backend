package org.ptit.okrs.core_redis;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

public abstract class BaseRedisServiceImpl<T> implements BaseRedisService<T> {

  private final RedisTemplate<String, Object> redisTemplate;
  private final long timeOut;
  private final TimeUnit unitTimeOut;

  public BaseRedisServiceImpl(RedisTemplate<String, Object> redisTemplate, long timeOut, TimeUnit unitTimeOut) {
    this.redisTemplate = redisTemplate;
    this.timeOut = timeOut;
    this.unitTimeOut = unitTimeOut;
  }

  protected abstract boolean isSavePersistent();

  @Override
  public void delete(String key) {
    redisTemplate.delete(key);
  }

  @Override
  public Object get(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  @Override
  public void set(String key, T object) {
    if (isSavePersistent()) {
      redisTemplate.opsForValue().set(key, object);
    } else {
      redisTemplate.opsForValue().set(key, object, timeOut, unitTimeOut);
    }
  }
}

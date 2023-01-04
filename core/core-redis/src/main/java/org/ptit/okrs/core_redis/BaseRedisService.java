package org.ptit.okrs.core_redis;

public interface BaseRedisService<T> {
  void delete(String key);

  Object get(String key);

  void set(String key, T object);
}

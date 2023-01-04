package org.ptit.okrs.core_redis;

import java.util.Map;

public interface BaseRedisHashService<T> {

  void delete(String key);

  void delete(String key, String field);

  Map<String, Object> get(String key);

  Object get(String key, String field);

  void set(String key, T object);

  void set(String key, String field, Object value);
}


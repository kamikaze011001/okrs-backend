package org.ptit.okrs.core.service.base.impl;

import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core.entity.base.BaseEntity;
import org.ptit.okrs.core.repository.base.BaseRepository;
import org.ptit.okrs.core.service.base.BaseService;

import java.util.List;

@Slf4j
public class BaseServiceImpl<T extends BaseEntity> implements BaseService<T> {

  private final BaseRepository<T> repository;

  public BaseServiceImpl(BaseRepository<T> repository) {
    this.repository = repository;
  }

  @Override
  public T create(T t) {
    log.info("(create)object: {}", t);
    return repository.save(t);
  }

  @Override
  public void delete(String id) {
    log.info("(delete)id: {}", id);
    repository.deleteById(id);
  }

  @Override
  public T find(String id) {
    log.debug("(find)id: {}", id);
    return repository.findById(id).orElse(null);
  }

  @Override
  public boolean isExist(String id) {
    log.debug("(isExist)id: {}", id);
    return repository.existsById(id);
  }

  @Override
  public T update(T t) {
    log.info("(update)object: {}", t);
    return repository.save(t);
  }

  @Override
  public List<T> list() {
    return repository.findAll();
  }
}

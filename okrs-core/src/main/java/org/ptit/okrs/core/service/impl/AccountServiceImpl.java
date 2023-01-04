package org.ptit.okrs.core.service.impl;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core.entity.Account;
import org.ptit.okrs.core.repository.AccountRepository;
import org.ptit.okrs.core.service.AccountService;
import org.ptit.okrs.core.service.base.impl.BaseServiceImpl;
import org.ptit.okrs.core_exception.NotFoundException;

@Slf4j
public class AccountServiceImpl extends BaseServiceImpl<Account> implements AccountService {

  private final AccountRepository repository;

  public AccountServiceImpl(AccountRepository repository) {
    super(repository);
    this.repository = repository;
  }

  @Override
  public void validateAccount(String id) {
    log.info("(validate)id: {}", id);
    if ( !isExist(id)) {
      log.error("(validate)id: {} not found", id);
      throw new NotFoundException(id, Account.class.getSimpleName());
    }
  }

  @Override
  public boolean existsUserIdAndUsername(String userId, String username) {
    log.info("(existsUserIdAndUsername)userId: {}, username: {}", userId, username);
    return repository.existsByUserIdAndUsername(userId, username);
  }
}

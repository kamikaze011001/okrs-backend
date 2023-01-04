package org.ptit.okrs.core.repository;

import org.ptit.okrs.core.entity.Account;
import org.ptit.okrs.core.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends BaseRepository<Account> {
  boolean existsByUserIdAndUsername(String userId, String username);
}

package org.ptit.okrs.core.service;

import org.ptit.okrs.core.entity.Account;
import org.ptit.okrs.core.service.base.BaseService;

public interface AccountService extends BaseService<Account> {

  /**
   * validate id of the new account created
   * @param id - the id of the new account created
   */
  void validateAccount(String  id);

  /**
   * check exists username and userId
   * @param userId the id of user
   * @param username the username of account
   * @return result method check
   */
  boolean existsUserIdAndUsername(String userId, String username);
}

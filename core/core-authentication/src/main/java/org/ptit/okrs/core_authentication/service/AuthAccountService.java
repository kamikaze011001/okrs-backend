package org.ptit.okrs.core_authentication.service;

import org.ptit.okrs.core_authentication.entity.AuthAccount;
import org.ptit.okrs.core_authentication.repository.AccountUserProjection;

public interface AuthAccountService {
  AuthAccount findById(String id);
  AuthAccount findByUserIdWithThrow(String userId);

  /**
   * find info to validate user
   * @param username - username of account
   * @return a project object contain info to validate user
   */
  AccountUserProjection findByUsername(String username);
  AuthAccount create(String userId, String username, String password);

  void activeByEmail(String email);

  /**
   * disable lock permanent of account
   * @param email - email of user
   */
  void disableLockPermanent(String email);

  /**
   * enable lock permanent of account
   * @param email - email of user
   */
  void enableLockPermanent(String email);

  /**
   * update new password for account  by email
   * @param email the id of the user can change password
   * @param password the new password of the account
   */
  void updatePasswordByEmail(String email, String password);
}

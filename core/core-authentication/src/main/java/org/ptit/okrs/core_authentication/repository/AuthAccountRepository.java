package org.ptit.okrs.core_authentication.repository;

import java.util.Optional;
import org.ptit.okrs.core_authentication.entity.AuthAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface AuthAccountRepository extends JpaRepository<AuthAccount, String> {
  Optional<AuthAccount> findFirstByUserId(String userId);

  @Query(
      "select new org.ptit.okrs.core_authentication.repository.AccountUserProjection("
          + "a.id, "
          + "a.username, "
          + "a.password, "
          + "a.userId, "
          + "u.email, "
          + "a.isActivated,"
          + "a.isLockPermanent)"
          + "from AuthAccount a inner join AuthUser u on a.userId = u.id where a.username = :username")
  Optional<AccountUserProjection> find(String username);

  boolean existsByUserId(String userId);

  boolean existsByUsername(String username);

  @Transactional
  @Modifying
  @Query(
      value =
          "UPDATE account SET is_activated = true FROM account a INNER JOIN user_okrs u ON a.user_id = u.id WHERE u.email = :email",
      nativeQuery = true)
  void activeAccountByEmail(@Param("email") String email);

  @Transactional
  @Modifying
  @Query(nativeQuery = true, value = "UPDATE account SET is_lock_permanent = false FROM account a INNER JOIN user_okrs u ON a.user_id = u.id WHERE u.email = :email")
  void disableLockPermanent(@Param("email") String email);

  @Transactional
  @Modifying
  @Query(nativeQuery = true, value = "UPDATE account SET is_lock_permanent = true FROM account a INNER JOIN user_okrs u ON a.user_id = u.id WHERE u.email = :email")
  void enableLockPermanent(@Param("email") String email);

  @Transactional
  @Modifying
  @Query(
      value =
          "UPDATE account SET password = :password FROM account a INNER JOIN user_okrs u ON a.user_id = u.id WHERE u.email = :email",
      nativeQuery = true)
  void updatePasswordByEmail(@Param("email") String email, @Param("password") String password);
}

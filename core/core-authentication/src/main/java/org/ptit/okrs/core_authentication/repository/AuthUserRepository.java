package org.ptit.okrs.core_authentication.repository;

import org.ptit.okrs.core_authentication.entity.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthUserRepository extends JpaRepository<AuthUser, String> {
  boolean existsByEmail(String email);
  Optional<AuthUser> findByEmail(String email);
}

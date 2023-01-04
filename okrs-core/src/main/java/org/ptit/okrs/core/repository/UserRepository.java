package org.ptit.okrs.core.repository;

import org.ptit.okrs.core.entity.User;
import org.ptit.okrs.core.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends BaseRepository<User> {

  boolean existsByEmail(String email);

  @Query("Select u.avatar from User u where u.id = :userId")
  String findAvatar(@Param("userId") String userId);

  @Transactional
  @Modifying
  @Query("UPDATE User SET avatar = :avatar WHERE id = :id")
  void updateAvatar(@Param("id") String userId, @Param("avatar") String pathAvatar);
}

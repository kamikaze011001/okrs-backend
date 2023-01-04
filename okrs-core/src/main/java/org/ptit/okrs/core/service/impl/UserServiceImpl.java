package org.ptit.okrs.core.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core.constant.Gender;
import org.ptit.okrs.core.entity.User;
import org.ptit.okrs.core.exception.AvatarNotFoundException;
import org.ptit.okrs.core.exception.EmailAlreadyExistsException;
import org.ptit.okrs.core.exception.EmailInvalidException;
import org.ptit.okrs.core.exception.PhoneNumberInvalidException;
import org.ptit.okrs.core.model.UserCreateResponse;
import org.ptit.okrs.core.model.UserResponse;
import org.ptit.okrs.core.repository.UserRepository;
import org.ptit.okrs.core.service.UserService;
import org.ptit.okrs.core.service.base.impl.BaseServiceImpl;
import org.ptit.okrs.core_exception.InternalServerError;
import org.ptit.okrs.core_exception.NotFoundException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
public class UserServiceImpl extends BaseServiceImpl<User> implements UserService {
  private final UserRepository repository;

  public UserServiceImpl(UserRepository repository) {
    super(repository);
    this.repository = repository;
  }

  @Override
  @Transactional
  public UserCreateResponse create(String name, String email) {
    log.info("(create)name: {}, email: {}", name, email);
    validateUserExistsByEmail(email);
    return UserCreateResponse.from(create(User.of(name, email)));
  }

  @Override
  @Transactional
  public UserResponse update(
      String id,
      String name,
      String phone,
      Integer dateOfBirth,
      Gender gender,
      String address) {
    log.info(
        "(update)id: {}, name: {}, phone: {}, dateOfBirth: {}, gender: {}, address: {}",
        id,
        name,
        phone,
        dateOfBirth,
        gender,
        address);

    validatePhoneNumber(phone);
    validateUserExists(id);
    return UserResponse.from(update(buildUser( id, name, phone, dateOfBirth, gender, address)));
  }

  @Override
  @Transactional(readOnly = true)
  public UserResponse get(String userId) {
    log.debug("(get)userId: {}", userId);
    validateUserExists(userId);
    return UserResponse.from(find(userId));
  }

  @Override
  @Transactional
  public void changePathAvatar(String userId, String pathAvatar) {
    log.info("(changePathAvatar)userId: {}, pathAvatar: {}", userId, pathAvatar);
    validateUserExists(userId);
    repository.updateAvatar(userId, pathAvatar);
  }

  @Override
  @Transactional(readOnly = true)
  public InputStreamResource getAvatar(String userId) {
    log.info("(getAvatar)userId: {}", userId);

    validateUserExists(userId);

    var pathFile = repository.findAvatar(userId);
    validatePathFileNull(pathFile, userId);

    return retrieveImage(pathFile);
  }

  private User buildUser(String id,
                         String name,
                         String phone,
                         Integer dateOfBirth,
                         Gender gender,
                         String address) {
    log.info("(buildUser)id: {}, name: {}, phone: {}, dateOfBirth: {}, gender: {}, address: {}",
            id,
            name,
            phone,
            dateOfBirth,
            gender,
            address);

    var userUpdate = find(id);
    userUpdate.setName(name);
    userUpdate.setPhone(phone);
    userUpdate.setDateOfBirth(dateOfBirth);
    userUpdate.setGender(gender);
    userUpdate.setAddress(address);
    return update(userUpdate);
  }

  private void validatePhoneNumber(String phoneNumber) {
    log.info("(validatePhoneNumber)");
    var regexPhoneNumber = "(0)(3|5|7|8|9)+([0-9]{8})\\b";
    if (!phoneNumber.matches(regexPhoneNumber)) {
      log.error("(update)phone: {} invalid ", phoneNumber);
      throw new PhoneNumberInvalidException(phoneNumber);
    }
  }

  private void validateUserExists(String id) {
    log.info("(validateUser)id: {}", id);
    if (!isExist(id)) {
      log.error("(validateUser)userId: {} not found", id);
      throw new NotFoundException(id, User.class.getSimpleName());
    }
  }

  private void validatePathFileNull(String pathFile, String userId) {
    log.info("(validatePathFileNull)pathFile: {}", pathFile);
    if (Objects.isNull(pathFile)) {
      log.error("(getAvatar)userId: {} not found", userId);
      throw new AvatarNotFoundException(userId);
    }
  }

  private InputStreamResource retrieveImage(String pathFile) {
    log.info("(retrieveAvatar)pathFile: {}", pathFile);
    try {
      var file = new File(pathFile);
      return new InputStreamResource(new FileInputStream(file));
    } catch (Exception e) {
      log.error("(getAvatar)exception: {}", e.getMessage());
      throw new InternalServerError();
    }
  }

  private void validateUserExistsByEmail(String email) {
    log.info("(validateUserExistsByEmail)email: {}", email);
    if (repository.existsByEmail(email)) {
      log.error("(create)email: {} conflict data", email);
      throw new EmailAlreadyExistsException(email);
    }
  }
}

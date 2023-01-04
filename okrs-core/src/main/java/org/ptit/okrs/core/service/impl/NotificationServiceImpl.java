package org.ptit.okrs.core.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.ptit.okrs.core.entity.Notification;
import org.ptit.okrs.core.model.NotificationResponse;
import org.ptit.okrs.core.repository.NotificationRepository;
import org.ptit.okrs.core.service.NotificationService;
import org.ptit.okrs.core.service.base.impl.BaseServiceImpl;
import org.ptit.okrs.core_exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

@Slf4j
public class NotificationServiceImpl extends BaseServiceImpl<Notification> implements NotificationService {

  private final NotificationRepository repository;

  public NotificationServiceImpl(NotificationRepository repository) {
    super(repository);
    this.repository = repository;
  }

  @Override
  public NotificationResponse create(String content, String userId) {
    log.info("(create)content : {}, userId : {}", content, userId);
    var notification = Notification.of(content, userId);
    notification = create(notification);
    String.format("<b>" + content + "</b>");
    return NotificationResponse.from(notification);
  }

  @Override
  public void deleteById(String id) {
    log.info("(deleteById)id: {}", id);
    if (!isExist(id)) {
      log.error("(deleteById)id: {} not found", id);
      throw new NotFoundException(id, Notification.class.getSimpleName());
    }
    repository.deleteById(id);
  }

  @Override
  public NotificationResponse getById(String id) {
    log.info("(getById)id: {}", id);
    if (!isExist(id)) {
      log.error("(getById)id: {} not found", id);
      throw new NotFoundException(id, Notification.class.getSimpleName());
    }
    return NotificationResponse.from(find(id));

  }

  @Override
  public Page<NotificationResponse> list(String userId, final Pageable pageable) {
    log.info("(list)userId : {}", userId);
    return repository.findByUserId(userId, pageable).map(NotificationResponse::from);
  }

  @Override
  public List<Notification> searchByDateLessThan(long date, int page, int size) {
    log.debug("(searchByDateLessThan)date: {}, page: {}, size: {}", date, page, size);
    return repository.findByCreatedAtLessThan(date, PageRequest.of(page, size)).getContent();
  }

  @Override
  @Async
  public void deleteAll(List<Notification> notifications) {
    log.info("(deleteAll)");
    repository.deleteAll(notifications);
  }

  @Override
  public NotificationResponse update(String id, String content, String userId) {
    log.info("(update)id: {}, content: {}, userId: {}", id, content, userId);
    var notification = find(id);
    if (notification == null) {
      log.error("(update)id: {} not found", id);
      throw new NotFoundException(id, Notification.class.getSimpleName());
    }
    notification.setContent(content);
    notification.setUserId(userId);
    return NotificationResponse.from(update(notification));
  }
}

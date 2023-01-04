package org.ptit.okrs.core.configuration;

import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManagerFactory;

import org.ptit.okrs.core.facade.OkrsFacadeService;
import org.ptit.okrs.core.facade.impl.OkrsFacadeServiceImpl;
import org.ptit.okrs.core.repository.AccountRepository;
import org.ptit.okrs.core.repository.DailyPlanRepository;
import org.ptit.okrs.core.repository.KeyResultRepository;
import org.ptit.okrs.core.repository.NotificationRepository;
import org.ptit.okrs.core.repository.ObjectiveRepository;
import org.ptit.okrs.core.repository.UserRepository;
import org.ptit.okrs.core.service.AccountService;
import org.ptit.okrs.core.service.CacheObjectiveService;
import org.ptit.okrs.core.service.DailyPlanService;
import org.ptit.okrs.core.service.KeyResultService;
import org.ptit.okrs.core.service.NotificationService;
import org.ptit.okrs.core.service.ObjectiveService;
import org.ptit.okrs.core.service.UserService;
import org.ptit.okrs.core.service.impl.AccountServiceImpl;
import org.ptit.okrs.core.service.impl.CacheObjectiveServiceImpl;
import org.ptit.okrs.core.service.impl.DailyPlanServiceImpl;
import org.ptit.okrs.core.service.impl.KeyResultServiceImpl;
import org.ptit.okrs.core.service.impl.NotificationServiceImpl;
import org.ptit.okrs.core.service.impl.ObjectiveServiceImpl;
import org.ptit.okrs.core.service.impl.UserServiceImpl;
import org.ptit.okrs.core_redis.config.EnableCoreRedis;
import org.ptit.orks.core_audit.AuditorAwareImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;

@Configuration
@EnableJpaRepositories(
    basePackages = {"org.ptit.okrs.core.repository"})
@ComponentScan(basePackages = {"org.ptit.okrs.core.repository"})
@EnableJpaAuditing
@EntityScan(basePackages = "org.ptit.okrs.core.entity")
@EnableCoreRedis
public class OkrsCoreConfiguration {

  @Value("${application.authentication.redis.objective_time_out:3}")
  private Integer objectiveTimeLife;

  @Bean
  @Primary
  public AuditorAware<String> auditorAware() {
    return new AuditorAwareImpl();
  }

  @Bean
  public DailyPlanService dailyPlanService(
      DailyPlanRepository repository, UserService userService) {
    return new DailyPlanServiceImpl(repository, userService);
  }

  @Bean
  public ObjectiveService objectiveService(
      ObjectiveRepository repository,
      KeyResultService keyResultService,
      CacheObjectiveService cacheObjectiveService) {
    return new ObjectiveServiceImpl(repository, keyResultService, cacheObjectiveService);
  }

  @Bean
  public OkrsFacadeService okrsFacadeService(
      ObjectiveService objectiveService, KeyResultService keyResultService, DailyPlanService dailyPlanService, NotificationService notificationService) {
    return new OkrsFacadeServiceImpl(objectiveService, keyResultService, dailyPlanService, notificationService);
  }

  @Bean
  public KeyResultService keyResultService(KeyResultRepository repository) {
    return new KeyResultServiceImpl(repository);
  }

  @Bean
  public UserService userService(UserRepository repository) {
    return new UserServiceImpl(repository);
  }

  @Bean
  public AccountService accountService(AccountRepository repository) {
    return new AccountServiceImpl(repository);
  }

  @Bean
  public NotificationService notificationService(NotificationRepository repository) {
    return new NotificationServiceImpl(repository);
  }

  @Bean
  public CacheObjectiveService cacheObjectiveService(RedisTemplate<String, Object> redisTemplate) {
    return new CacheObjectiveServiceImpl(redisTemplate, objectiveTimeLife, TimeUnit.MINUTES);
  }

  @Bean
  @Primary
  public JpaTransactionManager transactionManager(EntityManagerFactory managerFactory) {
    return new JpaTransactionManager(managerFactory);
  }
}

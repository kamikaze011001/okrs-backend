---
tags: [configuration, helm, spring-profiles, retention, silent-failure]
problem_type: bug-pattern
date: 2026-08-14
times_seen: 2
---
# A value present in config is not the same as the behaviour being active

**Symptom:** Two independent instances in one branch.

1. `logback-spring.xml` gated JSON logging behind `<springProfile name="dev,qa">`, and the profile
   was correct. But `deploy/charts/okrs-backend/values/dev.yaml` never set `env.springProfile`, so
   the container inherited `springProfile: default` from the chart. Rendering showed
   `SPRING_PROFILES_ACTIVE=default`: JSON logging never activated, the log collector parsed nothing,
   and every metric was tagged `environment="default"` — while the docs asserted the opposite.
2. The Loki Application set `limits_config.retention_period: 24h`, but Loki only enforces retention
   when a `compactor` block sets `retention_enabled: true`. Retention was inert; the store would
   grow until the `emptyDir` `sizeLimit` evicted the pod and destroyed every log.

**Root cause:** Both halves of each pair looked correct in isolation. A profile-gated feature reads
as done once the gate is written, and a limit reads as done once the number is set. Nobody owns the
question "does the deployed artifact actually put this into effect?", because that question lives
between files rather than inside any one of them.

**Rule:** When a behaviour is gated on a runtime value (Spring profile, feature flag, enable
toggle), render the deployed manifest or the effective runtime config and assert the value is
actually what the gate requires — never infer it from the source that reads it. For any limit,
quota, or retention setting, confirm the companion enable flag exists in the rendered output too.

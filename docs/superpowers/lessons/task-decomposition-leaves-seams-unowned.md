---
tags: [process, code-review, task-decomposition, integration, subagents]
problem_type: convention
date: 2026-08-14
times_seen: 1
---
# Splitting work into independently-reviewed tasks leaves the seams between them unowned

**Symptom:** An 11-task plan was executed one task at a time, each implemented fresh and reviewed
against its own brief. Nine tasks passed review on the first attempt. The whole-branch review then
found a Critical defect that none of the eleven task reviews could have caught: the logging task
shipped a correct `logback-spring.xml` gating JSON on the `dev` profile, and the values task shipped
correct values — but nothing set `env.springProfile`, so the profile never activated and the entire
logging path was dead end to end.

The same shape recurred in the reviewed-but-unlinked chain metrics take: app port → Service port
*name* → ServiceMonitor selector → Prometheus discovery flag → AppProject whitelist. Five files, five
separate task reviews, no reviewer positioned to trace the whole chain.

**Root cause:** A task-scoped review answers "does this diff match this brief?" A brief describes one
file's obligations, so a defect that lives *between* two correct files is outside every brief and
therefore outside every review. Task decomposition creates these seams as a by-product; more careful
per-task review cannot close them, because the information needed is not in any single task.

**Rule:** For any feature whose behaviour depends on a chain of artifacts agreeing (config value →
manifest → consumer, or producer field name → parser → sink), run one review that traces the full
chain end to end before merging, and state the chain explicitly as the thing to verify. Do not rely
on per-task reviews to catch integration defects. When writing the plan, name the cross-task
contracts — exact port names, field names, label keys — in each affected task's interface section so
at least the values are pinned even though the linkage is not reviewed.

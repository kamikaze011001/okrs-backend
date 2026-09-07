---
tags: [ci, cd, oidc, diagnosis, false-confidence, silent-failure]
problem_type: bug-pattern
date: 2026-09-01
times_seen: 2
---
# A path that has never run green hides N failures, not one

**Symptom:** Moving CI to a self-hosted runner was expected to surface one or two host gaps. The
`build-push-pin` job took five fixes to go green, each blocker invisible until the one before it
cleared:

1. `azure/setup-helm` could not reach `get.helm.sh` from the runner.
2. Azure CLI was not installed on the host.
3. `sudo apt-get install shellcheck` could not run — no passwordless sudo.
4. `vars.AZURE_CLIENT_ID` / `_TENANT_ID` / `_SUBSCRIPTION_ID` were never set on the GitHub
   environment, so `azure/login` failed before contacting Azure at all.
5. The federated credential subject no longer matched. GitHub had begun issuing
   `repo:owner@106855369/name@1308237376:environment:development`, while the Bicep-provisioned
   credential still said `repo:owner/name:environment:development`.

Only the first and third came from the migration. Blockers 4 and 5 had been latent since the
pipeline was written: `build-push-pin` had failed on `ubuntu-latest` with the identical
`azure/login` error two and a half weeks earlier, on every merge to `develop`, and nobody read it.

A second instance, two days later: activating the Argo CD root Application for the first
time took five more fixes, in series — an AppProject forbidding a chart's `kube-system`
Service, CRDs too large for a client-side apply, a PreSync hook referencing a ConfigMap
its own sync had not applied yet, a stale workload identity client ID, and a storage
provisioner that could not create its backing account. Every one sat in a path deferred
since the platform was built, waiting on an image pin that had never existed.

**Root cause:** Two compounding effects. A step that has never executed provides no evidence that
any *later* step works, so failures can only be discovered serially — each fix buys exactly one more
step of information. And config that was correct when written can rot silently while unexercised:
the OIDC subject format changed underneath a credential that no run had ever tried to use. The job
was not "broken by the migration"; the migration was the first thing that ran it far enough to find
out it had never worked.

Serial discovery is also self-inflicted when prerequisite checks are scattered. The Azure CLI check
sat inside the step that needed it rather than up front, so a bare host reported one missing tool
per run: fix, push, wait, learn the next one.

**Rule:** Treat "this has never run green" as *N unknown failures*, not one — budget for iteration
and do not report a migration as done because the parts that always ran still pass. Front-load every
prerequisite a job depends on into a single check that reports *all* missing dependencies at once,
so provisioning takes one report instead of a round trip per package. And when a scheduled or
merge-triggered job goes red, fix it or delete it that day: a red check nobody reads is worse than
no check, because it looks like coverage on the dashboard while proving nothing.

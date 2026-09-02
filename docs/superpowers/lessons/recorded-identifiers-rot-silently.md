---
tags: [oidc, workload-identity, azure, configuration, drift, silent-failure]
problem_type: bug-pattern
date: 2026-09-02
times_seen: 1
---
# An identifier copied from a deployment output rots without anything noticing

**Symptom:** Two instances in one week, both config that was correct when written.

1. **The federated credential subject.** `ci-identity.bicep` provisioned
   `repo:owner/name:environment:development`. GitHub began issuing subjects carrying the
   numeric owner and repository IDs — `repo:owner@106855369/name@1308237376:environment:development`
   — so no exchange could ever match. Every `build-push-pin` run failed with `AADSTS700213`
   for two and a half weeks before anyone read it. Nothing on our side had changed, and
   the repository still reported `use_immutable_subject: false` while its
   `sub_claim_prefix` already carried both IDs.
2. **The workload identity client ID.** `eso-environment/values/dev.yaml` pinned
   `1723ba78-...`, while the deployed `id-okrs-dev-hk-eso` was `44512923-...`. The
   ServiceAccount was annotated with a client ID belonging to no live identity, so
   External Secrets could not authenticate to Key Vault.

**Root cause:** Both values are *deployment outputs* pasted into source as if they were
constants. Nothing compares the recorded value against the live resource, so drift is
undetectable until something tries to use it — and the failures name neither the
identifier nor the mismatch. `InvalidProviderConfig: unable to create client` does not
mention which client, and `AADSTS700213` prints the subject *presented* but not the one
configured, so the reader has to know to go and diff it. Both look like broken
infrastructure rather than a stale string.

Worse, these values sit in paths that rarely execute — a CI push job, a first-time
bootstrap — so the rot has months to accumulate before anything reads it. See
[[never-run-paths-fail-in-series]].

**Rule:** Treat any GUID, subject, issuer, or resource name copied from a deployment
output as a cache with no invalidation. Prefer wiring it from the deployment output
directly. Where it must be hardcoded, record the command that reads back the live value
next to it, so verifying takes seconds rather than an investigation. When one of these
fails, diff the recorded value against the live resource *first* — before suspecting
permissions, networking, or the provider — because the error text will not point there.

---
tags: [ci, validation, kubeconform, helm, false-green, silent-failure]
problem_type: bug-pattern
date: 2026-08-14
times_seen: 2
---
# A CI check that validates nothing still exits 0

**Symptom:** Two instances in one branch.

1. `scripts/validate-deploy.sh` ran `kubeconform -ignore-missing-schemas` over the Argo CD
   manifests. With the CRD schema host unreachable, the run reported `Valid: 0, Skipped: 9, EXIT=0`
   — a green check that had validated nothing. Removing the flag gave `Valid: 10, Skipped: 0`: every
   manifest had a real schema, so the flag bought nothing and hid everything.
2. A new `ServiceMonitor` template was gated behind `metrics.serviceMonitor.enabled`, defaulting to
   `false`. The existing `helm template` validation lines all used values files where it was off, so
   the template rendered to nothing and CI never exercised it. It needed an explicit
   `--set metrics.serviceMonitor.enabled=true` run to be covered at all.

**Root cause:** Green is indistinguishable from vacuous at a glance. Tolerance flags
(`-ignore-missing-schemas`, `--skip-*`, `continue-on-error`) and default-off feature gates both turn
"could not check" or "nothing to check" into "check passed", and the exit code looks identical
either way.

**Rule:** A validation step must assert a positive count of artifacts actually checked, not just a
zero exit code. Do not add a skip-on-missing tolerance flag without first proving the check fails
without it; if everything resolves, omit the flag. For any default-off template or code path, add an
explicit validation run that turns it on.

---
tags: [argocd, gitops, sync, retry, silent-failure, false-confidence]
problem_type: bug-pattern
date: 2026-09-02
times_seen: 1
---
# Merging a fix does not deploy it, and Argo CD will not tell you

**Symptom:** Three instances during one dev bootstrap. In each, the fix was correct and
merged to the tracked branch, and the cluster kept running the broken state with no error
pointing at the reason.

1. **A stuck operation freezes every wave behind it.** `okrs-root` sat at
   `phase=Running`, `waiting for healthy state of Application/kube-prometheus-stack`, for
   over four hours. That app could never become healthy — it was trying to write a Service
   into `kube-system`, which its AppProject forbids. Because an Application will not
   re-evaluate its target revision while an operation is in flight, the root stayed pinned
   to an old commit. A `ServerSideApply` fix for `external-secrets` had been merged and
   was invisible on the cluster: the live Application still showed
   `["CreateNamespace=true","PruneLast=true"]`. One broken app in an early wave silently
   froze delivery of a fix aimed at a later wave.
2. **Retry exhaustion outlives the fix.** `external-secrets` and `okrs-dev-environment`
   both showed `Failed ... (retried 5 times)`. Once the retry budget is spent, automated
   sync does not try again, and `argocd.argoproj.io/refresh=hard` does not restart it —
   refresh recomputes drift, it does not sync. Both apps sat failed for as long as we let
   them, displaying an error whose cause had already been fixed and merged.
3. **Immutable fields make a merged change unappliable.** Moving the uploads PVC from
   `azurefile-csi`/RWX to `managed-csi`/RWO was merged, but `storageClassName` and
   `accessModes` cannot be changed in place, and the PVC carried
   `Delete=false,Prune=false`. Argo could not replace it and did not report a conflict —
   it reported Synced. The old PVC had to be deleted by hand before the new spec applied.

**Root cause:** Argo CD's status answers "did the last operation succeed?", not "is the
cluster running the merged commit?" Those diverge whenever an operation is stuck, a retry
budget is spent, or a field is immutable — and in all three the displayed error describes
the *original* failure, so it reads as "still broken" rather than "fix never applied". A
merged PR feels like a deploy, which is exactly the assumption that wastes the time.

**Rule:** After merging a GitOps fix, verify the *cluster object* changed, not the
Application's status — read back the field you edited (`kubectl get application X -o
jsonpath=...spec.syncPolicy.syncOptions`) before concluding the fix did not work. Treat
`phase=Running` older than a few minutes as stuck, not slow: clear it with
`kubectl patch ... --type=json -p '[{"op":"remove","path":"/operation"}]'`. Treat
`retried N times` as terminal and trigger a sync explicitly rather than refreshing. When
triggering one by hand, pass the Application's real `syncOptions` — an empty `sync: {}`
drops them, which here silently removed `CreateNamespace=true` and produced a
`namespaces "okrs-dev" not found` that looked like a new bug and was not.

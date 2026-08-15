---
tags: [helm, kubernetes, aks, resource-limits, chart-defaults, single-node]
problem_type: design-smell
date: 2026-08-14
times_seen: 1
---
# Upstream Helm chart defaults assume a production cluster, not this one

**Symptom:** Adding Loki 7.3.0 and kube-prometheus-stack 88.3.0 to a single-node AKS cluster
(~12.8 GB allocatable, already shared with Argo CD, External Secrets, and the app JVM). Rendering the
real charts before writing the manifests surfaced defaults that would each have broken the node:

- `chunksCache` defaults to enabled with `allocatedMemory: 8192`, and the chart requests 1.2x that —
  roughly **9.6 GB** for a cache. `resultsCache` adds ~1.2 GB.
- `deploymentMode` defaults to `SimpleScalable`; `singleBinary.replicas` defaults to `0`, so the
  single-binary pod would never start.
- `loki.storage.type` defaults to `s3`, `auth_enabled` to `true`, `replication_factor` to `3`, and
  `schemaConfig` to empty.
- Most dangerous: setting `singleBinary.persistence.enabled: false` renders a pod with **no volume at
  all** at `/var/loki`. Loki then writes chunks to the container's writable layer, unbounded, and
  eventually disk-pressure-evicts every pod on the node — strictly worse than the PVC being avoided.
- kube-prometheus-stack defaults `serviceMonitorSelectorNilUsesHelmValues: true`, restricting
  discovery to ServiceMonitors labelled with its own release name. A ServiceMonitor defined in a
  different chart is silently never scraped.

**Root cause:** Chart defaults are tuned for multi-node production installs with object storage.
They are safe-by-default for that shape and actively hostile to a small single-node dev cluster.
Values written from memory or from a tutorial reproduce the production assumptions invisibly, and
several of these fail silently rather than loudly.

**Rule:** Before pinning an upstream chart, run `helm show values` at the exact pinned version and
read the defaults for storage, replicas, caches, and discovery selectors — never write values from
memory. Then render with your values and assert on the output: total memory requests, PVC count,
that every expected volume mount exists, and that discovery selectors are permissive enough to see
resources from other charts.

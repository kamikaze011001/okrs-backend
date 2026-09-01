---
tags: [ci, github-actions, self-hosted, portability, supply-chain, silent-failure]
problem_type: bug-pattern
date: 2026-09-01
times_seen: 1
---
# `ubuntu-latest` is a dependency, and workflows never declare it

**Symptom:** Four jobs that had run for months on `ubuntu-latest` were pointed at a self-hosted
runner by changing only `runs-on`. Three separate assumptions the GitHub-hosted image had been
quietly satisfying broke at once:

1. `mvn` was assumed on `PATH`. The host had no Maven.
2. `sudo apt-get update && sudo apt-get install --yes shellcheck` failed with
   `sudo: a terminal is required to read the password`. The runner account had sudo, but
   password-gated.
3. `azure/setup-helm` could not download from `get.helm.sh` — three attempts, all refused — while
   the sibling job pulled Maven and its entire dependency tree over the same egress, and `curl`
   fetched the identical URL without trouble.

The prerequisite check written to catch case 2 got it wrong in an instructive way: it tested
`command -v sudo`, treating the binary's existence as proof the install would work. sudo was
present *and* unusable, which is exactly the failing case it was meant to detect.

**Root cause:** A hosted runner image bundles a large tool set, passwordless sudo, and unrestricted
egress, and a workflow inherits all of it without writing any of it down. `runs-on: ubuntu-latest`
is an undeclared dependency on every one of those properties. Nothing in the workflow distinguishes
"this job needs Docker" from "this job happens to run somewhere Docker exists", so the assumptions
are invisible until the substrate changes.

`setup-*` actions carry a second hidden dependency: reachability of whatever CDN they download from,
which is not the same as general internet access. `get.helm.sh` was unreachable on a host that
could reach Maven Central and GitHub perfectly well.

**Rule:** On a self-hosted runner, install tools as pinned binaries verified against a recorded
sha256 into `$RUNNER_TEMP/bin` on `$GITHUB_PATH` — never via a package manager, never with sudo, and
nothing left behind on a persistent host. Prefer this over a `setup-*` action, whose download host
is an undeclared dependency you cannot retry past. Pinning also stops a distro upgrade on the runner
from silently changing what a linter accepts.

Probe the *capability*, not the binary: `command -v sudo` says nothing about whether sudo will run
unattended, just as an installed CLI says nothing about whether it is authenticated. Where the check
is cheap, exercise the real thing — this is why the Docker check calls `docker info` rather than
stopping at `command -v docker`.

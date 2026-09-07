#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
REPO_KEY="${ARGOCD_REPO_KEY:-}"
KUBE_CONTEXT="${ARGOCD_KUBE_CONTEXT:-}"
ARGOCD_CHART_VERSION="10.2.2"
REPOSITORY_URL="git@github.com:kamikaze011001/okrs-backend.git"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --repo-key) REPO_KEY="$2"; shift 2 ;;
    --context) KUBE_CONTEXT="$2"; shift 2 ;;
    *) printf 'Unknown argument: %s\n' "$1" >&2; exit 2 ;;
  esac
done

for command_name in helm kubectl; do
  command -v "$command_name" >/dev/null 2>&1 || { printf 'Missing required command: %s\n' "$command_name" >&2; exit 1; }
done

# This script installs Argo CD and hands it admin of the whole cluster. Run against
# the wrong context and it lands in whichever unrelated cluster kubectl happened to
# point at, which is silent and expensive to undo. Every call below is therefore
# pinned to one explicitly resolved context rather than to ambient state.
if [[ -z "$KUBE_CONTEXT" ]]; then
  KUBE_CONTEXT="$(kubectl config current-context 2>/dev/null || true)"
  [[ -n "$KUBE_CONTEXT" ]] || {
    printf 'No kubectl context is set. Pass --context NAME or set ARGOCD_KUBE_CONTEXT.\n' >&2
    exit 1
  }
  printf '[okrs] No --context given; using the current context: %s\n' "$KUBE_CONTEXT"
fi

kubectl config get-contexts "$KUBE_CONTEXT" >/dev/null 2>&1 || {
  printf 'Unknown kubectl context: %s\n' "$KUBE_CONTEXT" >&2
  exit 1
}

KUBECTL=(kubectl --context "$KUBE_CONTEXT")
printf '[okrs] Target cluster context: %s\n' "$KUBE_CONTEXT"

"${KUBECTL[@]}" create namespace argocd --dry-run=client --output yaml | "${KUBECTL[@]}" apply -f - >/dev/null

printf '[okrs] Installing/upgrading Argo CD chart %s...\n' "$ARGOCD_CHART_VERSION"
helm upgrade --install argocd oci://ghcr.io/argoproj/argo-helm/argo-cd \
  --kube-context "$KUBE_CONTEXT" \
  --version "$ARGOCD_CHART_VERSION" \
  --namespace argocd \
  --values "$REPO_ROOT/deploy/argocd/values.yaml" \
  --wait \
  --timeout 10m

if [[ -n "$REPO_KEY" ]]; then
  [[ -f "$REPO_KEY" ]] || { printf 'Repository key not found: %s\n' "$REPO_KEY" >&2; exit 1; }
  printf '[okrs] Creating/updating the private repository credential...\n'
  "${KUBECTL[@]}" -n argocd create secret generic okrs-backend-repository \
    --from-literal=type=git \
    --from-literal=url="$REPOSITORY_URL" \
    --from-file=sshPrivateKey="$REPO_KEY" \
    --dry-run=client \
    --output yaml \
    | kubectl label --local -f - argocd.argoproj.io/secret-type=repository --overwrite --output yaml \
    | "${KUBECTL[@]}" apply -f - >/dev/null
elif ! "${KUBECTL[@]}" -n argocd get secret okrs-backend-repository >/dev/null 2>&1; then
  printf 'Argo CD needs a read-only GitHub deploy key. Re-run with --repo-key PATH or ARGOCD_REPO_KEY.\n' >&2
  exit 1
fi

printf '[okrs] Applying AppProjects and root Application...\n'
"${KUBECTL[@]}" apply -f "$REPO_ROOT/deploy/argocd/apps/projects.yaml"
if grep -q 'REPLACE_WITH_IMAGE_SHA' "$REPO_ROOT/deploy/charts/okrs-backend/values/dev.yaml"; then
  printf '[okrs] Argo CD is installed, but the root Application is deferred because dev has no immutable image pin yet.\n'
  printf '[okrs] Configure GitHub OIDC variables, run "Build, Push, and Deploy Dev", pull its GitOps commit, then rerun bootstrap.\n'
  exit 0
fi
"${KUBECTL[@]}" apply -f "$REPO_ROOT/deploy/argocd/root-application.yaml"
"${KUBECTL[@]}" -n argocd annotate application okrs-root argocd.argoproj.io/refresh=hard --overwrite >/dev/null

printf '[okrs] Bootstrap complete. Access the UI with:\n'
printf '  kubectl --context %s -n argocd port-forward service/argocd-server 8081:443\n' "$KUBE_CONTEXT"

#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
REPO_KEY="${ARGOCD_REPO_KEY:-}"
ARGOCD_CHART_VERSION="10.2.2"
REPOSITORY_URL="git@github.com:kamikaze011001/okrs-backend.git"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --repo-key) REPO_KEY="$2"; shift 2 ;;
    *) printf 'Unknown argument: %s\n' "$1" >&2; exit 2 ;;
  esac
done

for command_name in helm kubectl; do
  command -v "$command_name" >/dev/null 2>&1 || { printf 'Missing required command: %s\n' "$command_name" >&2; exit 1; }
done

kubectl create namespace argocd --dry-run=client --output yaml | kubectl apply -f - >/dev/null

printf '[okrs] Installing/upgrading Argo CD chart %s...\n' "$ARGOCD_CHART_VERSION"
helm upgrade --install argocd oci://ghcr.io/argoproj/argo-helm/argo-cd \
  --version "$ARGOCD_CHART_VERSION" \
  --namespace argocd \
  --values "$REPO_ROOT/deploy/argocd/values.yaml" \
  --wait \
  --timeout 10m

if [[ -n "$REPO_KEY" ]]; then
  [[ -f "$REPO_KEY" ]] || { printf 'Repository key not found: %s\n' "$REPO_KEY" >&2; exit 1; }
  printf '[okrs] Creating/updating the private repository credential...\n'
  kubectl -n argocd create secret generic okrs-backend-repository \
    --from-literal=type=git \
    --from-literal=url="$REPOSITORY_URL" \
    --from-file=sshPrivateKey="$REPO_KEY" \
    --dry-run=client \
    --output yaml \
    | kubectl label --local -f - argocd.argoproj.io/secret-type=repository --overwrite --output yaml \
    | kubectl apply -f - >/dev/null
elif ! kubectl -n argocd get secret okrs-backend-repository >/dev/null 2>&1; then
  printf 'Argo CD needs a read-only GitHub deploy key. Re-run with --repo-key PATH or ARGOCD_REPO_KEY.\n' >&2
  exit 1
fi

printf '[okrs] Applying AppProjects and root Application...\n'
kubectl apply -f "$REPO_ROOT/deploy/argocd/apps/projects.yaml"
if grep -q 'REPLACE_WITH_IMAGE_SHA' "$REPO_ROOT/deploy/charts/okrs-backend/values/dev.yaml"; then
  printf '[okrs] Argo CD is installed, but the root Application is deferred because dev has no immutable image pin yet.\n'
  printf '[okrs] Configure GitHub OIDC variables, run "Build, Push, and Deploy Dev", pull its GitOps commit, then rerun bootstrap.\n'
  exit 0
fi
kubectl apply -f "$REPO_ROOT/deploy/argocd/root-application.yaml"
kubectl -n argocd annotate application okrs-root argocd.argoproj.io/refresh=hard --overwrite >/dev/null

printf '[okrs] Bootstrap complete. Access the UI with:\n'
printf '  kubectl -n argocd port-forward service/argocd-server 8081:443\n'

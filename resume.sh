#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/lib/common.sh
source "$SCRIPT_DIR/scripts/lib/common.sh"

parse_lifecycle_args "$@"
load_environment "$ENVIRONMENT"
require_commands az openssl
require_azure_login

if [[ "$SCOPE" == "platform" || "$SCOPE" == "all" ]]; then
  start_platform
fi

if [[ "$SCOPE" == "environment" || "$SCOPE" == "all" ]]; then
  start_environment
  deploy_environment
fi

if [[ "$SCOPE" == "platform" || "$SCOPE" == "all" ]]; then
  configure_kube_context
  bootstrap_argocd "$REPO_KEY"
fi

if [[ "$SCOPE" == "environment" || "$SCOPE" == "all" ]]; then
  refresh_external_secrets || true
  wait_for_gitops || true
fi

log "Resume completed for scope=$SCOPE environment=$ENVIRONMENT."

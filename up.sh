#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/lib/common.sh
source "$SCRIPT_DIR/scripts/lib/common.sh"

parse_lifecycle_args "$@"
load_environment "$ENVIRONMENT"
require_commands az openssl
require_azure_login

case "$SCOPE" in
  platform)
    deploy_platform
    configure_kube_context
    bootstrap_argocd "$REPO_KEY"
    ;;
  environment)
    deploy_environment
    check_application_secrets || true
    ;;
  all)
    deploy_platform
    deploy_environment
    configure_kube_context
    bootstrap_argocd "$REPO_KEY"
    check_application_secrets || true
    wait_for_gitops || true
    ;;
esac

log "Up completed for scope=$SCOPE environment=$ENVIRONMENT."

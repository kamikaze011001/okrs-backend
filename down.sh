#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/lib/common.sh
source "$SCRIPT_DIR/scripts/lib/common.sh"

parse_lifecycle_args "$@"
load_environment "$ENVIRONMENT"
require_commands az
require_azure_login

if [[ "$SCOPE" == "environment" || "$SCOPE" == "all" ]]; then
  stop_environment
fi

if [[ "$SCOPE" == "platform" || "$SCOPE" == "all" ]]; then
  stop_platform
fi

log "Down completed for scope=$SCOPE environment=$ENVIRONMENT. Redis is disposable and was deleted when the environment scope was selected."

#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/lib/common.sh
source "$SCRIPT_DIR/scripts/lib/common.sh"

parse_lifecycle_args "$@"
load_environment "$ENVIRONMENT"
require_commands az
require_azure_login

if [[ "$ENVIRONMENT" == "dev" && "$SCOPE" != "all" ]]; then
  die "Legacy dev platform and environment resources share $PLATFORM_RESOURCE_GROUP. Use 'nuke.sh all --env dev'; partial deletion is intentionally refused."
fi

if [[ "$SCOPE" == "platform" ]]; then
  die "Platform deletion is refused while environment deployments may depend on it. Select 'all' explicitly."
fi

target_group="$ENVIRONMENT_RESOURCE_GROUP"
if [[ "$SCOPE" == "all" ]]; then
  target_group="$PLATFORM_RESOURCE_GROUP"
fi

confirmation="delete ${target_group}"
if [[ "$ASSUME_YES" != "true" ]]; then
  printf "This permanently deletes resource group '%s'.\nType '%s' to continue: " "$target_group" "$confirmation"
  read -r response
  [[ "$response" == "$confirmation" ]] || die "Nuke aborted."
fi

log "Submitting deletion for $target_group..."
az group delete --name "$target_group" --yes --no-wait
log "Deletion request accepted. Azure performs it asynchronously."

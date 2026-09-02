#!/usr/bin/env bash
# This file is sourced by several entrypoints; shared state is consumed by the caller.
# shellcheck disable=SC2034

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ENVIRONMENT="dev"
SCOPE=""
REPO_KEY="${ARGOCD_REPO_KEY:-}"
ASSUME_YES="false"

log() {
  printf '[okrs] %s\n' "$*"
}

die() {
  printf '[okrs] ERROR: %s\n' "$*" >&2
  exit 1
}

usage() {
  cat <<'USAGE'
Usage: ./<command>.sh platform|environment|all [--env dev] [--repo-key PATH] [--yes]

Scopes:
  platform     Shared AKS, ACR, Argo CD and cluster-wide operators
  environment Key Vault, PostgreSQL, Redis and workload identity
  all          Platform and environment in dependency order
USAGE
}

parse_lifecycle_args() {
  [[ $# -gt 0 ]] || { usage; exit 2; }
  SCOPE="$1"
  shift
  case "$SCOPE" in
    platform|environment|all) ;;
    *) usage; die "Unknown scope: $SCOPE" ;;
  esac

  while [[ $# -gt 0 ]]; do
    case "$1" in
      --env)
        [[ $# -ge 2 ]] || die "--env requires a value"
        ENVIRONMENT="$2"
        shift 2
        ;;
      --repo-key)
        [[ $# -ge 2 ]] || die "--repo-key requires a path"
        REPO_KEY="$2"
        shift 2
        ;;
      --yes)
        ASSUME_YES="true"
        shift
        ;;
      -h|--help)
        usage
        exit 0
        ;;
      *) die "Unknown argument: $1" ;;
    esac
  done
}

load_environment() {
  case "$1" in
    dev)
      AZURE_LOCATION="eastasia"
      PLATFORM_RESOURCE_GROUP="rg-okrs-dev-hk"
      ENVIRONMENT_RESOURCE_GROUP="rg-okrs-dev-hk"
      AKS_NAME="aks-okrs-dev-hk"
      KEY_VAULT_NAME="kv-okrs-dev-hk-ydycrv67h"
      POSTGRES_NAME="psql-okrs-dev-hk-ydycrv67hdqyc"
      REDIS_NAME="redis-okrs-dev-hk-ydycrv67hdqyc"
      KUBERNETES_NAMESPACE="okrs-dev"
      PLATFORM_PARAMETERS="$REPO_ROOT/infrastructure/platform/parameters/dev.bicepparam"
      ENVIRONMENT_PARAMETERS="$REPO_ROOT/infrastructure/environment/parameters/dev.bicepparam"
      ;;
    *) die "Environment '$1' is not enabled. QA is intentionally prepared but not provisioned." ;;
  esac
}

require_commands() {
  local command_name
  for command_name in "$@"; do
    command -v "$command_name" >/dev/null 2>&1 || die "Required command not found: $command_name"
  done
}

require_azure_login() {
  az account show --output none >/dev/null 2>&1 || die "Azure CLI is not authenticated. Run 'az login' first."
}

deployment_name() {
  printf 'okrs-%s-%s' "$1" "$ENVIRONMENT"
}

confirm_deployment() {
  local layer="$1" response expected
  [[ "$ASSUME_YES" == "true" ]] && return 0
  expected="apply ${layer} ${ENVIRONMENT}"
  printf "Review the what-if above. Type '%s' to apply it: " "$expected"
  read -r response
  [[ "$response" == "$expected" ]] || die "Deployment aborted."
}

deploy_platform() {
  local name
  name="$(deployment_name platform)"
  log "Previewing platform deployment $name..."
  az deployment sub what-if \
    --name "$name" \
    --location "$AZURE_LOCATION" \
    --parameters "$PLATFORM_PARAMETERS"
  confirm_deployment platform
  log "Applying platform deployment $name..."
  az deployment sub create \
    --name "$name" \
    --location "$AZURE_LOCATION" \
    --parameters "$PLATFORM_PARAMETERS" \
    --output table
}

postgres_password() {
  local password=""
  if az keyvault show --name "$KEY_VAULT_NAME" --resource-group "$ENVIRONMENT_RESOURCE_GROUP" --output none >/dev/null 2>&1; then
    password="$(az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name postgres-password --query value --output tsv 2>/dev/null || true)"
  fi
  if [[ -z "$password" ]]; then
    password="$(openssl rand -base64 32 | tr -d '/+=\n' | cut -c1-32)"
    log "Generated a PostgreSQL password; it will be stored in Key Vault and will not be printed." >&2
  fi
  printf '%s' "$password"
}

deploy_environment() {
  local name db_password
  wait_for_redis_deletion
  name="$(deployment_name environment)"
  db_password="$(postgres_password)"
  log "Previewing environment deployment $name..."
  az deployment sub what-if \
    --name "$name" \
    --location "$AZURE_LOCATION" \
    --parameters "$ENVIRONMENT_PARAMETERS" postgresAdministratorPassword="$db_password"
  confirm_deployment environment
  log "Applying environment deployment $name..."
  az deployment sub create \
    --name "$name" \
    --location "$AZURE_LOCATION" \
    --parameters "$ENVIRONMENT_PARAMETERS" postgresAdministratorPassword="$db_password" \
    --output table
  unset db_password
}

wait_for_redis_deletion() {
  local state attempt
  state="$(az redisenterprise show --resource-group "$ENVIRONMENT_RESOURCE_GROUP" --cluster-name "$REDIS_NAME" --query provisioningState --output tsv 2>/dev/null || true)"
  [[ "$state" == "Deleting" ]] || return 0
  log "Waiting for the previous asynchronous Redis deletion to finish..."
  for attempt in {1..120}; do
    if ! az redisenterprise show --resource-group "$ENVIRONMENT_RESOURCE_GROUP" --cluster-name "$REDIS_NAME" --output none >/dev/null 2>&1; then
      return 0
    fi
    sleep 10
  done
  die "Redis deletion did not finish within 20 minutes. Retry resume after Azure completes it."
}

configure_kube_context() {
  require_commands kubectl
  log "Loading AKS credentials for $AKS_NAME..."
  az aks get-credentials --resource-group "$PLATFORM_RESOURCE_GROUP" --name "$AKS_NAME" --overwrite-existing
}

bootstrap_argocd() {
  require_commands helm kubectl
  # az aks get-credentials names the context after the cluster, so pass it through
  # explicitly. Relying on it being current leaves the bootstrap one stray
  # kubectl config use-context away from installing Argo CD in another cluster.
  if [[ -n "$1" ]]; then
    "$REPO_ROOT/scripts/bootstrap-argocd.sh" --context "$AKS_NAME" --repo-key "$1"
  else
    "$REPO_ROOT/scripts/bootstrap-argocd.sh" --context "$AKS_NAME"
  fi
}

resource_state() {
  az "$@" --query state --output tsv 2>/dev/null || true
}

stop_environment() {
  local state
  state="$(resource_state postgres flexible-server show --resource-group "$ENVIRONMENT_RESOURCE_GROUP" --name "$POSTGRES_NAME")"
  if [[ "$state" != "" && "$state" != "Stopped" ]]; then
    log "Stopping PostgreSQL $POSTGRES_NAME..."
    az postgres flexible-server stop --resource-group "$ENVIRONMENT_RESOURCE_GROUP" --name "$POSTGRES_NAME"
  fi

  if az redisenterprise show --resource-group "$ENVIRONMENT_RESOURCE_GROUP" --cluster-name "$REDIS_NAME" --output none >/dev/null 2>&1; then
    log "Deleting disposable Redis $REDIS_NAME..."
    az redisenterprise delete --resource-group "$ENVIRONMENT_RESOURCE_GROUP" --cluster-name "$REDIS_NAME" --yes --no-wait
  fi
}

stop_platform() {
  local state
  state="$(az aks show --resource-group "$PLATFORM_RESOURCE_GROUP" --name "$AKS_NAME" --query powerState.code --output tsv 2>/dev/null || true)"
  if [[ "$state" == "Running" ]]; then
    log "Stopping AKS $AKS_NAME..."
    az aks stop --resource-group "$PLATFORM_RESOURCE_GROUP" --name "$AKS_NAME"
  fi
}

start_platform() {
  local state
  state="$(az aks show --resource-group "$PLATFORM_RESOURCE_GROUP" --name "$AKS_NAME" --query powerState.code --output tsv 2>/dev/null || true)"
  [[ -n "$state" ]] || die "AKS $AKS_NAME does not exist. Run './up.sh platform --env $ENVIRONMENT'."
  if [[ "$state" == "Stopped" ]]; then
    log "Starting AKS $AKS_NAME..."
    az aks start --resource-group "$PLATFORM_RESOURCE_GROUP" --name "$AKS_NAME"
  fi
}

start_environment() {
  local state
  state="$(resource_state postgres flexible-server show --resource-group "$ENVIRONMENT_RESOURCE_GROUP" --name "$POSTGRES_NAME")"
  if [[ "$state" == "Stopped" ]]; then
    log "Starting PostgreSQL $POSTGRES_NAME..."
    az postgres flexible-server start --resource-group "$ENVIRONMENT_RESOURCE_GROUP" --name "$POSTGRES_NAME"
  fi
}

check_application_secrets() {
  "$REPO_ROOT/scripts/secrets.sh" check --env "$ENVIRONMENT"
}

refresh_external_secrets() {
  command -v kubectl >/dev/null 2>&1 || return 0
  kubectl get namespace "$KUBERNETES_NAMESPACE" >/dev/null 2>&1 || return 0
  log "Requesting an immediate ExternalSecret refresh..."
  kubectl -n "$KUBERNETES_NAMESPACE" annotate externalsecret --all \
    force-sync="$(date +%s)" --overwrite >/dev/null
}

wait_for_gitops() {
  command -v kubectl >/dev/null 2>&1 || return 0
  kubectl get namespace argocd >/dev/null 2>&1 || return 0
  log "Waiting for Argo CD applications to become healthy..."
  kubectl -n argocd wait application --all \
    --for=jsonpath='{.status.health.status}'=Healthy \
    --timeout=15m
}

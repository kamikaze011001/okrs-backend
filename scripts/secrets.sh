#!/usr/bin/env bash
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck source=scripts/lib/common.sh
source "$SCRIPT_DIR/lib/common.sh"

REQUIRED_SECRETS=(
  jwt-access-token-secret
  jwt-refresh-token-secret
  spring-email-username
  spring-email-password
)

usage() {
  printf 'Usage: ./scripts/secrets.sh check --env dev | seed --env dev --file PATH\n'
}

[[ $# -gt 0 ]] || { usage; exit 2; }
action="$1"
shift
secret_file=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --env) ENVIRONMENT="$2"; shift 2 ;;
    --file) secret_file="$2"; shift 2 ;;
    *) die "Unknown argument: $1" ;;
  esac
done

load_environment "$ENVIRONMENT"
require_commands az
require_azure_login

check_secrets() {
  local missing=() secret_name
  for secret_name in "${REQUIRED_SECRETS[@]}"; do
    if ! az keyvault secret show --vault-name "$KEY_VAULT_NAME" --name "$secret_name" --query id --output tsv >/dev/null 2>&1; then
      missing+=("$secret_name")
    fi
  done
  if [[ ${#missing[@]} -gt 0 ]]; then
    printf '[okrs] Missing Key Vault application secrets:\n' >&2
    printf '  - %s\n' "${missing[@]}" >&2
    return 1
  fi
  log "All required application secrets exist in $KEY_VAULT_NAME."
}

seed_secrets() {
  [[ -f "$secret_file" ]] || die "Secret file not found: $secret_file"
  local line key value azure_name
  while IFS= read -r line || [[ -n "$line" ]]; do
    [[ -z "$line" || "$line" == \#* ]] && continue
    [[ "$line" == *=* ]] || die "Invalid secret line; expected KEY=value"
    key="${line%%=*}"
    value="${line#*=}"
    [[ -n "$key" && -n "$value" ]] || die "Secret keys and values must not be empty"
    case "$key" in
      JWT_ACCESS_TOKEN_SECRET) azure_name="jwt-access-token-secret" ;;
      JWT_REFRESH_TOKEN_SECRET) azure_name="jwt-refresh-token-secret" ;;
      SPRING_EMAIL_USERNAME) azure_name="spring-email-username" ;;
      SPRING_EMAIL_PASSWORD) azure_name="spring-email-password" ;;
      *) die "Unsupported application secret key: $key" ;;
    esac
    az keyvault secret set --vault-name "$KEY_VAULT_NAME" --name "$azure_name" --value "$value" --output none
    unset value
    log "Stored $azure_name."
  done < "$secret_file"
  check_secrets
}

case "$action" in
  check) check_secrets ;;
  seed) [[ -n "$secret_file" ]] || die "seed requires --file PATH"; seed_secrets ;;
  *) usage; die "Unknown action: $action" ;;
esac

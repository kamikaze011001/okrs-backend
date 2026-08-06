#!/usr/bin/env bash
set -Eeuo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

command -v az >/dev/null 2>&1 || {
  printf '[idempotency] Azure CLI is required.\n' >&2
  exit 1
}
command -v jq >/dev/null 2>&1 || {
  printf '[idempotency] jq is required.\n' >&2
  exit 1
}

identity_template="$(az bicep build --file infrastructure/platform/modules/ci-identity.bicep --stdout)"
if ! jq -e '
  [.resources[] | select(.type | endswith("/federatedIdentityCredentials"))] as $credentials
  | ($credentials | length) == 2
    and ($credentials[1].dependsOn | any(contains("/federatedIdentityCredentials")))
' >/dev/null <<<"$identity_template"; then
  printf '[idempotency] Federated credentials under one identity must be deployed serially.\n' >&2
  exit 1
fi

budget_template="$(az bicep build --file infrastructure/platform/modules/budget.bicep --stdout)"
if ! jq -e '.parameters.startDate | has("defaultValue") | not' >/dev/null <<<"$budget_template"; then
  printf '[idempotency] Budget startDate must be explicit and stable across deployments.\n' >&2
  exit 1
fi

printf '[idempotency] Infrastructure redeployment invariants passed.\n'

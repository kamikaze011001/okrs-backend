#!/usr/bin/env bash
set -Eeuo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

printf '[validate] Bash syntax\n'
bash -n up.sh down.sh resume.sh nuke.sh scripts/bootstrap-argocd.sh scripts/secrets.sh scripts/validate-deploy.sh scripts/lib/common.sh

if command -v shellcheck >/dev/null 2>&1; then
  printf '[validate] ShellCheck\n'
  shellcheck up.sh down.sh resume.sh nuke.sh scripts/bootstrap-argocd.sh scripts/secrets.sh scripts/validate-deploy.sh scripts/lib/common.sh
else
  printf '[validate] ShellCheck not installed; skipping local check.\n'
fi

printf '[validate] Helm charts\n'
helm lint deploy/charts/okrs-backend
helm lint deploy/charts/eso-environment \
  --set workloadIdentity.clientId=validation \
  --set workloadIdentity.tenantId=validation \
  --set secretStore.vaultUrl=https://validation.vault.azure.net/
helm template okrs-backend deploy/charts/okrs-backend --values deploy/charts/okrs-backend/values/local.yaml >/dev/null
helm template okrs-backend deploy/charts/okrs-backend --values deploy/charts/okrs-backend/values/dev.yaml >/dev/null
helm template okrs-backend deploy/charts/okrs-backend --values deploy/charts/okrs-backend/values/qa.yaml >/dev/null
helm template okrs-dev-environment deploy/charts/eso-environment --values deploy/charts/eso-environment/values/dev.yaml >/dev/null

if command -v az >/dev/null 2>&1; then
  printf '[validate] Bicep\n'
  az bicep build --file infrastructure/main.bicep --stdout >/dev/null
  az bicep build --file infrastructure/environment/main.bicep --stdout >/dev/null
  az bicep build-params --file infrastructure/platform/parameters/dev.bicepparam --stdout >/dev/null
  az bicep build-params --file infrastructure/environment/parameters/dev.bicepparam --stdout >/dev/null
else
  printf '[validate] Azure CLI not installed; skipping local Bicep compilation. CI performs it.\n'
fi

printf '[validate] Deployment artifacts passed available checks.\n'

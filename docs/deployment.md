# Azure and GitOps operations

## Ownership model

The existing dev resources are adopted in place. No resource is renamed or moved:

- Platform: `rg-okrs-dev-hk`, AKS `aks-okrs-dev-hk`, ACR `acrokrsdevydycrv67hdqyc`, Argo CD, and External Secrets Operator.
- Dev environment: namespace `okrs-dev`, the existing Key Vault, PostgreSQL, Redis, workload identity, SecretStore, ExternalSecrets, and service releases.
- Redis is disposable. PostgreSQL, Key Vault, PVCs, and ACR images are persistent.

Dev still shares the platform resource group for migration safety. A future QA environment should use its own environment resource group while reusing the platform AKS and ACR.

## One-time prerequisites

Install and authenticate `az`, `kubectl`, Helm, OpenSSL, and optionally ShellCheck. Register a read-only SSH deploy key on the private GitHub repository, then keep its private key outside the repository.

Before the first deployment, review both plans. Existing resources must not show delete or replacement actions:

```bash
az deployment sub what-if \
  --location eastasia \
  --parameters infrastructure/platform/parameters/dev.bicepparam

az deployment sub what-if \
  --location eastasia \
  --parameters infrastructure/environment/parameters/dev.bicepparam \
  postgresAdministratorPassword='CURRENT_PASSWORD'
```

The lifecycle scripts always run the same `what-if` immediately before applying Bicep and require the typed confirmation `apply <layer> dev`. Use `--yes` only after an already-reviewed, identical deployment when automation is intentional.

## Provision and bootstrap

Provision the platform first. Argo CD is installed, but the root Application is intentionally deferred while dev still contains `REPLACE_WITH_IMAGE_SHA`:

```bash
./up.sh platform --env dev --repo-key /secure/path/okrs-argocd
```

Set these non-secret variables on the GitHub `development` Environment from the platform deployment outputs:

- `AZURE_CLIENT_ID`
- `AZURE_TENANT_ID`
- `AZURE_SUBSCRIPTION_ID`

After one successful OIDC image push, set `acrAdminUserEnabled = false` in the platform parameter file and redeploy the platform.

Run **Build, Push, and Deploy Dev** with `workflow_dispatch`, pull its GitOps commit, then provision the environment, seed human-managed secrets, and activate the root app:

```bash
git pull
./up.sh environment --env dev
cp local/application-secrets.example.env local/application-secrets.env
./scripts/secrets.sh seed --env dev --file local/application-secrets.env
./up.sh all --env dev --repo-key /secure/path/okrs-argocd
```

Re-running `up` or `resume` is safe and adopts the explicitly named resources.

## Argo CD

```bash
make argocd-status
make argocd-port-forward
```

Open `https://localhost:8081`. The expected dependency order is:

1. `external-secrets`
2. `okrs-dev-environment`
3. `okrs-backend-dev`

The parent and child applications automatically sync, self-heal, and prune ordinary resources. Child Application deletion requires confirmation; PVCs use Helm retention and are not deleted by ordinary release pruning.

## Delivery flow

A code change on `develop` runs Maven verification, exchanges GitHub OIDC for the Azure CI identity, pushes `okrs-app:<commit-sha>`, and commits that SHA into dev values. Argo CD observes that Git commit, runs the Liquibase PreSync hook, then rolls out the Deployment.

The QA promotion workflow only promotes an already-built SHA and opens a PR. It refuses to run until QA infrastructure and Argo children exist and `deploy/environments/qa.enabled` has been added.

## Pause, resume, and destroy

```bash
./down.sh all --env dev
./resume.sh all --env dev --repo-key /secure/path/okrs-argocd
```

`down environment` stops PostgreSQL and deletes disposable Redis. `down platform` stops AKS. Resume uses the reverse order, recreates Redis through Bicep, updates its Key Vault key, forces ESO refresh, and waits for Argo health.

Dev environment-only destruction is refused because platform and dev resources share one resource group. Full destruction requires both explicit scope and exact confirmation:

```bash
./nuke.sh all --env dev
```

Key Vault purge protection is enabled by design and cannot be disabled. After a nuke, Azure retains the deleted vault and its name cannot be reused until the retention period expires; use a new Key Vault name or recover the vault for a subsequent environment.

## Validation

```bash
make validate-deploy
```

Local validation skips Bicep when Azure CLI is unavailable. The deployment-validation workflow installs Bicep and ShellCheck and runs the complete static suite.

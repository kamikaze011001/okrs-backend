using '../../main.bicep'

param projectName = 'okrs'
param platformEnvironmentName = 'dev-hk'
param location = 'eastasia'
param resourceGroupName = 'rg-okrs-dev-hk'

// Explicit names adopt the current resources instead of generating new ones.
param acrName = 'acrokrsdevydycrv67hdqyc'
param aksName = 'aks-okrs-dev-hk'
param aksDnsPrefix = 'okrsdev-hkdns'
param aksVmSize = 'Standard_D4s_v6'

param githubOwner = 'kamikaze011001'
param githubRepository = 'okrs-backend'
// GitHub embeds these numeric IDs in the OIDC subject it presents.
// Read them back with: gh api repos/kamikaze011001/okrs-backend --jq .id
param githubOwnerId = '106855369'
param githubRepositoryId = '1308237376'
param githubBranch = 'develop'
param githubEnvironmentName = 'development'

// OIDC has now pushed successfully (okrs-app:db2931a6..., 2026-08-31), so the admin
// user is no longer the fallback path and is disabled. The ACR_USERNAME and
// ACR_PASSWORD secrets on the GitHub development environment are dead once this is
// deployed and should be deleted.
param acrAdminUserEnabled = false
param budgetAmount = 50
param budgetStartDate = '2026-07-01T00:00:00Z'

param tags = {
  Lifecycle: 'development'
  Migration: 'adopt-in-place'
}

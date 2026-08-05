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
param githubBranch = 'develop'
param githubEnvironmentName = 'development'

// Keep true until the OIDC workflow has pushed successfully once; then set false.
param acrAdminUserEnabled = true
param budgetAmount = 50

param tags = {
  Lifecycle: 'development'
  Migration: 'adopt-in-place'
}

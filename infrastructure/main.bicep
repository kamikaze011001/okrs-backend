targetScope = 'subscription'

@description('Project identifier.')
param projectName string = 'okrs'

@description('Shared platform environment label. Kept as dev-hk to adopt the existing resources in place.')
param platformEnvironmentName string = 'dev-hk'

@description('Azure region.')
param location string = 'eastasia'

@description('Existing or new platform resource group.')
param resourceGroupName string

@description('Explicit ACR name. Required so an existing registry is adopted without replacement.')
param acrName string

@description('Explicit AKS name. Required so an existing cluster is adopted without replacement.')
param aksName string

@description('AKS DNS prefix.')
param aksDnsPrefix string = '${projectName}${platformEnvironmentName}dns'

@description('AKS system node VM size.')
param aksVmSize string = 'Standard_D4s_v6'

@description('Monthly resource-group budget.')
param budgetAmount int = 50

@description('Optional budget notification email. Notifications are omitted when empty.')
param alertEmail string = ''

@description('GitHub repository owner used by the CI federated credential.')
param githubOwner string = 'kamikaze011001'

@description('GitHub repository name used by the CI federated credential.')
param githubRepository string = 'okrs-backend'

@description('Branch allowed to exchange a GitHub OIDC token for the CI identity.')
param githubBranch string = 'develop'

@description('GitHub Environment allowed to exchange an OIDC token. Jobs that reference an Environment use this subject instead of the branch subject.')
param githubEnvironmentName string = 'development'

@description('Whether ACR admin credentials remain enabled during the OIDC migration.')
param acrAdminUserEnabled bool = true

@description('Additional resource tags.')
param tags object = {}

var commonTags = union(tags, {
  Project: projectName
  Environment: platformEnvironmentName
  Layer: 'platform'
  ManagedBy: 'Bicep'
})

resource platformResourceGroup 'Microsoft.Resources/resourceGroups@2024-03-01' = {
  name: resourceGroupName
  location: location
  tags: commonTags
}

module containerRegistry './platform/modules/container-registry.bicep' = {
  name: 'container-registry-${platformEnvironmentName}'
  scope: platformResourceGroup
  params: {
    name: acrName
    location: location
    adminUserEnabled: acrAdminUserEnabled
    tags: commonTags
  }
}

module kubernetes './platform/modules/aks.bicep' = {
  name: 'aks-${platformEnvironmentName}'
  scope: platformResourceGroup
  params: {
    name: aksName
    location: location
    dnsPrefix: aksDnsPrefix
    vmSize: aksVmSize
    acrName: containerRegistry.outputs.name
    tags: commonTags
  }
}

module ciIdentity './platform/modules/ci-identity.bicep' = {
  name: 'ci-identity-${platformEnvironmentName}'
  scope: platformResourceGroup
  params: {
    name: 'id-${projectName}-${platformEnvironmentName}-github'
    location: location
    githubOwner: githubOwner
    githubRepository: githubRepository
    githubBranch: githubBranch
    githubEnvironmentName: githubEnvironmentName
    acrName: containerRegistry.outputs.name
    tags: commonTags
  }
}

module budget './platform/modules/budget.bicep' = {
  name: 'budget-${platformEnvironmentName}'
  scope: platformResourceGroup
  params: {
    name: 'budget-${projectName}-${platformEnvironmentName}'
    amount: budgetAmount
    alertEmail: alertEmail
  }
}

output resourceGroupName string = platformResourceGroup.name
output acrName string = containerRegistry.outputs.name
output acrLoginServer string = containerRegistry.outputs.loginServer
output aksName string = kubernetes.outputs.name
output aksOidcIssuerUrl string = kubernetes.outputs.oidcIssuerUrl
output ciClientId string = ciIdentity.outputs.clientId
output ciPrincipalId string = ciIdentity.outputs.principalId
output tenantId string = subscription().tenantId
output subscriptionId string = subscription().subscriptionId

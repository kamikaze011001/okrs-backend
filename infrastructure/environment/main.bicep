targetScope = 'subscription'

@description('Project identifier.')
param projectName string = 'okrs'

@description('Environment identifier, for example dev-hk or qa-hk.')
param environmentName string

@description('Azure region.')
param location string = 'eastasia'

@description('Environment resource group.')
param resourceGroupName string = 'rg-${projectName}-${environmentName}'

@description('Kubernetes namespace for this environment.')
param kubernetesNamespace string

@description('Shared AKS resource group.')
param aksResourceGroupName string

@description('Shared AKS cluster name.')
param aksName string

@description('Manage the Key Vault declaratively; false only references an existing vault.')
param createKeyVault bool = true

@description('Explicit Key Vault name. Leave empty to generate one.')
param keyVaultName string = ''

@description('Manage PostgreSQL and Redis declaratively; false only references existing resources.')
param createDataServices bool = true

@description('Explicit PostgreSQL server name. Leave empty to generate one.')
param postgresServerName string = ''

@description('PostgreSQL administrator login.')
param postgresAdministratorLogin string = 'okrsadmin'

@secure()
@description('Required only when createDataServices is true.')
param postgresAdministratorPassword string = ''

@description('PostgreSQL application database.')
param postgresDatabaseName string = 'okrs_db'

@description('Explicit Redis cluster name. Leave empty to generate one.')
param redisClusterName string = ''

@description('Redis Enterprise SKU for new environments.')
param redisSkuName string = 'Balanced_B5'

@description('Redis TLS port.')
param redisPort int = 10000

@description('ServiceAccount used by the namespace SecretStore.')
param serviceAccountName string = 'okrs-secrets-reader'

@description('Optional user object ID granted Key Vault Secrets Officer on a new vault.')
param keyVaultDataAdminPrincipalId string = ''

@description('Additional resource tags.')
param tags object = {}

var commonTags = union(tags, {
  Project: projectName
  Environment: environmentName
  ManagedBy: 'Bicep'
})

resource environmentResourceGroup 'Microsoft.Resources/resourceGroups@2024-03-01' = {
  name: resourceGroupName
  location: location
  tags: commonTags
}

var generatedSuffix = uniqueString(environmentResourceGroup.id)

var resolvedKeyVaultName = !empty(keyVaultName)
  ? keyVaultName
  : take('kv-${projectName}-${environmentName}-${generatedSuffix}', 24)

var resolvedPostgresServerName = !empty(postgresServerName)
  ? postgresServerName
  : 'psql-${projectName}-${environmentName}-${generatedSuffix}'

var resolvedRedisClusterName = !empty(redisClusterName)
  ? redisClusterName
  : 'redis-${projectName}-${environmentName}-${generatedSuffix}'

resource sharedAks 'Microsoft.ContainerService/managedClusters@2023-08-01' existing = {
  name: aksName
  scope: resourceGroup(aksResourceGroupName)
}

module newKeyVault './modules/key-vault.bicep' = if (createKeyVault) {
  name: 'key-vault-${environmentName}'
  scope: environmentResourceGroup
  params: {
    keyVaultName: resolvedKeyVaultName
    location: location
    tenantId: subscription().tenantId
    enablePurgeProtection: false
    publicNetworkAccess: 'Enabled'
    keyVaultDataAdminPrincipalId: keyVaultDataAdminPrincipalId
    tags: commonTags
  }
}

module newPostgres './modules/postgres.bicep' = if (createDataServices) {
  name: 'postgres-${environmentName}'
  scope: environmentResourceGroup
  params: {
    serverName: resolvedPostgresServerName
    location: location
    administratorLogin: postgresAdministratorLogin
    administratorPassword: postgresAdministratorPassword
    databaseName: postgresDatabaseName
    postgresVersion: '14'
    availabilityZone: '1'
    skuName: 'Standard_B1ms'
    skuTier: 'Burstable'
    storageSizeGb: 32
    backupRetentionDays: 7
    tags: commonTags
  }
}

module newRedis './modules/redis.bicep' = if (createDataServices) {
  name: 'redis-${environmentName}'
  scope: environmentResourceGroup
  params: {
    clusterName: resolvedRedisClusterName
    location: location
    skuName: redisSkuName
    port: redisPort
    keyVaultName: resolvedKeyVaultName
    tags: commonTags
  }
  dependsOn: [
    newKeyVault
  ]
}

resource existingPostgres 'Microsoft.DBforPostgreSQL/flexibleServers@2025-08-01' existing = if (!createDataServices) {
  name: resolvedPostgresServerName
  scope: environmentResourceGroup
}

resource existingRedis 'Microsoft.Cache/redisEnterprise@2025-04-01' existing = if (!createDataServices) {
  name: resolvedRedisClusterName
  scope: environmentResourceGroup
}

module existingServiceMetadata './modules/service-metadata.bicep' = if (!createDataServices) {
  name: 'service-metadata-existing-${environmentName}'
  scope: environmentResourceGroup
  params: {
    keyVaultName: resolvedKeyVaultName
    postgresAdministratorLogin: postgresAdministratorLogin
    postgresFqdn: existingPostgres!.properties.fullyQualifiedDomainName
    postgresDatabaseName: postgresDatabaseName
    redisHostName: existingRedis!.properties.hostName
    redisPort: redisPort
  }
  dependsOn: [
    newKeyVault
  ]
}

module newServiceMetadata './modules/service-metadata.bicep' = if (createDataServices) {
  name: 'service-metadata-new-${environmentName}'
  scope: environmentResourceGroup
  params: {
    keyVaultName: resolvedKeyVaultName
    postgresAdministratorLogin: postgresAdministratorLogin
    postgresFqdn: newPostgres!.outputs.fqdn
    postgresDatabaseName: postgresDatabaseName
    redisHostName: newRedis!.outputs.hostName
    redisPort: redisPort
    postgresPassword: postgresAdministratorPassword
  }
  dependsOn: [
    newKeyVault
  ]
}

module secretAccess './modules/secret-access.bicep' = {
  name: 'secret-access-${environmentName}'
  scope: environmentResourceGroup
  params: {
    environmentName: environmentName
    location: location
    keyVaultName: resolvedKeyVaultName
    aksOidcIssuerUrl: sharedAks.properties.oidcIssuerProfile.issuerURL
    kubernetesNamespace: kubernetesNamespace
    serviceAccountName: serviceAccountName
    tags: commonTags
  }
  dependsOn: [
    newKeyVault
  ]
}

output environmentName string = environmentName
output resourceGroupName string = environmentResourceGroup.name
output kubernetesNamespace string = kubernetesNamespace
output keyVaultName string = resolvedKeyVaultName
output keyVaultUrl string = secretAccess.outputs.keyVaultUrl
output workloadIdentityClientId string = secretAccess.outputs.clientId
output workloadIdentityPrincipalId string = secretAccess.outputs.principalId
output tenantId string = secretAccess.outputs.tenantId
output serviceAccountName string = secretAccess.outputs.serviceAccountName
output postgresServerName string = resolvedPostgresServerName
output postgresDatabaseName string = postgresDatabaseName
output redisClusterName string = resolvedRedisClusterName
output redisPort int = redisPort

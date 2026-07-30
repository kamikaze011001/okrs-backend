// ==========================================
// PARAMETERS (Passed in from our up.sh script)
// ==========================================
param projectName string
param environment string
param location string = resourceGroup().location
param budgetAmount int = 50
param alertEmail string = ''
param budgetStartDate string = utcNow('yyyy-MM-01T00:00:00Z')
@secure()
param dbPassword string
@secure()
param userObjectId string

// ==========================================
// RESOURCES
// ==========================================
resource acr 'Microsoft.ContainerRegistry/registries@2023-01-01-preview' = {
    name: 'acr${projectName}dev${uniqueString(resourceGroup().id)}'
    location: location
    sku: {
        name: 'Basic'
    }
    properties: {
        adminUserEnabled: true
    }
}

resource budget 'Microsoft.Consumption/budgets@2021-10-01' = {
  name: 'budget-${projectName}-${environment}'
  properties: {
    amount: budgetAmount
    timeGrain: 'Monthly'
    category: 'Cost'
    timePeriod: {
      startDate: budgetStartDate
      endDate: '2026-08-30T00:00:00Z'
    }
    notifications: {
      Threshold50: {
        enabled: true
        operator: 'GreaterThan'
        threshold: 50
        contactEmails: [
          alertEmail
        ]
      }
      Threshold90: {
        enabled: true
        operator: 'GreaterThan'
        threshold: 90
        contactEmails: [
          alertEmail
        ]
      }
    }
  }
}

resource aks 'Microsoft.ContainerService/managedClusters@2023-08-01' = {
    name: 'aks-${projectName}-${environment}'
    location: location
    identity: {
        type: 'SystemAssigned'
    }
    properties: {
        dnsPrefix: '${projectName}${environment}dns'
        agentPoolProfiles: [
            {
                name: 'agentpool'
                count: 1
                vmSize: 'standard_d4s_v6'
                osType: 'Linux'
                mode: 'System'
            }
        ]
    }
}

var acrPullRoleDefinitionId = subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '7f951dda-4ed3-4680-a7ca-43fe172d538d')
resource aksAcrRoleAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  // A unique name is required for role assignments; we generate one using a GUID function
  name: guid(resourceGroup().id, aks.id, 'acrpull')
  scope: acr
  properties: {
    // We attach this role to the internal Kubelet identity that AKS uses to pull images
    principalId: aks.properties.identityProfile.kubeletidentity.objectId
    roleDefinitionId: acrPullRoleDefinitionId
    principalType: 'ServicePrincipal'
  }
}

resource postgresServer 'Microsoft.DBforPostgreSQL/flexibleServers@2023-12-01-preview' = {
    name: 'psql-${projectName}-${environment}-${uniqueString(resourceGroup().id)}'
    location: location
    sku: {
        name: 'Standard_B1ms'
        tier: 'Burstable'
    }
    properties: {
        version: '14'
        administratorLogin: 'okrsadmin'
        administratorLoginPassword: dbPassword
        storage: {
            storageSizeGB: 32
        }
    }
}

resource postgresFirewall 'Microsoft.DBforPostgreSQL/flexibleServers/firewallRules@2023-12-01-preview' = {
  parent: postgresServer
  name: 'AllowAzureIPs'
  properties: {
    startIpAddress: '0.0.0.0'
    endIpAddress: '0.0.0.0'
  }
}

resource managedRedis 'Microsoft.Cache/redisEnterprise@2024-10-01' = {
  name: 'redis-${projectName}-${environment}-${uniqueString(resourceGroup().id)}'
  location: location
  sku: {
    name: 'Balanced_B5' // Smallest tier for Managed Redis
  }
  identity: {
    type: 'None'
  }
  properties: {
    minimumTlsVersion: '1.2'
  }
}

resource redisDatabase 'Microsoft.Cache/redisEnterprise/databases@2024-10-01' = {
  parent: managedRedis
  name: 'default'
  properties: {
    clientProtocol: 'Encrypted'
    port: 10000
    clusteringPolicy: 'OSSCluster'
    evictionPolicy: 'NoEviction'
    persistence: {
      aofEnabled: false
      rdbEnabled: false
    }
  }
}

var keyVaultName = take('kv-${projectName}-${environment}-${uniqueString(resourceGroup().id)}', 24)

resource keyVault 'Microsoft.KeyVault/vaults@2023-07-01' = {
  name: keyVaultName
  location: location
  properties: {
    sku: {
      family: 'A'
      name: 'standard'
    }
    tenantId: subscription().tenantId
    enableRbacAuthorization: true // Forces Key Vault to use modern IAM/RBAC
  }
}

// This GUID is Azure's built-in ID for the "Key Vault Secrets Officer" role
var kvSecretsOfficerRoleDefId = subscriptionResourceId('Microsoft.Authorization/roleDefinitions', 'b86a8fe4-44ce-4948-aee5-eccb2c155cd7')
resource userKvRoleAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(resourceGroup().id, userObjectId, 'kv-secrets-officer')
  scope: keyVault
  properties: {
    principalId: userObjectId
    roleDefinitionId: kvSecretsOfficerRoleDefId
    principalType: 'User'
  }
}

resource dbPasswordSecret 'Microsoft.KeyVault/vaults/secrets@2023-07-01' = {
  parent: keyVault
  name: 'postgres-password'
  properties: {
    value: dbPassword
  }
}

resource redisHostSecret 'Microsoft.KeyVault/vaults/secrets@2023-07-01' = {
  parent: keyVault
  name: 'redis-host'
  properties: {
    value: '${managedRedis.properties.hostName}'
  }
}

resource redisPortSecret 'Microsoft.KeyVault/vaults/secrets@2023-07-01' = {
  parent: keyVault
  name: 'redis-port'
  properties: {
    value: '10000'
  }
}

resource redisPasswordSecret 'Microsoft.KeyVault/vaults/secrets@2023-07-01' = {
  parent: keyVault
  name: 'redis-password'
  properties: {
    value: '${redisDatabase.listKeys().primaryKey}'
  }
}

// This GUID is Azure's built-in ID for the "Key Vault Secrets User" role
var kvSecretsUserRoleDefId = subscriptionResourceId('Microsoft.Authorization/roleDefinitions', '4633458b-17de-408a-b874-0445c86b69e6')

resource aksKvRoleAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(resourceGroup().id, aks.id, 'kv-secrets-user')
  scope: keyVault
  properties: {
    // We grant read access to the same Kubelet identity we used for the ACR
    principalId: aks.properties.identityProfile.kubeletidentity.objectId
    roleDefinitionId: kvSecretsUserRoleDefId
    principalType: 'ServicePrincipal'
  }
}

// ==========================================
// OUTPUTS
// ==========================================
output acrLoginServer string = acr.properties.loginServer
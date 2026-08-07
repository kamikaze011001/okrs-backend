targetScope = 'resourceGroup'

@description('Globally unique Key Vault name.')
param keyVaultName string

@description('Azure region.')
param location string = resourceGroup().location

@description('Microsoft Entra tenant ID.')
param tenantId string = subscription().tenantId

@description('Enable purge protection. Azure does not allow this setting to be disabled after creation.')
param enablePurgeProtection bool = true

@allowed([
  'Enabled'
  'Disabled'
])
@description('Whether the Key Vault public endpoint is enabled.')
param publicNetworkAccess string = 'Enabled'

@description('Optional principal that may administer Key Vault secret values.')
param keyVaultDataAdminPrincipalId string = ''

@description('Tags applied to the Key Vault.')
param tags object = {}

resource keyVault 'Microsoft.KeyVault/vaults@2023-07-01' = {
  name: keyVaultName
  location: location
  tags: tags
  properties: {
    tenantId: tenantId
    sku: {
      family: 'A'
      name: 'standard'
    }
    enableRbacAuthorization: true
    enablePurgeProtection: enablePurgeProtection
    softDeleteRetentionInDays: 90
    publicNetworkAccess: publicNetworkAccess
    networkAcls: {
      bypass: 'AzureServices'
      defaultAction: 'Allow'
      ipRules: []
      virtualNetworkRules: []
    }
  }
}

var keyVaultSecretsOfficerRoleDefinitionId = subscriptionResourceId(
  'Microsoft.Authorization/roleDefinitions',
  'b86a8fe4-44ce-4948-aee5-eccb2c155cd7'
)

resource secretsOfficerRole 'Microsoft.Authorization/roleAssignments@2022-04-01' = if (!empty(keyVaultDataAdminPrincipalId)) {
  name: guid(
    keyVault.id,
    keyVaultDataAdminPrincipalId,
    keyVaultSecretsOfficerRoleDefinitionId
  )
  scope: keyVault
  properties: {
    principalId: keyVaultDataAdminPrincipalId
    roleDefinitionId: keyVaultSecretsOfficerRoleDefinitionId
    principalType: 'User'
  }
}

output id string = keyVault.id
output name string = keyVault.name
output url string = 'https://${keyVault.name}${environment().suffixes.keyvaultDns}/'

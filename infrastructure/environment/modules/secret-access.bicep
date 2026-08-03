targetScope = 'resourceGroup'

@description('Short environment identifier, for example dev-hk or qa-hk.')
param environmentName string

@description('Azure region used by the managed identity.')
param location string = resourceGroup().location

@description('Name of the existing or newly created Key Vault.')
param keyVaultName string

@description('AKS OIDC issuer URL, including its trailing slash.')
param aksOidcIssuerUrl string

@description('Kubernetes namespace that owns the SecretStore.')
param kubernetesNamespace string

@description('Kubernetes ServiceAccount referenced by ESO.')
param serviceAccountName string = 'okrs-secrets-reader'

@description('User-assigned identity name.')
param identityName string = 'id-okrs-${environmentName}-eso'

@description('Federated credential name.')
param federatedCredentialName string = 'fic-${environmentName}-eso'

@description('Tags applied to the managed identity.')
param tags object = {}

resource keyVault 'Microsoft.KeyVault/vaults@2023-07-01' existing = {
  name: keyVaultName
}

resource externalSecretsIdentity 'Microsoft.ManagedIdentity/userAssignedIdentities@2023-01-31' = {
  name: identityName
  location: location
  tags: tags
}

resource federatedCredential 'Microsoft.ManagedIdentity/userAssignedIdentities/federatedIdentityCredentials@2023-01-31' = {
  parent: externalSecretsIdentity
  name: federatedCredentialName
  properties: {
    audiences: [
      'api://AzureADTokenExchange'
    ]
    issuer: aksOidcIssuerUrl
    subject: 'system:serviceaccount:${kubernetesNamespace}:${serviceAccountName}'
  }
}

var keyVaultSecretsUserRoleDefinitionId = subscriptionResourceId(
  'Microsoft.Authorization/roleDefinitions',
  '4633458b-17de-408a-b874-0445c86b69e6'
)

resource keyVaultSecretsUserRole 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  name: guid(
    keyVault.id,
    externalSecretsIdentity.id,
    keyVaultSecretsUserRoleDefinitionId
  )
  scope: keyVault
  properties: {
    principalId: externalSecretsIdentity.properties.principalId
    roleDefinitionId: keyVaultSecretsUserRoleDefinitionId
    principalType: 'ServicePrincipal'
  }
}

output clientId string = externalSecretsIdentity.properties.clientId
output principalId string = externalSecretsIdentity.properties.principalId
output identityName string = externalSecretsIdentity.name
output keyVaultId string = keyVault.id
output keyVaultUrl string = 'https://${keyVaultName}${environment().suffixes.keyvaultDns}/'
output serviceAccountName string = serviceAccountName
output federatedSubject string = federatedCredential.properties.subject
output tenantId string = subscription().tenantId


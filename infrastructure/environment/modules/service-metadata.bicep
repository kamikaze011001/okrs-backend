targetScope = 'resourceGroup'

@description('Environment Key Vault name.')
param keyVaultName string

@description('PostgreSQL administrator login.')
param postgresAdministratorLogin string

@description('PostgreSQL fully qualified domain name.')
param postgresFqdn string

@description('PostgreSQL application database.')
param postgresDatabaseName string = 'okrs_db'

@description('Redis fully qualified domain name.')
param redisHostName string

@description('Redis TLS port.')
param redisPort int = 10000

resource keyVault 'Microsoft.KeyVault/vaults@2023-07-01' existing = {
  name: keyVaultName
}

resource postgresUser 'Microsoft.KeyVault/vaults/secrets@2023-07-01' = {
  parent: keyVault
  name: 'postgres-user'
  properties: {
    value: postgresAdministratorLogin
  }
}

resource postgresUrl 'Microsoft.KeyVault/vaults/secrets@2023-07-01' = {
  parent: keyVault
  name: 'spring-datasource-url'
  properties: {
    value: 'jdbc:postgresql://${postgresFqdn}:5432/${postgresDatabaseName}?sslmode=require'
  }
}

resource redisHost 'Microsoft.KeyVault/vaults/secrets@2023-07-01' = {
  parent: keyVault
  name: 'redis-host'
  properties: {
    value: redisHostName
  }
}

resource redisPortSecret 'Microsoft.KeyVault/vaults/secrets@2023-07-01' = {
  parent: keyVault
  name: 'redis-port'
  properties: {
    value: string(redisPort)
  }
}

output managedSecretNames array = [
  postgresUser.name
  postgresUrl.name
  redisHost.name
  redisPortSecret.name
]
targetScope = 'resourceGroup'

@description('Azure Managed Redis / Redis Enterprise cluster name.')
param clusterName string

@description('Azure region.')
param location string = resourceGroup().location

@description('Redis Enterprise SKU.')
param skuName string = 'Balanced_B5'

@description('Redis database port.')
param port int = 10000

@description('Environment Key Vault that receives the current Redis access key.')
param keyVaultName string

@description('Tags applied to Redis resources.')
param tags object = {}

resource redisCluster 'Microsoft.Cache/redisEnterprise@2025-04-01' = {
  name: clusterName
  location: location
  tags: tags
  sku: {
    name: skuName
  }
  identity: {
    type: 'None'
  }
  properties: {
    minimumTlsVersion: '1.2'
    encryption: {}
  }
}

resource redisDatabase 'Microsoft.Cache/redisEnterprise/databases@2025-04-01' = {
  parent: redisCluster
  name: 'default'
  properties: {
    clientProtocol: 'Encrypted'
    port: port
    // OSSCluster speaks the Redis Cluster protocol and answers with MOVED redirects,
    // which only a cluster-aware client follows. The application configures Spring
    // Data Redis as a standalone Jedis client, so every command died with
    // "JedisMovedDataException: MOVED 3878 <ip>:8501". EnterpriseCluster fronts the
    // shards with a proxy and presents one endpoint, which is what a standalone
    // client -- and a single-node cache used only for OTP and session state -- wants.
    clusteringPolicy: 'EnterpriseCluster'
    evictionPolicy: 'NoEviction'
    persistence: {
      aofEnabled: false
      rdbEnabled: false
    }
  }
}

resource keyVault 'Microsoft.KeyVault/vaults@2023-07-01' existing = {
  name: keyVaultName
}

resource redisPasswordSecret 'Microsoft.KeyVault/vaults/secrets@2023-07-01' = {
  parent: keyVault
  name: 'redis-password'
  properties: {
    value: redisDatabase.listKeys().primaryKey
  }
}

output id string = redisCluster.id
output name string = redisCluster.name
output databaseName string = redisDatabase.name
output hostName string = redisCluster.properties.hostName
output port int = port

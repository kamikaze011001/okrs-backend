targetScope = 'resourceGroup'

@description('Azure Managed Redis / Redis Enterprise cluster name.')
param clusterName string

@description('Azure region.')
param location string = resourceGroup().location

@description('Redis Enterprise SKU.')
param skuName string = 'Balanced_B5'

@description('Redis database port.')
param port int = 10000

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
    clusteringPolicy: 'OSSCluster'
    evictionPolicy: 'NoEviction'
    persistence: {
      aofEnabled: false
      rdbEnabled: false
    }
  }
}

output id string = redisCluster.id
output name string = redisCluster.name
output databaseName string = redisDatabase.name
output hostName string = redisCluster.properties.hostName
output port int = port
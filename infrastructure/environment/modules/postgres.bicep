targetScope = 'resourceGroup'

@description('PostgreSQL Flexible Server name.')
param serverName string

@description('Azure region.')
param location string = resourceGroup().location

@description('PostgreSQL administrator login.')
param administratorLogin string = 'okrsadmin'

@secure()
@description('PostgreSQL administrator password.')
param administratorPassword string

@description('Application database name.')
param databaseName string = 'okrs_db'

@description('PostgreSQL major version.')
param postgresVersion string = '14'

@description('Availability zone.')
param availabilityZone string = '1'

@description('Compute SKU.')
param skuName string = 'Standard_B1ms'

@description('Compute tier.')
param skuTier string = 'Burstable'

@description('Storage size in GiB.')
param storageSizeGb int = 32

@description('Backup retention in days.')
param backupRetentionDays int = 7

@description('Tags applied to PostgreSQL resources.')
param tags object = {}

resource postgresServer 'Microsoft.DBforPostgreSQL/flexibleServers@2025-08-01' = {
  name: serverName
  location: location
  tags: tags
  sku: {
    name: skuName
    tier: skuTier
  }
  properties: {
    administratorLogin: administratorLogin
    administratorLoginPassword: administratorPassword
    version: postgresVersion
    availabilityZone: availabilityZone
    authConfig: {
      activeDirectoryAuth: 'Disabled'
      passwordAuth: 'Enabled'
    }
    backup: {
      backupRetentionDays: backupRetentionDays
      geoRedundantBackup: 'Disabled'
    }
    highAvailability: {
      mode: 'Disabled'
    }
    network: {
      publicNetworkAccess: 'Enabled'
    }
    storage: {
      autoGrow: 'Disabled'
      storageSizeGB: storageSizeGb
      tier: 'P4'
      type: 'Premium_LRS'
    }
  }
}

resource applicationDatabase 'Microsoft.DBforPostgreSQL/flexibleServers/databases@2025-08-01' = {
  parent: postgresServer
  name: databaseName
  properties: {
    charset: 'UTF8'
    collation: 'en_US.utf8'
  }
}

resource allowAzureServices 'Microsoft.DBforPostgreSQL/flexibleServers/firewallRules@2025-08-01' = {
  parent: postgresServer
  name: 'AllowAzureIPs'
  properties: {
    startIpAddress: '0.0.0.0'
    endIpAddress: '0.0.0.0'
  }
}

output id string = postgresServer.id
output name string = postgresServer.name
output databaseName string = applicationDatabase.name
output administratorLogin string = administratorLogin
output fqdn string = postgresServer.properties.fullyQualifiedDomainName
output jdbcUrl string = 'jdbc:postgresql://${postgresServer.properties.fullyQualifiedDomainName}:5432/${databaseName}?sslmode=require'
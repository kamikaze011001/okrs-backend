using '../main.bicep'

param projectName = 'okrs'
param environmentName = 'dev-hk'
param location = 'eastasia'
param resourceGroupName = 'rg-okrs-dev-hk'
param kubernetesNamespace = 'okrs-dev'

param aksResourceGroupName = 'rg-okrs-dev-hk'
param aksName = 'aks-okrs-dev-hk'

param createKeyVault = false
param keyVaultName = 'kv-okrs-dev-hk-ydycrv67h'

param createDataServices = false
param postgresServerName = 'psql-okrs-dev-hk-ydycrv67hdqyc'
param postgresAdministratorLogin = 'okrsadmin'
param postgresDatabaseName = 'okrs_db'

param redisClusterName = 'redis-okrs-dev-hk-ydycrv67hdqyc'
param redisSkuName = 'Balanced_B5'
param redisPort = 10000

param serviceAccountName = 'okrs-secrets-reader'

param tags = {
  Lifecycle: 'development'
}
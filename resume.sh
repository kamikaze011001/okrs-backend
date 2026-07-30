#!/bin/bash

# ==========================================
# VARIABLES (Must match up.sh)
# ==========================================
PROJECT_NAME="okrs"
ENV="dev-hk"
RESOURCE_GROUP="rg-${PROJECT_NAME}-${ENV}"

echo "Initiating resume sequence for $RESOURCE_GROUP..."

AKS_NAME="aks-${PROJECT_NAME}-${ENV}"
echo "Checking AKS Cluster state..."
AKS_STATE=$(az aks show --name $AKS_NAME --resource-group $RESOURCE_GROUP --query "powerState.code" -o tsv 2>/dev/null)

if [ "$AKS_STATE" == "Stopped" ]; then
  echo "Waking up AKS Cluster: $AKS_NAME..."
  az aks start --name $AKS_NAME --resource-group $RESOURCE_GROUP
  echo "AKS Cluster is awake!"
else
  echo "AKS Cluster is already running."
fi

echo "Finding PostgreSQL server..."
PSQL_NAME=$(az postgres flexible-server list --resource-group $RESOURCE_GROUP --query "[0].name" -o tsv 2>/dev/null)

if [ -n "$PSQL_NAME" ]; then
  PSQL_STATE=$(az postgres flexible-server show --name "$PSQL_NAME" --resource-group $RESOURCE_GROUP --query "state" -o tsv 2>/dev/null)
  if [ "$PSQL_STATE" == "Stopped" ]; then
    echo "Waking up PostgreSQL Server: $PSQL_NAME..."
    az postgres flexible-server start --name "$PSQL_NAME" --resource-group $RESOURCE_GROUP
    echo "PostgreSQL is awake!"
  else
    echo "PostgreSQL is already running."
  fi
fi

echo "Fetching user ID for Key Vault permissions..."
USER_OBJECT_ID=$(az ad signed-in-user show --query id -o tsv)

echo "Checking for existing database password in Key Vault..."
EXISTING_KV=$(az keyvault list --resource-group $RESOURCE_GROUP --query "[0].name" -o tsv 2>/dev/null)

if [ -n "$EXISTING_KV" ]; then
  EXISTING_PASSWORD=$(az keyvault secret show --name "postgres-password" --vault-name "$EXISTING_KV" --query "value" -o tsv 2>/dev/null)
fi

if [ -n "$EXISTING_PASSWORD" ]; then
  echo "Found existing Postgres password. Reusing it."
  DB_PASSWORD=$EXISTING_PASSWORD
else
  echo "No existing password found. Generating a new one..."
  DB_PASSWORD=$(openssl rand -base64 12)
fi

echo "Running Bicep to repair infrastructure (recreating Redis and syncing keys)..."
az deployment group create \
  --resource-group $RESOURCE_GROUP \
  --template-file infrastructure/main.bicep \
  --parameters projectName=$PROJECT_NAME environment=$ENV dbPassword="$DB_PASSWORD" userObjectId="$USER_OBJECT_ID" \
  --output table

echo "Cluster is awake and running!"
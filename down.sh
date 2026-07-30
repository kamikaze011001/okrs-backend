#!/bin/bash

# ==========================================
# VARIABLES (Must match up.sh)
# ==========================================
PROJECT_NAME="okrs"
ENV="dev-hk"
RESOURCE_GROUP="rg-${PROJECT_NAME}-${ENV}"

echo "Initiating shutdown sequence for $RESOURCE_GROUP to save costs..."

AKS_NAME="aks-${PROJECT_NAME}-${ENV}"
echo "Checking AKS Cluster state..."
AKS_STATE=$(az aks show --name $AKS_NAME --resource-group $RESOURCE_GROUP --query "powerState.code" -o tsv 2>/dev/null)

if [ "$AKS_STATE" == "Running" ]; then
  echo "Stopping AKS Cluster: $AKS_NAME..."
  az aks stop --name $AKS_NAME --resource-group $RESOURCE_GROUP
  echo "AKS Cluster stopped."
else
  echo "AKS Cluster is already stopped or not found."
fi

echo "Finding PostgreSQL server..."
PSQL_NAME=$(az postgres flexible-server list --resource-group $RESOURCE_GROUP --query "[0].name" -o tsv 2>/dev/null)

if [ -n "$PSQL_NAME" ]; then
  echo "Stopping PostgreSQL Server: $PSQL_NAME..."
  az postgres flexible-server stop --name "$PSQL_NAME" --resource-group $RESOURCE_GROUP
  echo "PostgreSQL stopped successfully."
else
  echo "No PostgreSQL server found."
fi

echo "Finding Managed Redis cluster..."
REDIS_NAME=$(az redisenterprise list --resource-group $RESOURCE_GROUP --query "[0].name" -o tsv 2>/dev/null)

if [ -n "$REDIS_NAME" ]; then
  echo "Deleting Managed Redis to stop billing: $REDIS_NAME..."
  # --no-wait allows the script to finish without hanging for 10 minutes
  az redisenterprise delete \
    --cluster-name "$REDIS_NAME" \
    --resource-group $RESOURCE_GROUP \
    --yes \
    --no-wait
  echo "Redis deletion triggered in the background."
else
  echo "No Managed Redis cluster found."
fi

echo "=========================================="
echo "Shutdown complete! Your compute costs are now practically $0."
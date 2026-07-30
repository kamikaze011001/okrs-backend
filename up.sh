#!/bin/bash

PROJECT_NAME="okrs"
ENV="dev-hk"
LOCATION="eastasia"
RESOURCE_GROUP="rg-${PROJECT_NAME}-${ENV}"

echo "Checking for existing database password..."

# See if a Key Vault already exists in this Resource Group
EXISTING_KV=$(az keyvault list --resource-group $RESOURCE_GROUP --query "[0].name" -o tsv)

if [ -n "$EXISTING_KV" ]; then
  # Try to fetch the existing password from the Key Vault
  EXISTING_PASSWORD=$(az keyvault secret show --name "postgres-password" --vault-name "$EXISTING_KV" --query "value" -o tsv 2>/dev/null)
fi

if [ -n "$EXISTING_PASSWORD" ]; then
  echo "Found existing password in Key Vault: $EXISTING_KV. Reusing it."
  DB_PASSWORD=$EXISTING_PASSWORD
else
  echo "No existing password found. Generating a new one..."
  DB_PASSWORD=$(openssl rand -base64 12)
  echo "Generated DB Password: $DB_PASSWORD"
  echo "(Save this password somewhere safe if you need to connect manually!)"
fi

USER_OBJECT_ID=$(az ad signed-in-user show --query id -o tsv)

echo "Creating Resource Group: $RESOURCE_GROUP in $LOCATION..."
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION \
  --tags Environment=$ENV Project=$PROJECT_NAME

echo "Deploying infrastructure via main.bicep..."
# az deployment group create takes our bicep file and pushes it to Azure
az deployment group create \
  --resource-group $RESOURCE_GROUP \
  --template-file infrastructure/main.bicep \
  --parameters projectName=$PROJECT_NAME environment=$ENV dbPassword="$DB_PASSWORD" userObjectId="$USER_OBJECT_ID" \
  --output table

echo "Foundation setup complete!"
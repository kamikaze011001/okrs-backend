#!/bin/bash

# ==========================================
# VARIABLES (Must match up.sh)
# ==========================================
PROJECT_NAME="okrs"
ENV="dev-hk"
RESOURCE_GROUP="rg-${PROJECT_NAME}-${ENV}"

echo "WARNING: This will permanently delete the Resource Group '$RESOURCE_GROUP' and ALL resources inside it."
read -p "Are you absolutely sure you want to proceed? (y/N): " confirm

if [[ "$confirm" == [yY] || "$confirm" == [yY][eE][sS] ]]; then
  echo "Nuking Resource Group: $RESOURCE_GROUP..."
  # The --no-wait flag tells the CLI to send the delete command and return control to you immediately,
  # rather than forcing you to watch the terminal for 10 minutes while it deletes.
  az group delete \
    --name $RESOURCE_GROUP \
    --yes \
    --no-wait

  echo "Nuke command sent! The resources are being deleted in the background."
else
  echo "Nuke aborted. Your resources are safe."
fi
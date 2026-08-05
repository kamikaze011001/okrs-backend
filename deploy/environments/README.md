# Environment activation

Dev is active. QA values and its promotion workflow are prepared, but QA does not have Azure resources or active Argo CD child Applications yet.

When QA infrastructure, Key Vault secrets, ESO identity metadata, and child Applications have been created and validated, add an empty `qa.enabled` file here. The QA promotion workflow deliberately refuses to run before that marker exists.

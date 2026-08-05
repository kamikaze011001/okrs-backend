SHELL := /bin/bash

NAMESPACE ?= okrs-local
BACKEND_RELEASE ?= okrs-backend
BACKEND_RESOURCE_NAME ?= $(BACKEND_RELEASE)-okrs-backend
DEPENDENCIES_RELEASE ?= okrs-dependencies
BACKEND_CHART := deploy/charts/okrs-backend
DEPENDENCIES_CHART := deploy/charts/local-dependencies
LOCAL_VALUES := $(BACKEND_CHART)/values/local.yaml
LOCAL_ENV := local/.env
IMAGE := okrs-app
IMAGE_TAG := latest

.PHONY: \
	build namespace secret dependencies app deploy \
	status logs port-forward rollback restart clean \
	validate-deploy argocd-status argocd-port-forward

build:
	@echo "Building $(IMAGE):$(IMAGE_TAG) in Minikube..."
	minikube image build -t $(IMAGE):$(IMAGE_TAG) .

namespace:
	@echo "Ensuring namespace $(NAMESPACE) exists..."
	kubectl create namespace $(NAMESPACE) \
		--dry-run=client \
		--output yaml | kubectl apply -f -

secret: namespace
	@test -f $(LOCAL_ENV) || \
		(echo "Missing $(LOCAL_ENV). Create it before deploying."; exit 1)
	@echo "Creating/updating local application Secret..."
	kubectl -n $(NAMESPACE) create secret generic okrs-backend-credentials \
		--from-env-file=$(LOCAL_ENV) \
		--dry-run=client \
		--output yaml | kubectl apply -f -

dependencies: secret
	@echo "Ensuring the Valkey Helm repository is configured..."
	helm repo add valkey https://valkey.io/valkey-helm/ --force-update
	helm repo update valkey
	@echo "Building local chart dependencies..."
	helm dependency build $(DEPENDENCIES_CHART)
	@echo "Installing/upgrading local PostgreSQL and Valkey..."
	helm upgrade --install $(DEPENDENCIES_RELEASE) \
		$(DEPENDENCIES_CHART) \
		--namespace $(NAMESPACE) \
		--wait \
		--wait-for-jobs \
		--timeout 5m

app: secret
	@echo "Installing/upgrading the OKRs backend..."
	helm upgrade --install $(BACKEND_RELEASE) \
		$(BACKEND_CHART) \
		--namespace $(NAMESPACE) \
		--values $(LOCAL_VALUES) \
		--wait \
		--wait-for-jobs \
		--timeout 10m

deploy:
	$(MAKE) build
	$(MAKE) dependencies
	$(MAKE) app
	@echo "Deployment complete. Run 'make status' to inspect it."

status:
	kubectl -n $(NAMESPACE) get pods,services,persistentvolumeclaims,jobs
	helm -n $(NAMESPACE) list

logs:
	kubectl -n $(NAMESPACE) logs \
		--selector app.kubernetes.io/name=okrs-backend \
		--all-containers=true \
		--follow \
		--max-log-requests=10

port-forward:
	kubectl -n $(NAMESPACE) port-forward \
		service/$(BACKEND_RESOURCE_NAME) 8080:8080 \
		--address 0.0.0.0

rollback:
	helm rollback $(BACKEND_RELEASE) 0 \
		--namespace $(NAMESPACE) \
		--wait \
		--timeout 10m

restart: build
	kubectl -n $(NAMESPACE) rollout restart \
		deployment/$(BACKEND_RESOURCE_NAME)
	kubectl -n $(NAMESPACE) rollout status \
		deployment/$(BACKEND_RESOURCE_NAME) \
		--timeout=10m

clean:
	helm uninstall $(BACKEND_RELEASE) \
		--namespace $(NAMESPACE) \
		--ignore-not-found
	helm uninstall $(DEPENDENCIES_RELEASE) \
		--namespace $(NAMESPACE) \
		--ignore-not-found
	@echo "Releases removed. PersistentVolumeClaims and namespace were preserved."

validate-deploy:
	./scripts/validate-deploy.sh

argocd-status:
	kubectl -n argocd get applications.argoproj.io

argocd-port-forward:
	kubectl -n argocd port-forward service/argocd-server 8081:443

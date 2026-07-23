# ==============================================================================
# Minikube Local Deployment Makefile
# ==============================================================================

.PHONY: build env db app deploy status logs url clean restart

# 1. Build the Docker image inside Minikube's Docker engine
build:
	@echo "🚀 Building okrs-app:latest inside Minikube..."
	@eval $$(minikube docker-env) && docker build -t okrs-app:latest .

# 2. Apply ConfigMap, Secret, and Storage
env:
	@echo "🔐 Applying ConfigMap, Secret, and PVC..."
	kubectl apply -f k8s/environments/local-minikube/app-config.yaml
	kubectl apply -f k8s/environments/local-minikube/app-secret.yaml
	kubectl apply -f k8s/base/application/app-pvc.yaml

# 3. Apply Postgres and Redis
db:
	@echo "🗄️ Starting Databases..."
	kubectl apply -f k8s/base/infrastructure/postgres/postgres-statefulset.yaml
	kubectl apply -f k8s/base/infrastructure/redis/redis-statefulset.yaml

# 4. Apply Spring Boot Application
app:
	@echo "⚙️ Starting Spring Boot Application..."
	kubectl apply -f k8s/base/application/app-deployment.yaml
	kubectl apply -f k8s/base/application/app-service.yaml

# 5. The Master Command: Build and deploy everything in the correct order
deploy: build env db app
	@echo "✅ Deployment complete! Run 'make status' to check pods."

# ==============================================================================
# Utility Commands
# ==============================================================================

# Check the status of all your resources
status:
	kubectl get pods,svc,pvc

# Stream the live logs of your Spring Boot application
logs:
	@echo "📜 Tailing logs for okrs-app..."
	kubectl logs -l app=okrs-app -f

# Get the local WSL 2 URL to test in Windows Chrome
url:
	@echo "🌐 Opening Minikube tunnel..."
	minikube service okrs-app-service --url

# Delete everything from the cluster
clean:
	@echo "🧹 Cleaning up Kubernetes resources..."
	kubectl delete -f k8s/base/application/app-deployment.yaml --ignore-not-found
	kubectl delete -f k8s/base/application/app-service.yaml --ignore-not-found
	kubectl delete -f k8s/base/infrastructure/postgres/postgres-statefulset.yaml --ignore-not-found
	kubectl delete -f k8s/base/infrastructure/redis/redis-statefulset.yaml --ignore-not-found
	kubectl delete -f k8s/environments/local-minikube/app-config.yaml --ignore-not-found
	kubectl delete -f k8s/environments/local-minikube/app-secret.yaml --ignore-not-found
	kubectl delete -f k8s/base/application/app-pvc.yaml --ignore-not-found

# Rebuild the code and restart the Spring Boot pod without touching the databases
restart: build
	@echo "🔄 Restarting Spring Boot pod with new image..."
	kubectl rollout restart deployment/okrs-app-deployment
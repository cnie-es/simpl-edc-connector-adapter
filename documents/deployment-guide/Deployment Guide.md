# Deployment Guide

## Purpose
This guide provides step-by-step instructions and best practices for deploying the `edc-connector-adapter` project into production or controlled environments, including cloud, on-premises, containers, and Kubernetes. It covers environment requirements, configuration, deployment workflows, scaling, monitoring, security, and recovery procedures.

## Component Description
The component allows integration with the Gaia-X Data Space Connector ecosystem.

## Prerequisites
- Kafka broker (only for consumer profile)
- OpenBao Vault (only for consumer profile)
- Gaia-X EDC connector service installed for the provider participant.
- Gaia-X Federated catalogue service exposed by a Tier2 Gateway on the authority participant.

---

## 1. Environment Requirements

- **Infrastructure**: x86_64 architecture, minimum 2 vCPUs, 4GB RAM (production: 4+ vCPUs, 8GB+ RAM recommended).
- **Operating System**: Linux (Ubuntu 20.04+, CentOS 7+, or compatible). Windows containers are not supported.
- **Network**: Outbound internet access for image pulls and updates. Open required ports (see below).
- **Security & Permissions**:
    - Docker/Kubernetes: User must have permissions to manage containers and images.
    - Secrets: No secrets required.

---

## 2. Configuration Details

- **Environment Variables**: Set via Docker/Kubernetes manifests or `.env` files.
- **Config Files**: Mount configuration files as volumes or use Kubernetes ConfigMaps/Secrets.
- **Secrets Management**: No secrets required.

### 2.1 OpenBao Vault integration (consumer profile)
1) create a Vault role for the application
2) create your Vault secrets engine
3) create a new Vault secret for **edc-connector-adapter** application named *{{ .Release.Namespace }}-{{ .Release.Name }}*
4) edit your Helm *values.yaml* file and configure:
    - *vault.secretEngine*
    - *vault.role*
    - *vault.service*
5) adding the following keys for the required component credentials

### 2.2 Kafka credentials
```
KAFKA_AUTH_SASL_USERNAME
KAFKA_AUTH_SASL_PASSWORD
```
> if your Kakfa broker does not require any authentication set kafka.auth.type as blank in Helm values.yaml file

### 2.3 
---

## 3. Step-by-Step Deployment

### A. Docker
1. **Build the image** (if not using a published one):
   ```sh
   docker build -t edc-connector-adapter:latest .
   ```
2. **Run the container**:
   ```sh
   docker run -d --name edc-connector-adapter \
     -p 8080:8080 \
     edc-connector-adapter:latest
   ```

### B. Kubernetes (using Helm charts)
1. **Configure values** in `charts/values.yaml` (or override via CLI):
    - Required Helm variables to configure:
      ```yaml
      # Logging configuration
      log4j.config: <value>
      
      # OpenAPI configuration
      openapiConfig.servers: <value>
      
      # OpenTelemetry configuration
      openTelemetry.otlpExporter.endpoint: <value>
      openTelemetry.otlpExporter.protocol: <value>
      openTelemetry.propagators: <value>
      openTelemetry.environment: <value>
      openTelemetry.disabled: <value>
      
      # CORS configuration
      web.mvc.cors.allowedOrigins: <value>
      
      # Application profile (provider or consumer)
      profile: <value>
      
      # Url to the participant EDC connector (internal service)
      edcConnector.baseUrl: <value>
      ```
      
    - Required Helm variables for profile `provider`:
      ```yaml
      # Tier2 url to the provider EDC connector service behind the TLS gateway (ending with the route context name)
      # this will be enriched in the SD json by enrichAndValidate API
      edcConnector.tier2BaseUrl: <value>
      # The participant id to be used as assigner in policies as expected by the EDC connector on contract negotiation
      # If edcConnector.participantIdSource is provided then edcConnector.participantId value will be ignored. In other words its optional
      # and not required if edcConnector.participantIdSource is provided.
      edcConnector.participantId: <value>
      # Declaration of edcConnector.participantIdSource variable is optional. If value will be provided then edcConnector.participantId
      # will be ignored, and participantId will be fetched from authentication provider using this url. See init container
      # wait-for-participant-id defined in deployment.yaml for more details about the process.
      # If value is not provided then edcConnector.participantId will be used and no init container will be added.
      edcConnector.participantIdSource: <value>
      ```
      
    - Required Helm variables for profile `consumer`:
      ```yaml
      # Path to the application jar file (do not modify it)
      appJarPath: "/home/simpluser/app.jar"
      
      # EDC connector adapter api key to be used for the EDC connector registration
      edcConnector.apiKey: <value>
      
      # Urls list to KAFKA bootstrap servers (comma separated if more than one)
      kafka.bootstrapServers: <value>
      
      # Consumers group id
      kafka.consumer.groupId: <value>
      
      # Topic used by transfer process
      kafka.topics.transferProcess: <value>
      
      # Can be one of: SASL_PLAINTEXT (if blank no auth enabled)
      kafka.auth.type: <value>
      
      # Flag to stop application if kafka broker is not available
      kafka.fatalIfBrokerNotAvailable: <value>
      
      # Vault configuration for secrets management
      vault.secretEngine: <value>
      vault.secretPath: <value>
      vault.role: <value>
      vault.service: <value>
      ```
      
2. **Deploy with Helm**:
   ```sh
   helm install edc-connector-adapter ./charts
   ```
3. **Setup with ArgoCD**:
    - Create an ArgoCD application pointing to your Helm chart repository
    - Configure sync options to use the Helm chart values
    - Set the target namespace and cluster for deployment
    - Enable automated sync if desired for continuous deployment

4. **Verify deployment**:
   ```sh
   kubectl get pods -l app=edc-connector-adapter
   kubectl logs <pod-name>
   ```

5. **Check startup status**:
    - Verify the service is running correctly by calling the `/status` endpoint

### C. Cloud Platforms
- Use managed Kubernetes (EKS, AKS, GKE) or container services (ECS, ACI) as above.
- Ensure cloud firewalls/security groups allow required ports.

---

## 4. Scaling and Monitoring

- **Scaling**:
    - Docker: Run multiple containers behind a load balancer (e.g., NGINX, HAProxy).
    - Kubernetes: Set `replicaCount` in Helm values or use Horizontal Pod Autoscaler.
- **Monitoring**:
    - Integrate with Prometheus, Grafana, or cloud-native monitoring.
    - Expose health endpoints and configure liveness/readiness probes in Kubernetes.

---

## 5. Security Best Practices

- **Network**: This component should only be accessible from trusted networks or as internal cluster service.
- **Updates**: Regularly update base images and dependencies.

---

## 6. Rollback and Recovery

- **Docker**: Stop and remove the faulty container, then redeploy the previous image tag.
- **Kubernetes**: Use `helm rollback` or `kubectl rollout undo` to revert to a previous release.
- **Backups**: Regularly back up configuration, secrets, and persistent data.
- **Logs**: Aggregate logs for troubleshooting (e.g., ELK stack, cloud logging).

---

## References
- [Docker Documentation](https://docs.docker.com/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Helm Documentation](https://helm.sh/docs/)

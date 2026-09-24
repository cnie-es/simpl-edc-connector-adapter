# Installation Guide

## Overview
This guide will help you install and run **edcconnectoradapter** on a fresh system. Follow the steps for your operating system and refer to troubleshooting if you encounter issues.

---

## Prerequisites

- **Java**: JDK 17 or higher
- **Maven**: 3.8.x or higher
- **Git**: Latest stable version
- **Operating System**: Windows, macOS, or Linux
- **Internet Connection**: Required for downloading dependencies

> **Note:** Ensure your `JAVA_HOME` and `MAVEN_HOME` environment variables are set correctly.

---

## Step-by-Step Installation

### 1. Clone the Repository

```
git clone https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter.git
cd edcconnectoradapter
```

### 2. Build the Project

#### Windows
Run:
```
#build_and_release.bat
```

#### macOS/Linux
Run:
```
chmod +x #build_and_release.bat
./#build_and_release.bat
```

_Alternatively, use Maven directly:_
```
mvn clean install
```

### 3. Run the Application

Refer to the documentation or run the generated JAR/WAR file as per your deployment needs.

---

### 4. Verify Installation

To verify the application is running correctly, call the `/status` endpoint:

```
curl http://localhost:<port>/status
```

Replace `<port>` with the actual port number configured for the application.

---

## Example Output

After a successful build, you should see output similar to:
```
[INFO] BUILD SUCCESS
```

---

## Configuration
### Configuration Parameters
##### Core Service Configuration
###### This section defines the core runtime settings of the service, including ports, domain resolution and environment identifiers.
---------------
| Parameter name         | Description                       | Default value       | Allowed values/range | Minimum version | Status |
| ---------------------- | --------------------------------- | ------------------- | -------------------- | --------------- | ------ |
| servicePort            | Service port exposed internally   | 8080                | integer (1–65535)    | 1.12.0           | Active |
| containerPort          | Container listening port          | 8080                | integer (1–65535)    | 1.12.0           | Active |
| domainUrlEnvIdentifier | Domain URL environment identifier | dev                 | string               | 1.12.0           | Active |
| domain                 | Cluster domain                    | dev.simpl-europe.eu | valid DNS            | 1.12.0           | Active |

#### Logging Configuration
###### This section defines logging behaviour and dynamic configuration reload.
--------------
| Parameter name        | Description                            | Default value           | Allowed values/range            | Minimum version | Status |
| --------------------- | -------------------------------------- | ----------------------- | ------------------------------- | --------------- | ------ |
| log4j.config          | Path to log4j2 configuration file      | file:/config/log4j2.xml | valid file path                 | 1.12.0           | Active |
| log4j.monitorInterval | Log4j config reload interval (seconds) | 10                      | integer ≥ 0                     | 1.12.0           | Active |
| log4j.level.root      | Root logging level                     | INFO                    | TRACE, DEBUG, INFO, WARN, ERROR | 1.12.0           | Active |
| log4j.level.app       | Application logging level              | INFO                    | TRACE, DEBUG, INFO, WARN, ERROR | 1.12.0           | Active |
| log4j.level.feign     | Feign client logging level             | INFO                    | TRACE, DEBUG, INFO, WARN, ERROR | 1.12.0           | Active |

#### OpenAPI Configuration
###### This section defines OpenAPI/Swagger exposure settings.
--------------
| Parameter name        | Description                                                 | Default value                                                                                          | Allowed values/range | Minimum version | Status |
| --------------------- | ----------------------------------------------------------- | ------------------------------------------------------------------------------------------------------ | -------------------- | --------------- | ------ |
| openapiConfig.servers | Comma-separated list of server URLs used by Swagger/OpenAPI | [https://edc-connector-adapter.dev.simpl-europe.eu](https://edc-connector-adapter.dev.simpl-europe.eu) | valid URL(s)         | 1.12.0           | Active |

#### Observability (OpenTelemetry)
###### This section defines telemetry and distributed tracing configuration.
--------------
| Parameter name                      | Description                | Default value                                                                                  | Allowed values/range | Minimum version | Status |
| ----------------------------------- | -------------------------- | ---------------------------------------------------------------------------------------------- | -------------------- | --------------- | ------ |
| openTelemetry.disabled              | Disable OpenTelemetry SDK  | true                                                                                           | true/false           | 1.12.0           | Active |
| openTelemetry.environment           | Logical environment name   | local-dev                                                                                      | string               | 1.12.0           | Active |
| openTelemetry.service               | Logical service identifier | edc-connector-adapter                                                                          | string               | 1.12.0           | Active |
| openTelemetry.otlpExporter.endpoint | OTLP collector endpoint    | [http://collector.common01.dev.simpl-europe.eu](http://collector.common01.dev.simpl-europe.eu) | valid URL            | 1.12.0           | Active |
| openTelemetry.otlpExporter.protocol | OTLP protocol              | http/protobuf                                                                                  | http/protobuf, grpc  | 1.12.0           | Active |
| openTelemetry.propagators           | Context propagation type   | tracecontext                                                                                   | tracecontext         | 1.12.0           | Active |

#### Resource Configuration
###### This section defines Kubernetes resource allocation (CPU and memory requests/limits).
------------------
| Parameter name            | Description    | Default value | Allowed values/range     | Minimum version | Status |
| ------------------------- | -------------- | ------------- | ------------------------ | --------------- | ------ |
| resources.requests.cpu    | CPU request    | 0m            | Kubernetes CPU format    | 1.12.0           | Active |
| resources.requests.memory | Memory request | 0Gi           | Kubernetes memory format | 1.12.0           | Active |
| resources.limits.cpu      | CPU limit      | 1000m         | Kubernetes CPU format    | 1.12.0           | Active |
| resources.limits.memory   | Memory limit   | 2Gi           | Kubernetes memory format | 1.12.0           | Active |

#### Ingress Configuration
###### This section defines ingress exposure and TLS configuration.
------------------
| Parameter name        | Description             | Default value | Allowed values/range | Minimum version | Status |
| --------------------- | ----------------------- | ------------- | -------------------- | --------------- | ------ |
| ingress.enabled       | Enable ingress exposure | false         | true/false           | 1.12.0           | Active |
| ingress.clusterIssuer | Cluster issuer name     | dev-prod      | string               | 1.12.0           | Active |
| ingress.ingressClass  | Ingress class           | nginx         | string               | 1.12.0           | Active |

#### Security Configuration
###### This section defines security-related configuration, including CORS and JWT enforcement.
------------------
| Parameter name                   | Description                        | Default value      | Allowed values/range         | Minimum version | Status |
| -------------------------------- | ---------------------------------- | ------------------ | ---------------------------- | --------------- | ------ |
| web.mvc.cors.allowedOrigins      | Allowed CORS origins               | ""                 | comma-separated list or *    | 1.12.0           | Active |
| web.mvc.bearerToken.required     | Require JWT Bearer token           | true               | true/false                   | 1.12.0           | Active |
| web.mvc.bearerToken.allowedPaths | Paths excluded from JWT validation | /actuator/health/* | comma-separated ant patterns | 1.12.0           | Active |

#### Application Profile Configuration
###### This section defines the operational profile of the adapter.
------------------
| Parameter name | Description         | Default value | Allowed values/range | Minimum version | Status |
| -------------- | ------------------- | ------------- | -------------------- | --------------- | ------ |
| profile        | Application profile | provider      | provider, consumer   | 1.12.0           | Active |


#### Provider Connector Configuration
###### This section defines connectivity to the provider EDC connector (active when profile = provider)
------------------
| Parameter name                   | Description                                         | Default value                                                                                          | Allowed values/range | Minimum version | Status   |
| -------------------------------- | --------------------------------------------------- | ------------------------------------------------------------------------------------------------------ | -------------------- | --------------- | -------- |
| edcConnector.baseUrl             | Provider connector base URL                         | [https://edc-provider.dev.simpl-europe.eu](https://edc-provider.dev.simpl-europe.eu)                   | valid HTTPS URL      | 1.12.0           | Active   |
| edcConnector.tier2BaseUrl        | Tier2 connector URL exposed via TLS gateway         | [https://edc-provider.dev.simpl-europe.eu/protocol](https://edc-provider.dev.simpl-europe.eu/protocol) | valid HTTPS URL      | 1.12.0           | Active   |
| edcConnector.participantId       | Participant identifier used in contract negotiation | provider                                                                                               | string               | 1.12.0           | Active   |
| edcConnector.participantIdSource | External source for participant identifier          | optional                                                                                               | valid URL            | 1.12.0           | Optional |

#### Consumer Profile Configuration
###### This section defines optional parameters used when profile = consumer.
------------------
| Parameter name                      | Description                        | Default value | Allowed values/range | Minimum version | Status   |
| ----------------------------------- | ---------------------------------- | ------------- | -------------------- | --------------- | -------- |
| edcConnector.apiKey                 | API key used by consumer connector | optional      | string               | 1.12.0           | Optional |
| edcConnector.deprovisioning.enabled | Enable deprovisioning requests     | false         | true/false           | 1.12.0           | Optional |


#### Kafka Configuration (Consumer Profile)
###### This section defines Kafka integration when consumer profile is enabled.
------------------
| Parameter name                  | Description                            | Default value                 | Allowed values/range                | Minimum version | Status   |
| ------------------------------- | -------------------------------------- | ----------------------------- | ----------------------------------- | --------------- | -------- |
| kafka.bootstrapServers          | Kafka bootstrap servers                | localhost:9092                | host:port                           | 1.12.0           | Optional |
| kafka.consumer.groupId          | Kafka consumer group ID                | contract_consumption          | string                              | 1.12.0           | Optional |
| kafka.topics.transferProcess    | Kafka topic for transfer process       | contract_consumption.transfer | string                              | 1.12.0           | Optional |
| kafka.auth.type                 | Kafka authentication type              | SASL_PLAINTEXT                | PLAINTEXT, SASL_PLAINTEXT, SASL_SSL | 1.12.0           | Optional |
| kafka.fatalIfBrokerNotAvailable | Fail application if broker unavailable | true                          | true/false                          | 1.12.0           | Optional |


---

## Troubleshooting

- **Java not found**: Ensure Java is installed and `JAVA_HOME` is set.
- **Maven not found**: Install Maven and add it to your PATH.
- **Permission denied (macOS/Linux)**: Run `chmod +x #build_and_release.bat`.
- **Dependency download errors**: Check your internet connection and proxy settings.

---

## Automated Options

- Use the provided `#build_and_release.bat` script for automated build and release.
- For Dockerized deployments, check for a `Dockerfile` or related scripts in the repository.

---

## Version Information

- **edcconnectoradapter**: v1.0.0 or higher
- **Java**: 17+
- **Maven**: 3.8+

# EDC Connector Adapter

> ⚠️ **Modified work — CNIE-ES fork.**
> This repository is **not** the original SIMPL edcconnectoradapter. It is a derivative work based
> on the upstream `1.14.0` development line (commit `0a9cd20`,
> [upstream](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter)),
> modified by the EDNEL-RIOJA project team for CNIE-ES between **2026-05-04 and 2026-09-10** to add
> business logging of contract and transfer records, offering field mappings and participant ping.
> Distributed as `1.14.0-edval` under the **EUPL-1.2**, the same licence as the original work. Full
> details of what was changed and when: [NOTICE.EDNEL.md](NOTICE.EDNEL.md).

> **Purpose**: The EDC Connector Adapter component is designed to provide an abstraction layer between the EDC Connector and the SD-Tooling and ContractConsumption components.
For SD-Tooling, it exposes methods that enable asset registration on the connector, while for ContractConsumption, it provides methods to initiate ContractNegotiation and TransferProcess operations.
The purpose of this module is to facilitate the potential integration of alternative connectors to EDC in the future.

---

## 📑 Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [⚡ Quick Start](#-quick-start)
   - [Use as a Dependency](#use-as-a-dependency)
   - [Run Locally](#run-locally)
4. [Installation guide](#installation--guide)
5. [Configuration](#configuration)
6. [User Guide](#user--guide)
7. [Testing](#testing)
8. [Contributing](#contributing)
9. [Contact & Support](#contact--support)

---

## Overview

The **EDC Connector Adapter** component provides an abstraction layer between the EDC Connector and the SD-Tooling and ContractConsumption components. Its main purpose is to decouple the business logic of Simpl-Open modules from the specific implementation of the EDC Connector, enabling future extensibility and interoperability with alternative connector technologies.

**Key Features**
- Abstraction of connector-specific logic, providing a unified interface for upper-layer components.
- API methods for asset and policies registration on the EDC Connector, exposed to the SD-Tooling component.
- API methods for initiating ContractNegotiation and TransferProcess operations, exposed to the ContractConsumption component.
- Translation and orchestration of requests between Simpl-Open business services and the EDC protocol endpoints.
- Extensibility mechanism allowing future integration of different connector implementations beyond EDC.
- Centralized error handling, logging, and response normalization to ensure consistent communication across modules.

**Relation to other Simpl-Open agents or modules**

The component is deployed as an internal service on both the **Consumer Agent and the Provider Agent**, acting as a mediation layer between higher-level business modules and the respective EDC Connectors.
On the Consumer Agent side, it intermediates communication **between the ContractConsumption BE and the EDC Consumer Connector**, managing the initiation and coordination of negotiation and data transfer processes.
On the Provider Agent side, it serves as the integration layer **between the SD-Tooling component and the EDC Provider Connector**, handling asset registration and publication operations.
This dual deployment ensures consistent interaction patterns across both agents, promotes connector-agnostic integration, and supports long-term maintainability, interoperability, and compliance within the Simpl-Open ecosystem.

---

## Prerequisites

```bash
Java 21+
Maven 3.9+
Access to EU GitLab Package Registry (for repo declared in POM file)
IDE with plugin Lombok enabled (IntelliJ/Eclipse/VS Code)
Enabled connectivity with Kafka cluster
Enabled connectivity with EDC Consumer/Provider Connector
Enabled connectivity with Security Vault
```

---

## ⚡ Quick Start

## Installation Guide

The instructions for running the application locally can be found in the following file → [Installation Guide](documents/installation-guide/Installation%20Guide.md)

---

## Configuration

The instructions for setting config parameters can be found in the following file → [Configuration Parameters](documents/installation-guide/Installation%20Guide.md#configuration)

---

## Deployment Guide

The instructions for setting up configuration and deploy in Kubernetes cluster can be found in the following file → [Deployment Guide](documents/deployment-guide/Deployment%20Guide.md)

---

## Upgrade Guide

At the following link, you can find the guide that outlines the changes made in the latest version, including configuration updates, integrations with other systems, and new or modified functionalities, to facilitate the setup of the application within the target environment. → [Upgrade Guide](documents/upgrade-guide/Upgrade%20Guide.md)

---

## Testing

Testing is covered through the CI/CD pipeline associated with the GIT repository.
This pipeline automatically runs Unit Tests, SAST (Static Application Security Testing) using SonarQube, and security tests performed with Fortify.

---

## Contributing

At the following link, you can find all the information related to the delivery process management adopted for Simpl-Open across its various components.
[Release Management](https://confluence.simplprogramme.eu/display/SIMPL/2050+-+Release+mgnt)

---

## Contact & Support

- **Maintainers**: `Data1 Team`

---

## Licence

The original work, edcconnectoradapter, is © European Union / SIMPL Programme and is licensed under
the **European Union Public Licence v. 1.2 (EUPL-1.2)**, whose full official text is reproduced in
[LICENSE](LICENSE). Third-party components included in the product are listed in [NOTICE](NOTICE) /
[NOTICE.json](NOTICE.json) / [THIRD_PARTY_LICENCES.md](THIRD_PARTY_LICENCES.md).

This fork is a **modified version** of that work, distributed under the same licence. The
modification notice required by Art. 5 of the EUPL (what was modified, by whom and when) is in
[NOTICE.EDNEL.md](NOTICE.EDNEL.md). The complete corresponding source code, including the revision
history, is available at https://github.com/cnie-es/simpl-edc-connector-adapter.

---

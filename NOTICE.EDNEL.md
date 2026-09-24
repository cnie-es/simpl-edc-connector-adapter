# Modification notice (EUPL 1.2, Art. 5)

**This is a modified version of SIMPL edcconnectoradapter. It is not the original work.**

The original work, edcconnectoradapter, is part of the SIMPL programme (© European Union / SIMPL
Programme) and is licensed under the **European Union Public Licence v. 1.2 (EUPL-1.2)**. See
[LICENSE](LICENSE) for the licence of the work and [NOTICE](NOTICE) / [NOTICE.json](NOTICE.json) /
[THIRD_PARTY_LICENCES.md](THIRD_PARTY_LICENCES.md) for the third-party components included in it.
All original copyright, licence and disclaimer notices are kept intact and unmodified in this fork.

## Upstream baseline

| | |
|---|---|
| Original work | edcconnectoradapter (`eu.europa.ec.simpl.sdtooling:edc-connector-adapter`) |
| Upstream repository | https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter |
| Baseline version | `1.14.0` development line |
| Baseline commit | `0a9cd200e492490ba06b5fb58e09841ef8852728` (2026-03-27) |

## Modifications

| | |
|---|---|
| Modified by | EDNEL-RIOJA project team, for CNIE-ES |
| Public repository of this derivative work | https://github.com/cnie-es/simpl-edc-connector-adapter |
| Version of this derivative work | `1.14.0-edval` |
| Dates of modification | **2026-05-04 to 2026-09-10** |

The modifications are licensed under the **EUPL-1.2**, the same licence as the original work.

The complete source code of this derivative work is available at the public repository above as a
vetted release snapshot, and will remain freely available there for as long as the Work is
distributed. The upstream repository and exact baseline commit are recorded above. Each release
snapshot includes `SBOM.cyclonedx.json`, which binds the upstream and work revisions, release tag,
public repository, snapshot hash and image digest. The distribution history contains release
snapshots rather than a copy of the upstream Git history, so the complete set of changes is the
diff between the baseline commit `0a9cd20`, fetched from its authoritative upstream repository,
and the published snapshot. The tables below record what each file contributed. The published
container images (`ghcr.io/cnie-es/data1-edcconnectoradapter`) are built from that snapshot.

### Summary of the changes

- **Business logging of contract and transfer records**: new `ContractBusinessLogger`,
  `TransferBusinessLogger` and `ContractRecordEmitter`, with the `ContractRecordLog`,
  `TransferRecordLog` and `EdcContractAgreement` models and the `ContractBusinessOperation` /
  `TransferBusinessOperation` constants, so that contract negotiation and transfer status reach the
  logs and Kafka for the Clearing House.
- **Offering field mapping**: additional descriptive fields taken from the general service
  properties and exposed for frontend usage, with the mappings adjusted to support string values.
- **Participant ping**: new `ParticipantPingClient`, so that `providerName` is no longer lost after
  a failed participant ping.
- **Transfer request fix**: `-PUSH` is no longer appended when the transfer request already ends
  with `-PULL`.
- `pom.xml`: the project version moved from `${env.PROJECT_RELEASE_VERSION}` to `${revision}`, as
  Maven 4 requires a non-environment expression there.
- A **GitHub Actions pipeline** that builds and publishes the container image.

A second, non-functional group of changes (2026-09-10) adds the notices this licence requires of a
derivative work: this file, the notice at the top of [README.md](README.md), the licence and
source-code metadata in `pom.xml` and in the OCI labels of the `Dockerfile`, and the reproduction of
the full official licence text inside [LICENSE](LICENSE), which previously only linked to it. See
[CHANGELOG.md](CHANGELOG.md) for the itemised list.

### Files added

| File | Date |
|---|---|
| `.github/workflows/build-push.yml` | 2026-05-04 (updated 2026-09-10) |
| `src/test/resources/test/corpus-offering-payload.json` | 2026-05-07 |
| `src/test/resources/test/lcr-offering-payload.json` | 2026-05-07 |
| `src/test/resources/test/model-offering-payload.json` | 2026-05-07 |
| `src/main/java/eu/europa/ec/simpl/edcconnectoradapter/constant/TransferBusinessOperation.java` | 2026-06-17 |
| `src/main/java/eu/europa/ec/simpl/edcconnectoradapter/logging/TransferBusinessLogger.java` | 2026-06-17 |
| `src/main/java/eu/europa/ec/simpl/edcconnectoradapter/model/edc/transfer/TransferRecordLog.java` | 2026-06-17 |
| `src/main/java/eu/europa/ec/simpl/edcconnectoradapter/client/authprovider/ParticipantPingClient.java` | 2026-06-22 |
| `src/main/java/eu/europa/ec/simpl/edcconnectoradapter/constant/ContractBusinessOperation.java` | 2026-06-22 |
| `src/main/java/eu/europa/ec/simpl/edcconnectoradapter/logging/ContractBusinessLogger.java` | 2026-06-22 |
| `src/main/java/eu/europa/ec/simpl/edcconnectoradapter/logging/ContractRecordEmitter.java` | 2026-06-22 |
| `src/main/java/eu/europa/ec/simpl/edcconnectoradapter/model/edc/contract/ContractRecordLog.java` | 2026-06-22 |
| `src/main/java/eu/europa/ec/simpl/edcconnectoradapter/model/edc/contract/response/EdcContractAgreement.java` | 2026-06-22 |
| `src/test/java/eu/europa/ec/simpl/edcconnectoradapter/client/authprovider/ParticipantPingClientTest.java` | 2026-09-02 |
| `NOTICE.EDNEL.md` (this file) | 2026-09-10, 2026-09-14 |

### Files modified

| File | Date |
|---|---|
| `pom.xml` | 2026-05-07, 2026-09-10 |
| `src/main/java/eu/europa/ec/simpl/edcconnectoradapter/service/provider/registration/RegistrationServiceImpl.java` | 2026-05-04, 2026-05-07, 2026-05-13 |
| `src/test/java/eu/europa/ec/simpl/edcconnectoradapter/service/provider/registration/RegistrationServiceTest.java` | 2026-05-04, 2026-05-07, 2026-05-13 |
| `src/main/java/eu/europa/ec/simpl/edcconnectoradapter/service/consumer/transferprocess/TransferProcessServiceImpl.java` | 2026-05-25, 2026-06-17, 2026-06-22, 2026-09-02 |
| `src/main/resources/application-consumer.properties` | 2026-06-17, 2026-06-22 |
| `src/main/java/eu/europa/ec/simpl/edcconnectoradapter/service/consumer/transferprocess/TransferProcessServiceKafkaImpl.java` | 2026-06-17, 2026-06-22 |
| `src/test/java/eu/europa/ec/simpl/edcconnectoradapter/service/consumer/transferprocess/TransferProcessServiceKafkaTest.java` | 2026-06-17, 2026-06-22 |
| `src/test/java/eu/europa/ec/simpl/edcconnectoradapter/service/consumer/transferprocess/TransferProcessServiceTest.java` | 2026-06-17, 2026-06-22 |
| `src/main/java/eu/europa/ec/simpl/edcconnectoradapter/client/edcconnector/EDCConnectorContractClient.java` | 2026-06-22 |
| `src/main/java/eu/europa/ec/simpl/edcconnectoradapter/service/consumer/contractnegotiation/ContractNegotiationService.java` | 2026-06-22 |
| `src/test/java/eu/europa/ec/simpl/edcconnectoradapter/service/consumer/contractnegotiation/ContractNegotiationServiceTest.java` | 2026-06-22 |
| `src/main/java/eu/europa/ec/simpl/edcconnectoradapter/service/consumer/contractnegotiation/ContractNegotiationServiceImpl.java` | 2026-06-22, 2026-09-02 |
| `src/main/resources/application.properties` | 2026-09-02 |
| `charts/templates/deployment.yaml` | 2026-09-02, 2026-09-10 |
| `charts/values.yaml` | 2026-09-02, 2026-09-10 |
| `Dockerfile` | 2026-09-10, 2026-09-14 |
| `LICENSE` | 2026-09-10 |
| `README.md` | 2026-09-10 |
| `CHANGELOG.md` | 2026-09-10 |
| `charts/Chart.yaml` | 2026-09-10 |
| `pipeline.variables.sh` | 2026-09-10 |

No file of the original work has been removed, and no copyright, licence or disclaimer notice of
the original work has been altered. The only change to [LICENSE](LICENSE) is the addition, below
the original heading, of the full official text of the EUPL-1.2 that the file previously referenced
only by hyperlink; nothing in the original file was removed or reworded.

The per-file diff for every change is obtainable with:

```
git diff 0a9cd200e492490ba06b5fb58e09841ef8852728..ednel
```

For a published image, substitute the release tag it was built from for `ednel`.

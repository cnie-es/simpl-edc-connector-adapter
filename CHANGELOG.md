## 1.14.0-edval (2026-09-10)

> Derivative work by the **EDNEL-RIOJA** project team for **CNIE-ES**, based on the upstream
> edcconnectoradapter `1.14.0` development line (commit `0a9cd20`). Modified between
> **2026-05-04 and 2026-09-10**, licensed under EUPL-1.2 like the original work. See
> [NOTICE.EDNEL.md](NOTICE.EDNEL.md) for the full modification notice.

### Added (2026-05-04 → 2026-09-02)

- **Business logging of contract and transfer records**: `ContractBusinessLogger`,
  `TransferBusinessLogger` and `ContractRecordEmitter`, with the `ContractRecordLog`,
  `TransferRecordLog` and `EdcContractAgreement` models and the `ContractBusinessOperation` /
  `TransferBusinessOperation` constants, so that contract negotiation and transfer status reach the
  logs and Kafka for the Clearing House.
- **Participant ping**: `ParticipantPingClient`, with its unit test, so that `providerName` is no
  longer lost after a failed participant ping.
- Offering payload fixtures for the tests (`corpus`, `lcr` and `model`).
- GitHub Actions pipeline building and publishing the container image.

### Changed (2026-05-04 → 2026-09-02)

- Additional descriptive fields taken from the general service properties and exposed for frontend
  usage, with the mappings adjusted to support string values.
- `-PUSH` is no longer appended when the transfer request already ends with `-PULL`.
- `pom.xml`: the project version moved from `${env.PROJECT_RELEASE_VERSION}` to `${revision}`, as
  Maven 4 requires a non-environment expression there.

### Licence compliance (2026-09-10)

Notices required by Art. 5 of the EUPL-1.2 (Attribution right, Provision of Source Code) for this
derivative work:

- `NOTICE.EDNEL.md`: modification notice stating that the work has been modified, by whom, when and
  what was changed, with the repository where the complete corresponding source code is available.
- `README.md`: prominent notice at the top of the file identifying this repository as a modified
  version of edcconnectoradapter, plus a Licence section.
- `LICENSE`: the full official text of the EUPL-1.2 is now reproduced in the file, which previously
  only linked to it, so that a copy of the Licence travels with every copy of the Work. The
  original SIMPL heading is kept intact.
- `Dockerfile`: OCI image labels (`licenses`, `source`, `vendor`, `description`) and `LICENSE`,
  `NOTICE` and `NOTICE.EDNEL.md` copied into `/licenses/`, so the notices and the pointer to the
  source code travel with the published container image.
- `pom.xml`: the project coordinates move from `eu.europa.ec.simpl.sdtooling:edc-connector-adapter`
  to `es.cnie.simpl.sdtooling:simpl-edc-connector-adapter`, so that a modified artifact is not
  identified under a namespace belonging to the licensor (Art. 5, Legal Protection); `licenses`,
  `scm`, `url` and `developers` metadata filled in. The `${revision}` version mechanism is kept: it
  is functional and pinning a version would break the release build.
- `.github/workflows/build-push.yml`: the container image namespace is derived from the repository
  owner instead of being hard-coded, so that the published image and the source code it is built
  from always live in the same organisation, and the project version is published as an image tag
  so the tag the chart resolves to by default exists in the registry.
- The release version reaches the Maven build: the `Dockerfile` builder stage takes
  `PROJECT_RELEASE_VERSION` and passes it as `-Drevision`, which the POM resolves its version from
  (2026-09-10). Without it the jar inside the image declared the `0.0.1-SNAPSHOT` local default
  while the image was tagged and labelled with the real version, and any SBOM derived from the
  image would have reported the placeholder.
- Helm chart made loadable outside the upstream GitLab pipeline: `Chart.yaml` and `values.yaml`
  carried unsubstituted `${PROJECT_RELEASE_VERSION}` and `${CI_REGISTRY_IMAGE}` placeholders, which
  that pipeline replaced and which made the chart fail to load anywhere else. `image.repository` and
  `image.tag` now have working defaults, overridable at install time, and optional
  `imagePullSecrets` are added, as the published image is private.
- The version of this fork, `1.14.0-edval`, is stated consistently in the chart `version` and
  `appVersion` and in `pipeline.variables.sh`, which the image copies in and which reports the
  running version; both still declared the upstream `1.14.0`.
- Fixed an inherited chart defect while making it loadable: the deployment template read
  `.Values.pullPolicy` from the root while `values.yaml` defines `image.pullPolicy`, so the
  declared pull policy was silently dropped and `imagePullPolicy` rendered empty. Present in the
  upstream baseline, not introduced by this fork.


## 1.6.0 (2025-09-04)

### added (1 change)

- [[SIMPL-17313](https://jira.simplprogramme.eu/browse/SIMPL-17313) Create...](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/ed7fc24a27e16cd041ff5b248f9e9b043a17d323) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/30))

### fixed (1 change)

- [[SIMPL-8258](https://jira.simplprogramme.eu/browse/SIMPL-8258) Extract](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/64d0ce21ccd4db9a3497dc3ac42db5bb648b3c19) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/30))



## 1.5.1 (2025-08-07)

### fixed (3 changes)

- [[SIMPL-16125](https://jira.simplprogramme.eu/browse/SIMPL-16125) Entry/Acceptance Criteria Report](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/e37bbc6d2651190e236184eae237252949a851b6) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/28))
- [[SIMPL-16125](https://jira.simplprogramme.eu/browse/SIMPL-16125) Entry/Acceptance Criteria Report](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/a476db01d5fe1ec7349b45167eb85f3e429f3e28) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/28))
- [[SIMPL-16125](https://jira.simplprogramme.eu/browse/SIMPL-16125) Entry/Acceptance Criteria Report](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/b157a9f643fde31b6aaa59accd90f507c8a0aca2) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/28))

### changed (1 change)

- [[SIMPL-16125](https://jira.simplprogramme.eu/browse/SIMPL-16125)](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/2de03a26c90f7fd570da7f94117d33af899cacf6) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/28))


## 1.5.0 (2025-08-01)

### fixed (3 changes)

- [[SIMPL-15891](https://jira.simplprogramme.eu/browse/SIMPL-15891) Hardcoded values in ingresses.](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/f81230dea7afc26540144bc74dff8105967f763b) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/22))
- [[SIMPL-14722](https://jira.simplprogramme.eu/browse/SIMPL-14722) Resolve](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/27b2079c64b6b11bee4b798efee88e19ecd081d3) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/22))
- [[SIMPL-4720](https://jira.simplprogramme.eu/browse/SIMPL-4720) Update](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/f5624f9ad6b10bdafa4e7035b66282c8b7b6a146) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/22))


## 1.4.0 (2025-07-11)

### added (1 change)

- [[SIMPL-10304](https://jira.simplprogramme.eu/browse/SIMPL-10304) Extend the...](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/ada15065d9b5def37335b1a51d5533b4d1afd678) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/18))

### fixed (1 change)

- [[SIMPL-14116](https://jira.simplprogramme.eu/browse/SIMPL-14116) Resolve](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/640ce356669a665c1311fa5ee3ee454397a75040) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/18))


## 1.3.0 (2025-06-19)

### fixed (1 change)

- [fixed RegistationControlle register() error handling for missing](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/0bed359fc9f4b79114d47ee380aab87400bfd208) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/16))

### added (2 changes)

- [added new key in values.yaml to specify a different service name in open](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/b91ccc6f3bb75decd871257762b5e20e8ea79a07) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/16))
- [[SIMPL-13521](https://jira.simplprogramme.eu/browse/SIMPL-13521) Added ArgoCD manifests.](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/9a2644a1d691505268b54e8d2c00a1cce8211b42) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/16))

### changed (2 changes)

- [error responses aligned to belgif problem model](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/8e1a82c3e4c20b96e2ad9a2e6d06b3b3d886ac6c) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/16))
- [aligned to simpl-data1-common version 1.1.0 to support belgif problem](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/0768af7de07c21980c23626059ba05b9c93bcacd) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/16))


## 1.2.0 (2025-05-29)

### added (2 changes)

- [[SIMPL-12727](https://jira.simplprogramme.eu/browse/SIMPL-12727)](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/4825fe7969321cb55d5309d47a93e3ee2b5602f7) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/14))
- [[SIMPL-12999](https://jira.simplprogramme.eu/browse/SIMPL-12999) Config](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/f947b11e05cdca9dbf95a87fd8e992349f023963) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/14))


## 1.1.2 (2025-05-09)

### added (1 change)

- [[SIMPL-12186](https://jira.simplprogramme.eu/browse/SIMPL-12186) Enable](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/011dabe5fa7081460420edfe9e54111ea8de9af8) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/11))


## 1.1.1 (2025-05-08)

No changes.


## 1.1.0 (2025-05-07)

### changed (2 changes)

- [[SIMPL-12218](https://jira.simplprogramme.eu/browse/SIMPL-12218)](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/35722bad7fbd6840eadeac77818fcce07f29de2e) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/7))
- [[SIMPL-12726](https://jira.simplprogramme.eu/browse/SIMPL-12726)](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/5c07cf0ec248609eec11390f96c8e97517f5ac49) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/7))


## 1.0.2 (2025-04-16)

### fixed (2 changes)

- [fixed caching](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/159fb062d245771a659d99aa4e957a764b90907a) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/5))
- [fixed version value at startup](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/59f6d4268dcfe6b8df9f61d063cd05a720fae390) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/5))


## 1.0.1 (2025-04-16)

### changed (1 change)

- [logging improved](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/commit/25b2a3d07e93a4856e9bcef60b85d64771b0b983) ([merge request](https://code.europa.eu/simpl/simpl-open/development/data1/edcconnectoradapter/-/merge_requests/3))


## 1.0.0 (2025-04-15)

No changes.


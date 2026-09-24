# Upgrade Guide

## Purpose
This guide helps to upgrade the `edc-connector-adapter` release from the previous one.

---

## Breaking Changes
- requires OpenBao Vault when `profile: consumer`
- profile `consumerNoKafka` has been removed: for the consumer participant in values.yaml change profile from `consumerNoKafka` to `consumer`
- values.yaml keys removed:
    - topics.transferProcess:
- values.yaml keys added:
    - kafka.transfer-status.topic
    - kafka.transfer-deprovisioning.topic
    
---


## Upgrade Process
1. **Read the release notes** for the target version.
2. **Stop the running service**:
    - On Windows: `net stop <service-name>` or stop via your process manager.
3. **Backup** as described above.
4. **Update the application files**:
    - Replace the old deployment package with the new one.
    - If using Maven, run:
      ```
      mvn clean install
      ```
5. **Restart the service**:
    - On Windows: `net start <service-name>`
6. **Verify the application** is running as expected.

---

## Rollback Instructions
If the upgrade fails or issues are detected:
1. **Stop the service.**
2. **Install the previous version.**
3. **Restart the service.**
4. **Verify the application** is working as before.

---

## Migration Scripts
- no migration scripts required for this release.

---

## Additional Recommendations
- Test the upgrade in a staging environment before production.
- Review logs after upgrade for errors or warnings.

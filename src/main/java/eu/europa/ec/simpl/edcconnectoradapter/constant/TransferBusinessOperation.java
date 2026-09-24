package eu.europa.ec.simpl.edcconnectoradapter.constant;

/**
 * Business operations emitted as structured technical logs for transfer processes. These logs are collected into
 * Elasticsearch (technical-logs-*) and consumed by the Clearing House, mirroring the contract module pattern that
 * emits {@code contractAgreementCreateTO}.
 */
public enum TransferBusinessOperation {
    TRANSFER_INITIATED("TRANSFER_INITIATED"),
    TRANSFER_STATE_CHANGED("TRANSFER_STATE_CHANGED");

    private final String description;

    TransferBusinessOperation(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}

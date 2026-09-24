package eu.europa.ec.simpl.edcconnectoradapter.constant;

/**
 * Business operations emitted as structured business logs for contract negotiations. These records are collected into
 * Elasticsearch (via a dedicated Kafka topic, into {@code contract-records-*}) and consumed by the Clearing House,
 * mirroring {@link TransferBusinessOperation} for the transfer flow.
 */
public enum ContractBusinessOperation {
    CONTRACT_NEGOTIATION_INITIATED("CONTRACT_NEGOTIATION_INITIATED"),
    CONTRACT_STATE_CHANGED("CONTRACT_STATE_CHANGED");

    private final String description;

    ContractBusinessOperation(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}

package eu.europa.ec.simpl.edcconnectoradapter.util;

import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ContractNegotiation;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ContractNegotiationId;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferProcess;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferProcessId;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.common.response.EdcAcknowledgementId;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract.response.EdcContractNegotiation;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.transfer.response.EdcTransferProcess;

public final class ModelUtil {

    private ModelUtil() {}

    public static ContractNegotiation transform(EdcContractNegotiation source) {
        return ContractNegotiation.builder()
                .contractAgreementId(source.getContractAgreementId())
                .contractNegotiationId(source.getContractNegotiationId())
                .counterPartyAddress(source.getCounterPartyAddress())
                .counterPartyId(source.getCounterPartyId())
                .createdAt(source.getCreatedAt())
                .errorDetail(source.getErrorDetail())
                .protocol(source.getProtocol())
                .state(source.getState())
                .type(source.getType())
                .build();
    }

    public static TransferProcess transform(EdcTransferProcess source) {
        return TransferProcess.builder()
                .assetId(source.getAssetId())
                .contractId(source.getContractId())
                .correlationId(source.getCorrelationId())
                .errorDetail(source.getErrorDetail())
                .finalState(source.getFinalState())
                .state(source.getState())
                .stateTimestamp(source.getStateTimestamp())
                .transferProcessId(source.getTransferProcessId())
                .transferType(source.getTransferType())
                .type(source.getType())
                .build();
    }

    public static ContractNegotiationId toContractNegotiationId(EdcAcknowledgementId source) {
        return ContractNegotiationId.builder().negotiationId(source.getId()).build();
    }

    public static TransferProcessId toTransferProcessId(EdcAcknowledgementId source) {
        return TransferProcessId.builder().transferId(source.getId()).build();
    }
}

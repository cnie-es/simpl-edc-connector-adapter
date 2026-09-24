package eu.europa.ec.simpl.edcconnectoradapter.service.consumer.contractnegotiation;

import eu.europa.ec.simpl.data1.common.adapter.connector.model.catalog.CatalogSearchResult;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ContractNegotiation;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ContractNegotiationId;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ContractNegotiationRequest;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract.response.EdcContractAgreement;

public interface ContractNegotiationService {

    CatalogSearchResult getCatalog(ContractNegotiationRequest request);

    ContractNegotiationId initiateContractNegotiation(ContractNegotiationRequest request);

    ContractNegotiation getContractNegotiation(String negotiationId);

    EdcContractAgreement getContractAgreement(String agreementId);
}

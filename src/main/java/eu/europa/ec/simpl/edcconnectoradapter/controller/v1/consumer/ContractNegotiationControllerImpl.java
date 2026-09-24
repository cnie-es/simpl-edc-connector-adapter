package eu.europa.ec.simpl.edcconnectoradapter.controller.v1.consumer;

import eu.europa.ec.simpl.data1.common.adapter.connector.model.catalog.CatalogSearchResult;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ContractNegotiation;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ContractNegotiationId;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ContractNegotiationRequest;
import eu.europa.ec.simpl.data1.common.controller.AbstractController;
import eu.europa.ec.simpl.data1.common.logging.LogRequest;
import eu.europa.ec.simpl.edcconnectoradapter.service.consumer.contractnegotiation.ContractNegotiationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Profile({"consumer", "build", "test"})
@RestController("contractNegotiationControllerV1")
@Log4j2
@RequiredArgsConstructor
public class ContractNegotiationControllerImpl extends AbstractController implements ContractNegotiationController {

    private final ContractNegotiationService contractNegotiationService;

    @LogRequest
    @Override
    public ResponseEntity<CatalogSearchResult> searchCatalogOffers(ContractNegotiationRequest request) {
        log.debug("searchCatalogOffers(): invoking contractNegotiationService.getCatalog() with {}", request);
        CatalogSearchResult catalogSearchResult = contractNegotiationService.getCatalog(request);

        log.debug("searchCatalogOffers(): returning response OK with {}", catalogSearchResult);
        return ResponseEntity.ok(catalogSearchResult);
    }

    @LogRequest
    @Override
    public ResponseEntity<ContractNegotiationId> startContractNegotiation(ContractNegotiationRequest request) {
        log.debug(
                "startContractNegotiation(): invoking contractNegotiationService.initiateContractNegotiation() with {}",
                request);
        ContractNegotiationId negotiationIdResponse = contractNegotiationService.initiateContractNegotiation(request);

        log.debug("startContractNegotiation(): returning response OK with {}", negotiationIdResponse);
        return ResponseEntity.ok(negotiationIdResponse);
    }

    @LogRequest
    @Override
    public ResponseEntity<ContractNegotiation> getContractNegotiationStatus(String contractNegotiationId) {
        log.debug(
                "getContractNegotiationStatus(): invoking contractNegotiationService.getContractNegotiation() with  contractNegotiationId {}",
                contractNegotiationId);
        ContractNegotiation contractNegotiation =
                contractNegotiationService.getContractNegotiation(contractNegotiationId);

        log.debug("getContractNegotiationStatus(): returning response OK with {}", contractNegotiation);
        return ResponseEntity.ok(contractNegotiation);
    }
}

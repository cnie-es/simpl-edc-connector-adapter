package eu.europa.ec.simpl.edcconnectoradapter.service.consumer.contractnegotiation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.catalog.CatalogSearchResult;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ContractNegotiation;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ContractNegotiationId;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ContractNegotiationRequest;
import eu.europa.ec.simpl.data1.common.exception.RemoteServiceErrorException;
import eu.europa.ec.simpl.edcconnectoradapter.JacksonObjectValuesMapperUtil;
import eu.europa.ec.simpl.edcconnectoradapter.TestConstants;
import eu.europa.ec.simpl.edcconnectoradapter.client.authprovider.ParticipantPingClient;
import eu.europa.ec.simpl.edcconnectoradapter.client.edcconnector.EDCConnectorCatalogClient;
import eu.europa.ec.simpl.edcconnectoradapter.client.edcconnector.EDCConnectorContractClient;
import eu.europa.ec.simpl.edcconnectoradapter.logging.ContractRecordEmitter;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.common.response.EdcAcknowledgementId;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract.response.EdcContractAgreement;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract.response.EdcContractNegotiation;
import eu.europa.ec.simpl.edcconnectoradapter.service.catalogmapper.CatalogMapperService;
import eu.europa.ec.simpl.edcconnectoradapter.service.catalogmapper.CatalogMapperServiceImpl;
import eu.europa.ec.simpl.edcconnectoradapter.service.policymapper.PolicyMapperService;
import eu.europa.ec.simpl.edcconnectoradapter.service.policymapper.PolicyMapperServiceImpl;
import eu.europa.ec.simpl.edcconnectoradapter.util.TestUtil;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ContractNegotiationServiceTest {

    private static final String CONTRACT_NEGOTIATION_ID = "contr-neg-id";
    private static final String CONTRACT_AGREEMENT_ID = "contr-agr-id";

    private static final String EDC_CONNECTOR_BASE_URL = "http://connectorUrl";

    @Mock
    private EDCConnectorContractClient edcConnectorContractClient;

    @Mock
    private EDCConnectorCatalogClient edcConnectorCatalogClient;

    @Mock
    private ParticipantPingClient participantPingClient;

    @Mock
    private ContractRecordEmitter contractRecordEmitter;

    // using the impl type to access protected fiels and methods
    private ContractNegotiationServiceImpl contractNegotiationService;

    @BeforeEach
    void setUpEach() {
        PolicyMapperService policyMapperService = new PolicyMapperServiceImpl();
        CatalogMapperService catalogMapperService = new CatalogMapperServiceImpl(policyMapperService);
        contractNegotiationService = new ContractNegotiationServiceImpl(
                edcConnectorCatalogClient,
                edcConnectorContractClient,
                catalogMapperService,
                participantPingClient,
                contractRecordEmitter);
        ReflectionTestUtils.setField(contractNegotiationService, "edcConnectorBaseUrl", EDC_CONNECTOR_BASE_URL);
        ReflectionTestUtils.setField(contractNegotiationService, "edcConnectorApiKeyValue", "apikeydefaultvalue");
    }

    @Test
    void testInit() {
        contractNegotiationService.init();
        assertEquals(
                EDC_CONNECTOR_BASE_URL + "/management", contractNegotiationService.edcConsumerManagementUrl.toString());
    }

    @Test
    void testGetCatalog() throws JsonProcessingException {
        ContractNegotiationRequest request = ContractNegotiationRequest.builder()
                .providerEndpoint("http://providerUrl")
                .contractDefinitionId(TestConstants.CATALOG_RESPONSE_ONE_OFFER_CONTRACT_DEFINITION_ID)
                .assetId("asset-test")
                .build();

        when(edcConnectorCatalogClient.requestCatalog(any(), any(), any()))
                .thenReturn(
                        JacksonObjectValuesMapperUtil.mapStringToObjectNode(TestConstants.CATALOG_RESPONSE_ONE_OFFER));

        CatalogSearchResult result = contractNegotiationService.getCatalog(request);
        assertNotNull(result);
        assertNotNull(result.getDatasets());
        assertTrue(result.getDatasets().size() > 0);
    }

    @Test
    void testGetCatalogWithEmptyResponse() throws JsonProcessingException {
        ContractNegotiationRequest request = ContractNegotiationRequest.builder()
                .providerEndpoint("http://providerUrl")
                .contractDefinitionId("contract-def-test")
                .assetId("asset-test")
                .build();

        when(edcConnectorCatalogClient.requestCatalog(any(), any(), any()))
                .thenReturn(JacksonObjectValuesMapperUtil.mapStringToObjectNode(TestConstants.EMPTY_CATALOG_RESPONSE));

        CatalogSearchResult result = contractNegotiationService.getCatalog(request);
        assertNotNull(result);
        assertNotNull(result.getDatasets());
        assertEquals(0, result.getDatasets().size());
    }

    @Test
    void testGetCatalogWithFeignException() {
        ContractNegotiationRequest request = ContractNegotiationRequest.builder()
                .providerEndpoint("http://providerUrl")
                .contractDefinitionId(TestConstants.CATALOG_RESPONSE_ONE_OFFER_CONTRACT_DEFINITION_ID)
                .assetId("asset-test")
                .build();

        when(edcConnectorCatalogClient.requestCatalog(any(), any(), any()))
                .thenThrow(FeignException.Unauthorized.class);

        assertThrows(RemoteServiceErrorException.class, () -> contractNegotiationService.getCatalog(request));
    }

    @Test
    void testInitiateContractNegotiation() throws JsonProcessingException {
        ContractNegotiationRequest request = ContractNegotiationRequest.builder()
                .providerEndpoint("http://providerUrl")
                .contractDefinitionId(TestConstants.CATALOG_RESPONSE_ONE_OFFER_CONTRACT_DEFINITION_ID)
                .assetId("asset-test")
                .build();

        JsonNode providerCatalog =
                JacksonObjectValuesMapperUtil.mapStringToObjectNode(TestUtil.CATALOG_RESPONSE_ONE_OFFER);
        when(edcConnectorCatalogClient.requestCatalog(any(), any(), any())).thenReturn(providerCatalog);

        when(edcConnectorContractClient.initiateContractNegotiation(any(), any(), any()))
                .thenReturn(EdcAcknowledgementId.builder()
                        .id(CONTRACT_NEGOTIATION_ID)
                        .build());

        ContractNegotiationId result = contractNegotiationService.initiateContractNegotiation(request);
        assertNotNull(result);
        assertEquals(CONTRACT_NEGOTIATION_ID, result.getNegotiationId());
    }

    @Test
    void testInitiateContractNegotiationWithFeignException() throws JsonProcessingException {
        ContractNegotiationRequest request = ContractNegotiationRequest.builder()
                .providerEndpoint("http://providerUrl")
                .contractDefinitionId(TestConstants.CATALOG_RESPONSE_ONE_OFFER_CONTRACT_DEFINITION_ID)
                .assetId("asset-test")
                .build();

        JsonNode providerCatalog =
                JacksonObjectValuesMapperUtil.mapStringToObjectNode(TestUtil.CATALOG_RESPONSE_ONE_OFFER);
        when(edcConnectorCatalogClient.requestCatalog(any(), any(), any())).thenReturn(providerCatalog);

        when(edcConnectorContractClient.initiateContractNegotiation(any(), any(), any()))
                .thenThrow(FeignException.Unauthorized.class);

        assertThrows(
                RemoteServiceErrorException.class,
                () -> contractNegotiationService.initiateContractNegotiation(request));
    }

    @Test
    void testGetContractNegotiation() {

        when(edcConnectorContractClient.getContractNegotiation(any(), any(), any()))
                .thenReturn(EdcContractNegotiation.builder()
                        .contractNegotiationId(CONTRACT_NEGOTIATION_ID)
                        .contractAgreementId(CONTRACT_AGREEMENT_ID)
                        .build());

        ContractNegotiation response = contractNegotiationService.getContractNegotiation(CONTRACT_NEGOTIATION_ID);
        assertNotNull(response);
        assertEquals(CONTRACT_NEGOTIATION_ID, response.getContractNegotiationId());
        assertEquals(CONTRACT_AGREEMENT_ID, response.getContractAgreementId());
    }

    @Test
    void testGetContractNegotiationWithFeignException() {

        when(edcConnectorContractClient.getContractNegotiation(any(), any(), any()))
                .thenThrow(FeignException.Unauthorized.class);

        assertThrows(
                RemoteServiceErrorException.class,
                () -> contractNegotiationService.getContractNegotiation(CONTRACT_NEGOTIATION_ID));
    }

    @Test
    void testGetContractAgreement() {

        when(edcConnectorContractClient.getContractAgreement(any(), any(), any()))
                .thenReturn(EdcContractAgreement.builder()
                        .contractAgreementId(CONTRACT_AGREEMENT_ID)
                        .providerId("urn:participant:provider")
                        .consumerId("urn:participant:consumer")
                        .assetId("asset-1")
                        .build());

        EdcContractAgreement response = contractNegotiationService.getContractAgreement(CONTRACT_AGREEMENT_ID);
        assertNotNull(response);
        assertEquals(CONTRACT_AGREEMENT_ID, response.getContractAgreementId());
        assertEquals("urn:participant:provider", response.getProviderId());
        assertEquals("urn:participant:consumer", response.getConsumerId());
    }

    @Test
    void testGetContractAgreementWithFeignException() {

        when(edcConnectorContractClient.getContractAgreement(any(), any(), any()))
                .thenThrow(FeignException.Unauthorized.class);

        assertThrows(
                RemoteServiceErrorException.class,
                () -> contractNegotiationService.getContractAgreement(CONTRACT_AGREEMENT_ID));
    }
}

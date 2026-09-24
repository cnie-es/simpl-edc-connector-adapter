package eu.europa.ec.simpl.edcconnectoradapter.service.catalogmapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.catalog.Policy;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.catalog.PolicyConstraint;
import eu.europa.ec.simpl.edcconnectoradapter.JacksonObjectValuesMapperUtil;
import eu.europa.ec.simpl.edcconnectoradapter.TestConstants;
import eu.europa.ec.simpl.edcconnectoradapter.service.policymapper.PolicyMapperService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CatalogMapperServiceTest {

    @Mock
    private PolicyMapperService policyMapperService;

    private CatalogMapperService catalogMapperService;

    @BeforeEach
    void setUpEach() {
        catalogMapperService = new CatalogMapperServiceImpl(policyMapperService);
    }

    @Test
    void testMapEdcCatalogOneOffer() throws Exception {
        PolicyConstraint policyConstraint = PolicyConstraint.builder()
                .condition("count")
                .conditionOperator("less than or equal to")
                .conditionValue("10")
                .build();
        Policy policy =
                Policy.builder().policyConstraints(List.of(policyConstraint)).build();
        when(policyMapperService.mapPolicyConstraint(any())).thenReturn(policy);

        JsonNode edcCatalogResponse =
                JacksonObjectValuesMapperUtil.mapStringToObjectNode(TestConstants.CATALOG_RESPONSE_ONE_OFFER);

        var catalogResponse =
                catalogMapperService.mapEdcCatalog(edcCatalogResponse, "f7e3aab7-7858-4ae2-90b8-08dffc531ecd");
        assertNotNull(catalogResponse);
        assertTrue(catalogResponse.getDatasets().size() > 0);
        assertEquals(policy, catalogResponse.getDatasets().get(0).getPolicy());
        assertEquals(
                "ddb635ae-7ee7-44a5-813a-06b529702c0e-1",
                catalogResponse.getDatasets().get(0).getAssetId());
    }

    @Test
    void testMapEdcCatalogMultipleOffers() throws Exception {
        Policy policy = Policy.builder().policyConstraints(List.of()).build();
        when(policyMapperService.mapPolicyConstraint(any())).thenReturn(policy);

        JsonNode edcCatalogResponse =
                JacksonObjectValuesMapperUtil.mapStringToObjectNode(TestConstants.CATALOG_RESPONSE_MULTIPLE_OFFERS);

        var catalogResponse =
                catalogMapperService.mapEdcCatalog(edcCatalogResponse, "6af160a8-43e0-4d41-8b56-d3f7e2036430");

        assertNotNull(catalogResponse);
        assertTrue(catalogResponse.getDatasets().size() > 0);
        assertEquals(policy, catalogResponse.getDatasets().get(0).getPolicy());
        assertEquals(
                "c249e7b1-6b0d-4ec1-8fca-73c43427301c",
                catalogResponse.getDatasets().get(0).getAssetId());
    }

    @Test
    void testMapEmptyEdcCatalog() throws Exception {
        JsonNode edcCatalogResponse =
                JacksonObjectValuesMapperUtil.mapStringToObjectNode(TestConstants.EMPTY_CATALOG_RESPONSE);

        var catalogResponse =
                catalogMapperService.mapEdcCatalog(edcCatalogResponse, "f7e3aab7-7858-4ae2-90b8-08dffc531ecd");
        assertNotNull(catalogResponse);
        assertEquals(0, catalogResponse.getDatasets().size());
    }

    @Test
    void testMapEdcCatalogOfferForNegotiationOneOffer() throws Exception {
        JsonNode edcCatalogResponse =
                JacksonObjectValuesMapperUtil.mapStringToObjectNode(TestConstants.CATALOG_RESPONSE_ONE_OFFER);

        var catalogOfferForNegotiation = catalogMapperService.mapEdcCatalogOfferForNegotiation(
                edcCatalogResponse, "f7e3aab7-7858-4ae2-90b8-08dffc531ecd");
        assertNotNull(catalogOfferForNegotiation);
        assertTrue(catalogOfferForNegotiation.has("odrl:permission"));
        assertTrue(catalogOfferForNegotiation.has("odrl:assigner"));
        assertTrue(catalogOfferForNegotiation.get("odrl:assigner").has("@id"));
    }

    @Test
    void testMapEdcCatalogOfferForNegotiationMultipleOffers() throws Exception {
        JsonNode edcCatalogResponse =
                JacksonObjectValuesMapperUtil.mapStringToObjectNode(TestConstants.CATALOG_RESPONSE_MULTIPLE_OFFERS);

        var catalogOfferForNegotiation = catalogMapperService.mapEdcCatalogOfferForNegotiation(
                edcCatalogResponse, "6af160a8-43e0-4d41-8b56-d3f7e2036430");
        assertNotNull(catalogOfferForNegotiation);
        assertTrue(catalogOfferForNegotiation.has("odrl:permission"));
        assertTrue(catalogOfferForNegotiation.has("odrl:assigner"));
        assertTrue(catalogOfferForNegotiation.get("odrl:assigner").has("@id"));
    }
}

package eu.europa.ec.simpl.edcconnectoradapter.service.provider.registration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.simpl.data1.common.constant.CommonConstants;
import eu.europa.ec.simpl.data1.common.enumeration.OfferType;
import eu.europa.ec.simpl.data1.common.exception.InvalidPayloadException;
import eu.europa.ec.simpl.data1.common.exception.InvalidSDJsonException;
import eu.europa.ec.simpl.data1.common.exception.RemoteServiceErrorException;
import eu.europa.ec.simpl.data1.common.exception.RemoteServiceUnexpectedResponseException;
import eu.europa.ec.simpl.data1.common.util.JsonPathUtil;
import eu.europa.ec.simpl.data1.common.util.SDUtil;
import eu.europa.ec.simpl.edcconnectoradapter.TestSupport;
import eu.europa.ec.simpl.edcconnectoradapter.client.edcconnector.EDCConnectorManagementClient;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.management.request.EdcAssetDefinition;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.management.request.EdcContractDefinition;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.management.request.EdcPolicyDefinition;
import feign.FeignException;
import feign.Request;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    private static final String NS = SDUtil.toNS(CommonConstants.ECOSYSTEM);

    private static final String DATA_OFFERING_JSON_FILE = "test/data-offering.json";
    private static final String INFRA_OFFERING_JSON_FILE = "test/infra-offering.json";
    private static final String DATA_OFFERING_V2_PAYLOAD_FILE = "test/data-offering-v2-payload.json";
    private static final String INFRA_OFFERING_V2_PAYLOAD_FILE = "test/infra-offering-v2-payload.json";
    private static final String CORPUS_OFFERING_PAYLOAD_FILE = "test/corpus-offering-payload.json";
    private static final String LCR_OFFERING_PAYLOAD_FILE = "test/lcr-offering-payload.json";
    private static final String MODEL_OFFERING_PAYLOAD_FILE = "test/model-offering-payload.json";

    private RegistrationService registrationService;

    @Mock
    private EDCConnectorManagementClient edcConnectorManagementClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUpEach() {
        registrationService = new RegistrationServiceImpl(edcConnectorManagementClient, objectMapper);
        ReflectionTestUtils.setField(
                registrationService, "edcConnectorTier2BaseUrl", "https://edc-connector-tier2-base-url");
    }

    @Test
    void testEnrich() throws Exception {
        JSONObject sdJsonLd = TestSupport.getResourceAsJsonObj(DATA_OFFERING_JSON_FILE, null);

        JSONObject result = registrationService.enrich(sdJsonLd, CommonConstants.ECOSYSTEM);
        assertNotNull(result, "Result should not be null");
        assertTrue(
                StringUtils.isNotBlank(JsonPathUtil.getStringValue(
                        result, "$." + NS + "edcConnector." + NS + "providerEndpointURL", false)),
                "Provider endpoint URL should not be blank");
    }

    @Test
    void testEnrichWithEmptyEcosystem() throws Exception {
        JSONObject sdJsonLd = TestSupport.getResourceAsJsonObj(DATA_OFFERING_JSON_FILE, null);

        JSONObject result = registrationService.enrich(sdJsonLd, "");
        assertNotNull(result, "Result should not be null");
        assertTrue(
                StringUtils.isNotBlank(
                        JsonPathUtil.getStringValue(result, "$.edcConnector.providerEndpointURL", false)),
                "Provider endpoint URL should not be blank");
    }

    private static Stream<Arguments> TestRegister() {
        // offeringFileName
        return Stream.of(
                Arguments.of(OfferType.DATA, DATA_OFFERING_JSON_FILE),
                Arguments.of(OfferType.INFRASTRUCTURE, INFRA_OFFERING_JSON_FILE));
    }

    @ParameterizedTest
    @MethodSource("TestRegister")
    void testRegisterV1(OfferType offerType, String offeringFile) throws IOException {
        JSONObject sdJsonLd = TestSupport.getResourceAsJsonObj(offeringFile, null);

        String responseBody = "{\"@id\":\"f31452f6-2d41-4edd-bf1f-e06329c244d9\"}";
        ArgumentCaptor<EdcAssetDefinition> assetCaptor = ArgumentCaptor.forClass(EdcAssetDefinition.class);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        when(edcConnectorManagementClient.register(any(EdcContractDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        JSONObject result = registrationService.registerV1(sdJsonLd, CommonConstants.ECOSYSTEM, offerType);

        verify(edcConnectorManagementClient).register(assetCaptor.capture());
        assertNotNull(assetCaptor.getValue().getProperties(), "EdcAssetDefinition properties should not be null");

        assertNotNull(result, "Result should not be null");
        assertTrue(
                StringUtils.isNotBlank(
                        JsonPathUtil.getStringValue(result, "$." + NS + "edcRegistration." + NS + "assetId", false)),
                "Asset ID should not be blank");
        assertTrue(
                StringUtils.isNotBlank(JsonPathUtil.getStringValue(
                        result, "$." + NS + "edcRegistration." + NS + "accessPolicyId", false)),
                "Access policy ID should not be blank");
        assertTrue(
                StringUtils.isNotBlank(JsonPathUtil.getStringValue(
                        result, "$." + NS + "edcRegistration." + NS + "servicePolicyId", false)),
                "Service policy ID should not be blank");
        assertTrue(
                StringUtils.isNotBlank(JsonPathUtil.getStringValue(
                        result, "$." + NS + "edcRegistration." + NS + "contractDefinitionId", false)),
                "Contract definition ID should not be blank");

        // verifico che sia stato rimosso generalServiceProperties.providerDataAddress
        assertNull(
                result.getJSONObject(NS + "generalServiceProperties")
                        .optJSONObject(NS + CommonConstants.SD.PROVIDER_DATA_ADDRESS),
                "Provider data address should be removed from general service properties");
    }

    @Test
    void testRegisterV1WithEmptyEcosystem() throws Exception {
        OfferType offerType = OfferType.DATA;
        JSONObject sdJsonLd = TestSupport.getResourceAsJsonObj(DATA_OFFERING_JSON_FILE, null);

        assertThrows(
                InvalidSDJsonException.class,
                () -> registrationService.registerV1(sdJsonLd, "", offerType),
                "Should throw InvalidSDJsonException when ecosystem is empty");
    }

    @Test
    void testRegisterV1WithInvalidDataAddressJson() throws Exception {
        OfferType offerType = OfferType.DATA;
        JSONObject sdJsonLd = TestSupport.getResourceAsJsonObj(DATA_OFFERING_JSON_FILE, null);

        // replacing providerDataAddress value with an invalid json string format
        JSONObject propertiesObj = sdJsonLd.getJSONObject(NS + CommonConstants.SD.ASSET_PROPERTIES);
        propertiesObj.put(NS + CommonConstants.SD.PROVIDER_DATA_ADDRESS, "not a valid json");

        assertThrows(
                InvalidSDJsonException.class,
                () -> registrationService.registerV1(sdJsonLd, CommonConstants.ECOSYSTEM, offerType),
                "Should throw InvalidSDJsonException when provider data address is invalid JSON");
    }

    @Test
    void testRegisterV1NoValueFound() {
        OfferType offerType = OfferType.DATA;
        JSONObject sdJsonLd = new JSONObject("{\"param\":\"value\"}");
        assertThrows(
                InvalidSDJsonException.class,
                () -> registrationService.registerV1(sdJsonLd, CommonConstants.ECOSYSTEM, offerType),
                "Should throw InvalidSDJsonException when required values are not found");
    }

    @Test
    void testRegisterV1AssetDefinitionMissingAssetProperties() throws Exception {
        OfferType offerType = OfferType.DATA;
        JSONObject sdJsonLd = TestSupport.getResourceAsJsonObj(DATA_OFFERING_JSON_FILE, null);

        // Remove the assetProperties key to trigger the if condition at line 107
        sdJsonLd.remove(NS + CommonConstants.SD.ASSET_PROPERTIES);

        assertThrows(
                InvalidSDJsonException.class,
                () -> registrationService.registerV1(sdJsonLd, CommonConstants.ECOSYSTEM, offerType),
                "Should throw InvalidSDJsonException when assetProperties is missing");
    }

    @Test
    void testRegisterV1WithInvalidAccessPolicyJson() throws Exception {
        OfferType offerType = OfferType.DATA;
        JSONObject sdJsonLd = TestSupport.getResourceAsJsonObj(DATA_OFFERING_JSON_FILE, null);

        // Replace access-policy with invalid JSON that will cause JsonProcessingException in objectMapper.readValue()
        JSONObject servicePolicyObj = sdJsonLd.getJSONObject(NS + "servicePolicy");
        servicePolicyObj.put(NS + "access-policy", "{invalid json content missing quotes and braces");

        assertThrows(
                InvalidSDJsonException.class,
                () -> registrationService.registerV1(sdJsonLd, CommonConstants.ECOSYSTEM, offerType),
                "Should throw InvalidSDJsonException when access-policy JSON is malformed");
    }

    @Test
    void testRegisterV1WithInvalidUsagePolicyJson() throws Exception {
        OfferType offerType = OfferType.DATA;
        JSONObject sdJsonLd = TestSupport.getResourceAsJsonObj(DATA_OFFERING_JSON_FILE, null);

        // Replace usage-policy with invalid JSON that will cause JsonProcessingException in objectMapper.readValue()
        JSONObject servicePolicyObj = sdJsonLd.getJSONObject(NS + "servicePolicy");
        servicePolicyObj.put(NS + "usage-policy", "not valid json at all {{{");

        assertThrows(
                InvalidSDJsonException.class,
                () -> registrationService.registerV1(sdJsonLd, CommonConstants.ECOSYSTEM, offerType),
                "Should throw InvalidSDJsonException when usage-policy JSON is malformed");
    }

    @Test
    void testRegisterV1AssetWithRemoteServiceUnexpectedResponseException() throws Exception {
        OfferType offerType = OfferType.DATA;
        JSONObject sdJsonLd = TestSupport.getResourceAsJsonObj(DATA_OFFERING_JSON_FILE, null);

        ResponseEntity<String> invalidResponseEntity = new ResponseEntity<>("no json content", HttpStatus.OK);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(invalidResponseEntity);
        assertThrows(
                RemoteServiceUnexpectedResponseException.class,
                () -> registrationService.registerV1(sdJsonLd, CommonConstants.ECOSYSTEM, offerType),
                "Should throw RemoteServiceUnexpectedResponseException when asset registration returns invalid response");
    }

    private static Stream<Arguments> WithExceptionsParameters() {
        Request request = mock(Request.class);
        return Stream.of(Arguments.of(new FeignException.BadRequest("test", request, "test".getBytes(), null)));
    }

    @ParameterizedTest
    @MethodSource("WithExceptionsParameters")
    void testRegisterV1AssetWithExceptions(Exception e) throws Exception {
        OfferType offerType = OfferType.DATA;
        JSONObject sdJsonLd = TestSupport.getResourceAsJsonObj(DATA_OFFERING_JSON_FILE, null);

        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenThrow(e);
        assertThrows(
                RemoteServiceErrorException.class,
                () -> registrationService.registerV1(sdJsonLd, CommonConstants.ECOSYSTEM, offerType),
                "Should throw RemoteServiceErrorException when asset registration fails with exception");
    }

    @Test
    void testRegisterV1AssetWithNotOKresponseCode() throws Exception {
        OfferType offerType = OfferType.DATA;
        JSONObject sdJsonLd = TestSupport.getResourceAsJsonObj(DATA_OFFERING_JSON_FILE, null);

        ResponseEntity<String> notOKResponseEntity = new ResponseEntity<>("does not care", HttpStatus.ACCEPTED);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(notOKResponseEntity);

        assertThrows(
                RemoteServiceErrorException.class,
                () -> registrationService.registerV1(sdJsonLd, CommonConstants.ECOSYSTEM, offerType),
                "Should throw RemoteServiceErrorException when asset registration returns non-OK status");
    }

    @Test
    void testRegisterV1PolicyWithRemoteServiceUnexpectedResponseException() throws Exception {
        OfferType offerType = OfferType.DATA;
        JSONObject sdJsonLd = TestSupport.getResourceAsJsonObj(DATA_OFFERING_JSON_FILE, null);

        ResponseEntity<String> validResponseEntity =
                new ResponseEntity<>("{\"@id\": \"fbda41f7-d339-4b44-8919-dce1a89f8e70\"}", HttpStatus.OK);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(validResponseEntity);

        ResponseEntity<String> invalidResponseEntity = new ResponseEntity<>("no json content", HttpStatus.OK);
        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenReturn(invalidResponseEntity);

        assertThrows(
                RemoteServiceUnexpectedResponseException.class,
                () -> registrationService.registerV1(sdJsonLd, CommonConstants.ECOSYSTEM, offerType),
                "Should throw RemoteServiceUnexpectedResponseException when policy registration returns invalid response");
    }

    @ParameterizedTest
    @MethodSource("WithExceptionsParameters")
    void testRegisterV1PolicyWithExceptions(Exception e) throws Exception {
        OfferType offerType = OfferType.DATA;
        JSONObject sdJsonLd = TestSupport.getResourceAsJsonObj(DATA_OFFERING_JSON_FILE, null);

        ResponseEntity<String> validResponseEntity =
                new ResponseEntity<>("{\"@id\": \"fbda41f7-d339-4b44-8919-dce1a89f8e70\"}", HttpStatus.OK);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(validResponseEntity);

        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenThrow(e);
        assertThrows(
                RemoteServiceErrorException.class,
                () -> registrationService.registerV1(sdJsonLd, CommonConstants.ECOSYSTEM, offerType),
                "Should throw RemoteServiceErrorException when policy registration fails with exception");
    }

    @Test
    void testRegisterV1PolicyWithNotOKresponseCode() throws Exception {
        OfferType offerType = OfferType.DATA;
        JSONObject sdJsonLd = TestSupport.getResourceAsJsonObj(DATA_OFFERING_JSON_FILE, null);

        ResponseEntity<String> validResponseEntity =
                new ResponseEntity<>("{\"@id\": \"fbda41f7-d339-4b44-8919-dce1a89f8e70\"}", HttpStatus.OK);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(validResponseEntity);

        ResponseEntity<String> notOKResponseEntity = new ResponseEntity<>("does not care", HttpStatus.ACCEPTED);
        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenReturn(notOKResponseEntity);

        assertThrows(
                RemoteServiceErrorException.class,
                () -> registrationService.registerV1(sdJsonLd, CommonConstants.ECOSYSTEM, offerType),
                "Should throw RemoteServiceErrorException when policy registration returns non-OK status");
    }

    @Test
    void testRegisterV1ContractWithRemoteServiceUnexpectedResponseException() throws Exception {
        OfferType offerType = OfferType.DATA;
        JSONObject sdJsonLd = TestSupport.getResourceAsJsonObj(DATA_OFFERING_JSON_FILE, null);

        ResponseEntity<String> validResponseEntity =
                new ResponseEntity<>("{\"@id\": \"fbda41f7-d339-4b44-8919-dce1a89f8e70\"}", HttpStatus.OK);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(validResponseEntity);
        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenReturn(validResponseEntity);

        ResponseEntity<String> invalidResponseEntity = new ResponseEntity<>("no json content", HttpStatus.OK);
        when(edcConnectorManagementClient.register(any(EdcContractDefinition.class)))
                .thenReturn(invalidResponseEntity);

        assertThrows(
                RemoteServiceUnexpectedResponseException.class,
                () -> registrationService.registerV1(sdJsonLd, CommonConstants.ECOSYSTEM, offerType),
                "Should throw RemoteServiceUnexpectedResponseException when contract registration returns invalid response");
    }

    @ParameterizedTest
    @MethodSource("WithExceptionsParameters")
    void testRegisterV1ContractWithExceptions(Exception e) throws Exception {
        OfferType offerType = OfferType.DATA;
        JSONObject sdJsonLd = TestSupport.getResourceAsJsonObj(DATA_OFFERING_JSON_FILE, null);

        ResponseEntity<String> validResponseEntity =
                new ResponseEntity<>("{\"@id\": \"fbda41f7-d339-4b44-8919-dce1a89f8e70\"}", HttpStatus.OK);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(validResponseEntity);
        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenReturn(validResponseEntity);

        when(edcConnectorManagementClient.register(any(EdcContractDefinition.class)))
                .thenThrow(e);
        assertThrows(
                RemoteServiceErrorException.class,
                () -> registrationService.registerV1(sdJsonLd, CommonConstants.ECOSYSTEM, offerType),
                "Should throw RemoteServiceErrorException when contract registration fails with exception");
    }

    @Test
    void testRegisterV1ContractWithNotOKresponseCode() throws Exception {
        OfferType offerType = OfferType.DATA;
        JSONObject sdJsonLd = TestSupport.getResourceAsJsonObj(DATA_OFFERING_JSON_FILE, null);

        ResponseEntity<String> validResponseEntity =
                new ResponseEntity<>("{\"@id\": \"fbda41f7-d339-4b44-8919-dce1a89f8e70\"}", HttpStatus.OK);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(validResponseEntity);
        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenReturn(validResponseEntity);

        ResponseEntity<String> notOKResponseEntity = new ResponseEntity<>("does not care", HttpStatus.ACCEPTED);
        when(edcConnectorManagementClient.register(any(EdcContractDefinition.class)))
                .thenReturn(notOKResponseEntity);

        assertThrows(
                RemoteServiceErrorException.class,
                () -> registrationService.registerV1(sdJsonLd, CommonConstants.ECOSYSTEM, offerType),
                "Should throw RemoteServiceErrorException when contract registration returns non-OK status");
    }

    // -----------------------------------------------------------------------
    // registerV2 tests
    // -----------------------------------------------------------------------

    private static Stream<Arguments> TestRegisterV2() {
        return Stream.of(
                Arguments.of(OfferType.DATA, DATA_OFFERING_V2_PAYLOAD_FILE, "test eng 2025"),
                Arguments.of(OfferType.INFRASTRUCTURE, INFRA_OFFERING_V2_PAYLOAD_FILE, "dummy"));
    }

    @ParameterizedTest
    @MethodSource("TestRegisterV2")
    void testRegisterV2(OfferType offerType, String payloadFile, String expectedSimplDescription)
            throws IOException {
        JSONObject payload = TestSupport.getResourceAsJsonObj(payloadFile, null);

        String responseBody = "{\"@id\":\"f31452f6-2d41-4edd-bf1f-e06329c244d9\"}";

        ArgumentCaptor<EdcAssetDefinition> assetCaptor = ArgumentCaptor.forClass(EdcAssetDefinition.class);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        when(edcConnectorManagementClient.register(any(EdcContractDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        JSONObject result = registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, offerType);

        verify(edcConnectorManagementClient).register(assetCaptor.capture());
        Map<String, String> properties = assetCaptor.getValue().getProperties();
        assertNotNull(properties, "EdcAssetDefinition properties should not be null");
        assertTrue(properties.containsKey("templateId"), "EdcAssetDefinition properties should contain templateId");
        assertTrue(
                properties.containsKey(NS + "description"),
                "EdcAssetDefinition properties should contain namespace-aligned description key");
        assertEquals(
                expectedSimplDescription,
                properties.get(NS + "description"),
                "Description should match self-description generalServiceProperties");

        assertNotNull(result, "Result should not be null");
        assertTrue(
                StringUtils.isNotBlank(
                        JsonPathUtil.getStringValue(result, "$." + NS + "edcRegistration." + NS + "assetId", false)),
                "Asset ID should not be blank");
        assertTrue(
                StringUtils.isNotBlank(JsonPathUtil.getStringValue(
                        result, "$." + NS + "edcRegistration." + NS + "accessPolicyId", false)),
                "Access policy ID should not be blank");
        assertTrue(
                StringUtils.isNotBlank(JsonPathUtil.getStringValue(
                        result, "$." + NS + "edcRegistration." + NS + "servicePolicyId", false)),
                "Service policy ID should not be blank");
        assertTrue(
                StringUtils.isNotBlank(JsonPathUtil.getStringValue(
                        result, "$." + NS + "edcRegistration." + NS + "contractDefinitionId", false)),
                "Contract definition ID should not be blank");
    }

    private static Stream<Arguments> testRegisterV2MapsAssetSpecificProperties() {
        return Stream.of(
                Arguments.of(
                        CORPUS_OFFERING_PAYLOAD_FILE,
                        "ABSITA dataset1",
                        "The ABSITA dataset contains 4,121 reviews written in Italian and collected from the website booking.com. Reviews are annotated according to seven aspects: pulizia (cleanliness), comfort, servizi (amenities), staff, qualita-prezzo (value), wifi (wireless Internet connection) and posizione (location). For each aspect, the polarity (positive, negative) of its mention has been annotated. The positive and negative polarities are annotated independently, thus for each aspect four sentiment classes are possible: positive (positive=1, negative=0), negative (positive=0, negative=1), neutral (positive=0, negative=0), mixed (positive=1, negative=1).The dataset has been created and used in the context of the ABSITA task (http://sag.art.uniroma2.it/absita/), organised as part of EVALITA 2018 (http://www.evalita.it/2018).",
                        "ms:Corpus",
                        "ABSITA dataset1",
                        "free",
                        "0"),
                Arguments.of(
                        LCR_OFFERING_PAYLOAD_FILE,
                        "Biodiversity Thesaurus",
                        "This bilingual thesaurus organizes the key concepts of biodiversity sciences, in their basic and applied ecological components. It uses polyhierarchy and includes 818 reciprocal associative relationships. The 1654 French and English descriptors (designating 827 concepts) are enriched with a large number of synonyms (1860) and hidden variants (7020) in both languages. Concepts are grouped into 82 collections, by semantic categories, thematic fields and EBV classes (Essential Biodiversity Variables). Definitions are given with their sources. This resource is aligned with the international thesauri AGROVOC, GEMET (GEneral Multilingual Environmental Thesaurus) and EnvThes (Environmental Thesaurus), as well as with the ontology ENVO (Environment Ontology).",
                        "ms:LexicalConceptualResource",
                        "Biodiversity Thesaurus",
                        "free",
                        "0"),
                Arguments.of(
                        MODEL_OFFERING_PAYLOAD_FILE,
                        "Word2Vec embeddings for English",
                        "Word2Vec model for English.",
                        "ms:MLModel",
                        "Word2Vec embeddings for English",
                        "free",
                        "0"));
    }

    @ParameterizedTest
    @MethodSource
    void testRegisterV2MapsAssetSpecificProperties(
            String payloadFile,
            String expectedAssetTitle,
            String expectedAssetDescription,
            String expectedAssetTypeId,
            String expectedSimplName,
            String expectedPriceType,
            String expectedPrice)
            throws IOException {
        JSONObject sdJson = TestSupport.getResourceAsJsonObj(payloadFile, null);
        JSONObject payload = toV2Payload(sdJson, "5");

        String responseBody = "{\"@id\":\"f31452f6-2d41-4edd-bf1f-e06329c244d9\"}";
        ArgumentCaptor<EdcAssetDefinition> assetCaptor = ArgumentCaptor.forClass(EdcAssetDefinition.class);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        when(edcConnectorManagementClient.register(any(EdcContractDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA);

        verify(edcConnectorManagementClient).register(assetCaptor.capture());
        Map<String, String> properties = assetCaptor.getValue().getProperties();
        assertEquals(sdJson.getString("@id"), properties.get("sdId"), "sdId should match self-description @id");
        assertEquals(expectedAssetTypeId, properties.get("assetType"), "assetType should match asset rdf:type @id");
        assertNotNull(properties.get("createdAt"), "createdAt should be set");
        assertEquals(expectedAssetTitle, properties.get("assetTitle"), "assetTitle should be mapped");
        assertEquals(
                expectedAssetDescription,
                properties.get("assetDescription"),
                "assetDescription should be mapped");
        assertEquals(expectedAssetTypeId, properties.get("assetTypeId"), "assetTypeId should be mapped");
        assertEquals(expectedSimplName, properties.get(NS + "name"), "simpl:name should be mapped");
        assertEquals(expectedPriceType, properties.get(NS + "priceType"), "simpl:priceType should be mapped");
        assertEquals(expectedPrice, properties.get(NS + "price"), "simpl:price should be mapped");

        assertEquals(sdJson.getString("@id"), properties.get("offer.offerID"));
        assertEquals(expectedSimplName, properties.get("offer.offer_name"));
        assertEquals(expectedAssetDescription, properties.get("offer.offerDescription"));
        assertEquals("true", properties.get("offer.isPublic"));
        assertEquals("true", properties.get("offer.isFree"));
        assertEquals("EUR", properties.get("offer.currency"));
        assertEquals(expectedPriceType, properties.get("offer.priceType"));
        assertEquals(expectedPrice, properties.get("offer.price"));
        assertEquals("https://licence.test.com", properties.get("offer.license"));
    }

    @Test
    void testRegisterV2OmitsAssetSpecificPropertiesWhenAssetNodesMissing() throws IOException {
        JSONObject sdJson = TestSupport.getResourceAsJsonObj(CORPUS_OFFERING_PAYLOAD_FILE, null);
        sdJson.remove("edval:corpusAsset");
        JSONObject payload = toV2Payload(sdJson, "5");

        String responseBody = "{\"@id\":\"f31452f6-2d41-4edd-bf1f-e06329c244d9\"}";
        ArgumentCaptor<EdcAssetDefinition> assetCaptor = ArgumentCaptor.forClass(EdcAssetDefinition.class);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        when(edcConnectorManagementClient.register(any(EdcContractDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA);

        verify(edcConnectorManagementClient).register(assetCaptor.capture());
        Map<String, String> properties = assetCaptor.getValue().getProperties();
        assertEquals(sdJson.getString("@id"), properties.get("sdId"), "sdId should still be mapped");
        assertFalse(properties.containsKey("assetType"), "assetType should be omitted when no asset node exists");
        assertFalse(properties.containsKey("assetTitle"), "assetTitle should be omitted when no asset node exists");
        assertFalse(
                properties.containsKey("assetDescription"),
                "assetDescription should be omitted when no asset node exists");
        assertFalse(
                properties.containsKey("assetTypeId"),
                "assetTypeId should be omitted when no asset node exists");
        assertEquals(
                "ABSITA dataset1",
                properties.get("offer.offer_name"),
                "flat offer.offer_name should still be built from generalServiceProperties");
        assertEquals("ABSITA dataset1", properties.get(NS + "name"), "simpl:name should still be mapped");
        assertEquals("free", properties.get(NS + "priceType"), "simpl:priceType should still be mapped");
        assertEquals("0", properties.get(NS + "price"), "simpl:price should still be mapped");
    }

    @Test
    void testRegisterV2MissingSdJson() {
        JSONObject payload = new JSONObject("{\"properties\":{\"providerDataAddress\":\"{}\",\"templateId\":\"t1\"}}");

        assertThrows(
                Exception.class,
                () -> registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA),
                "Should throw an exception when sdJson field is missing from payload");
    }

    @Test
    void testRegisterV2MissingProperties() throws IOException {
        JSONObject payload = TestSupport.getResourceAsJsonObj(DATA_OFFERING_V2_PAYLOAD_FILE, null);

        // Remove properties
        payload.remove("properties");

        assertThrows(
                InvalidPayloadException.class,
                () -> registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA),
                "Should throw InvalidPayloadException when properties is missing");
    }

    @Test
    void testRegisterV2MissingResourceAddress() throws IOException {
        JSONObject payload = TestSupport.getResourceAsJsonObj(DATA_OFFERING_V2_PAYLOAD_FILE, null);

        // Remove resourceAddress from properties
        payload.getJSONObject("properties").remove("resourceAddress");

        assertThrows(
                InvalidPayloadException.class,
                () -> registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA),
                "Should throw InvalidPayloadException when resourceAddress is missing");
    }

    @Test
    void testRegisterV2MissingTemplateId() throws IOException {
        JSONObject payload = TestSupport.getResourceAsJsonObj(DATA_OFFERING_V2_PAYLOAD_FILE, null);

        // Remove templateId from properties.resourceAddress
        payload.getJSONObject("properties").getJSONObject("resourceAddress").remove("templateId");

        assertThrows(
                InvalidPayloadException.class,
                () -> registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA),
                "Should throw InvalidPayloadException when templateId is missing");
    }

    @Test
    void testRegisterV2MissingResourceAddressValue() throws IOException {
        JSONObject payload = TestSupport.getResourceAsJsonObj(DATA_OFFERING_V2_PAYLOAD_FILE, null);

        // Remove templateId from properties.resourceAddress
        payload.getJSONObject("properties").getJSONObject("resourceAddress").remove("value");

        assertThrows(
                InvalidPayloadException.class,
                () -> registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA),
                "Should throw InvalidPayloadException when resourceAddress value is missing");
    }

    @Test
    void testRegisterV2OmitsDescriptionWhenGeneralServicePropertiesMissing() throws IOException {
        JSONObject payload = TestSupport.getResourceAsJsonObj(DATA_OFFERING_V2_PAYLOAD_FILE, null);
        payload.getJSONObject(CommonConstants.Enrich.PAYLOAD_SD_FIELD_NAME)
                .remove(NS + "generalServiceProperties");

        String responseBody = "{\"@id\":\"f31452f6-2d41-4edd-bf1f-e06329c244d9\"}";
        ArgumentCaptor<EdcAssetDefinition> assetCaptor = ArgumentCaptor.forClass(EdcAssetDefinition.class);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        when(edcConnectorManagementClient.register(any(EdcContractDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA);

        verify(edcConnectorManagementClient).register(assetCaptor.capture());
        Map<String, String> properties = assetCaptor.getValue().getProperties();
        assertNotNull(properties, "EdcAssetDefinition properties should not be null");
        assertTrue(properties.containsKey("templateId"), "templateId should still be required and present");
        assertFalse(
                properties.containsKey(NS + "description"),
                "Namespace-aligned description should be omitted when generalServiceProperties is absent");
    }

    @Test
    void testRegisterV2WithInvalidAccessPolicyJson() throws IOException {
        JSONObject payload = TestSupport.getResourceAsJsonObj(DATA_OFFERING_V2_PAYLOAD_FILE, null);

        JSONObject sdJson = payload.getJSONObject(CommonConstants.Enrich.PAYLOAD_SD_FIELD_NAME);
        JSONObject servicePolicyObj = sdJson.getJSONObject(NS + "servicePolicy");
        servicePolicyObj.put(NS + "access-policy", "{invalid json content missing quotes and braces");

        assertThrows(
                InvalidSDJsonException.class,
                () -> registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA),
                "Should throw InvalidSDJsonException when access-policy JSON is malformed");
    }

    @Test
    void testRegisterV2WithInvalidUsagePolicyJson() throws IOException {
        JSONObject payload = TestSupport.getResourceAsJsonObj(DATA_OFFERING_V2_PAYLOAD_FILE, null);

        JSONObject sdJson = payload.getJSONObject(CommonConstants.Enrich.PAYLOAD_SD_FIELD_NAME);
        JSONObject servicePolicyObj = sdJson.getJSONObject(NS + "servicePolicy");
        servicePolicyObj.put(NS + "usage-policy", "not valid json at all {{{");

        assertThrows(
                InvalidSDJsonException.class,
                () -> registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA),
                "Should throw InvalidSDJsonException when usage-policy JSON is malformed");
    }

    @Test
    void testRegisterV2AssetWithRemoteServiceUnexpectedResponseException() throws IOException {
        JSONObject payload = TestSupport.getResourceAsJsonObj(DATA_OFFERING_V2_PAYLOAD_FILE, null);

        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(new ResponseEntity<>("no json content", HttpStatus.OK));

        assertThrows(
                RemoteServiceUnexpectedResponseException.class,
                () -> registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA),
                "Should throw RemoteServiceUnexpectedResponseException when asset registration returns invalid response");
    }

    @ParameterizedTest
    @MethodSource("WithExceptionsParameters")
    void testRegisterV2AssetWithExceptions(Exception e) throws IOException {
        JSONObject payload = TestSupport.getResourceAsJsonObj(DATA_OFFERING_V2_PAYLOAD_FILE, null);

        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenThrow(e);

        assertThrows(
                RemoteServiceErrorException.class,
                () -> registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA),
                "Should throw RemoteServiceErrorException when asset registration fails with FeignException");
    }

    @Test
    void testRegisterV2AssetWithNotOKresponseCode() throws IOException {
        JSONObject payload = TestSupport.getResourceAsJsonObj(DATA_OFFERING_V2_PAYLOAD_FILE, null);

        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(new ResponseEntity<>("does not care", HttpStatus.ACCEPTED));

        assertThrows(
                RemoteServiceErrorException.class,
                () -> registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA),
                "Should throw RemoteServiceErrorException when asset registration returns non-OK status");
    }

    @Test
    void testRegisterV2PolicyWithRemoteServiceUnexpectedResponseException() throws IOException {
        JSONObject payload = TestSupport.getResourceAsJsonObj(DATA_OFFERING_V2_PAYLOAD_FILE, null);

        ResponseEntity<String> validResponseEntity =
                new ResponseEntity<>("{\"@id\": \"fbda41f7-d339-4b44-8919-dce1a89f8e70\"}", HttpStatus.OK);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(validResponseEntity);
        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenReturn(new ResponseEntity<>("no json content", HttpStatus.OK));

        assertThrows(
                RemoteServiceUnexpectedResponseException.class,
                () -> registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA),
                "Should throw RemoteServiceUnexpectedResponseException when policy registration returns invalid response");
    }

    @ParameterizedTest
    @MethodSource("WithExceptionsParameters")
    void testRegisterV2PolicyWithExceptions(Exception e) throws IOException {
        JSONObject payload = TestSupport.getResourceAsJsonObj(DATA_OFFERING_V2_PAYLOAD_FILE, null);

        ResponseEntity<String> validResponseEntity =
                new ResponseEntity<>("{\"@id\": \"fbda41f7-d339-4b44-8919-dce1a89f8e70\"}", HttpStatus.OK);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(validResponseEntity);
        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenThrow(e);

        assertThrows(
                RemoteServiceErrorException.class,
                () -> registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA),
                "Should throw RemoteServiceErrorException when policy registration fails with FeignException");
    }

    @Test
    void testRegisterV2PolicyWithNotOKresponseCode() throws IOException {
        JSONObject payload = TestSupport.getResourceAsJsonObj(DATA_OFFERING_V2_PAYLOAD_FILE, null);

        ResponseEntity<String> validResponseEntity =
                new ResponseEntity<>("{\"@id\": \"fbda41f7-d339-4b44-8919-dce1a89f8e70\"}", HttpStatus.OK);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(validResponseEntity);
        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenReturn(new ResponseEntity<>("does not care", HttpStatus.ACCEPTED));

        assertThrows(
                RemoteServiceErrorException.class,
                () -> registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA),
                "Should throw RemoteServiceErrorException when policy registration returns non-OK status");
    }

    @Test
    void testRegisterV2ContractWithRemoteServiceUnexpectedResponseException() throws IOException {
        JSONObject payload = TestSupport.getResourceAsJsonObj(DATA_OFFERING_V2_PAYLOAD_FILE, null);

        ResponseEntity<String> validResponseEntity =
                new ResponseEntity<>("{\"@id\": \"fbda41f7-d339-4b44-8919-dce1a89f8e70\"}", HttpStatus.OK);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(validResponseEntity);
        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenReturn(validResponseEntity);
        when(edcConnectorManagementClient.register(any(EdcContractDefinition.class)))
                .thenReturn(new ResponseEntity<>("no json content", HttpStatus.OK));

        assertThrows(
                RemoteServiceUnexpectedResponseException.class,
                () -> registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA),
                "Should throw RemoteServiceUnexpectedResponseException when contract registration returns invalid response");
    }

    @ParameterizedTest
    @MethodSource("WithExceptionsParameters")
    void testRegisterV2ContractWithExceptions(Exception e) throws IOException {
        JSONObject payload = TestSupport.getResourceAsJsonObj(DATA_OFFERING_V2_PAYLOAD_FILE, null);

        ResponseEntity<String> validResponseEntity =
                new ResponseEntity<>("{\"@id\": \"fbda41f7-d339-4b44-8919-dce1a89f8e70\"}", HttpStatus.OK);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(validResponseEntity);
        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenReturn(validResponseEntity);
        when(edcConnectorManagementClient.register(any(EdcContractDefinition.class)))
                .thenThrow(e);

        assertThrows(
                RemoteServiceErrorException.class,
                () -> registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA),
                "Should throw RemoteServiceErrorException when contract registration fails with FeignException");
    }

    @Test
    void testRegisterV1WithPolicyHavingNoPermissions() throws IOException {
        // Tests the null-permissions branch in setTarget(): the policy has no permission array
        OfferType offerType = OfferType.DATA;
        JSONObject sdJsonLd = TestSupport.getResourceAsJsonObj(DATA_OFFERING_JSON_FILE, null);

        // Replace access-policy and usage-policy with JSON that has no "permission" field
        String policyWithoutPermissions = "{\"profile\":\"http://www.w3.org/ns/odrl/2/odrl.jsonld\","
                + "\"target\":\"some-target\","
                + "\"uid\":\"a5bedff0-e3e3-4806-8c83-9a97002fae4e\","
                + "\"@context\":\"http://www.w3.org/ns/odrl.jsonld\","
                + "\"@type\":\"Set\"}";
        JSONObject servicePolicyObj = sdJsonLd.getJSONObject(NS + "servicePolicy");
        servicePolicyObj.put(NS + "access-policy", policyWithoutPermissions);
        servicePolicyObj.put(NS + "usage-policy", policyWithoutPermissions);

        String responseBody = "{\"@id\":\"f31452f6-2d41-4edd-bf1f-e06329c244d9\"}";
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        when(edcConnectorManagementClient.register(any(EdcContractDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        // Should complete without NullPointerException despite null permissions
        JSONObject result = registrationService.registerV1(sdJsonLd, CommonConstants.ECOSYSTEM, offerType);

        assertNotNull(result, "Result should not be null even when policy has no permissions");
        assertTrue(
                StringUtils.isNotBlank(
                        JsonPathUtil.getStringValue(result, "$." + NS + "edcRegistration." + NS + "assetId", false)),
                "Asset ID should not be blank");
    }

    @Test
    void testRegisterV2WithPolicyHavingNoPermissions() throws IOException {
        // Tests the null-permissions branch in setTarget(): the policy has no permission array
        JSONObject payload = TestSupport.getResourceAsJsonObj(DATA_OFFERING_V2_PAYLOAD_FILE, null);

        // Replace access-policy and usage-policy with JSON that has no "permission" field
        String policyWithoutPermissions = "{\"profile\":\"http://www.w3.org/ns/odrl/2/odrl.jsonld\","
                + "\"target\":\"some-target\","
                + "\"uid\":\"a5bedff0-e3e3-4806-8c83-9a97002fae4e\","
                + "\"@context\":\"http://www.w3.org/ns/odrl.jsonld\","
                + "\"@type\":\"Set\"}";
        JSONObject sdJson = payload.getJSONObject(CommonConstants.Enrich.PAYLOAD_SD_FIELD_NAME);
        JSONObject servicePolicyObj = sdJson.getJSONObject(NS + "servicePolicy");
        servicePolicyObj.put(NS + "access-policy", policyWithoutPermissions);
        servicePolicyObj.put(NS + "usage-policy", policyWithoutPermissions);

        String responseBody = "{\"@id\":\"f31452f6-2d41-4edd-bf1f-e06329c244d9\"}";
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        when(edcConnectorManagementClient.register(any(EdcContractDefinition.class)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

        // Should complete without NullPointerException despite null permissions
        JSONObject result = registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA);

        assertNotNull(result, "Result should not be null even when policy has no permissions");
        assertTrue(
                StringUtils.isNotBlank(
                        JsonPathUtil.getStringValue(result, "$." + NS + "edcRegistration." + NS + "assetId", false)),
                "Asset ID should not be blank");
    }

    @Test
    void testRegisterV2ContractWithNotOKresponseCode() throws IOException {
        JSONObject payload = TestSupport.getResourceAsJsonObj(DATA_OFFERING_V2_PAYLOAD_FILE, null);

        ResponseEntity<String> validResponseEntity =
                new ResponseEntity<>("{\"@id\": \"fbda41f7-d339-4b44-8919-dce1a89f8e70\"}", HttpStatus.OK);
        when(edcConnectorManagementClient.register(any(EdcAssetDefinition.class)))
                .thenReturn(validResponseEntity);
        when(edcConnectorManagementClient.register(any(EdcPolicyDefinition.class)))
                .thenReturn(validResponseEntity);
        when(edcConnectorManagementClient.register(any(EdcContractDefinition.class)))
                .thenReturn(new ResponseEntity<>("does not care", HttpStatus.ACCEPTED));

        assertThrows(
                RemoteServiceErrorException.class,
                () -> registrationService.registerV2(payload, CommonConstants.ECOSYSTEM, OfferType.DATA),
                "Should throw RemoteServiceErrorException when contract registration returns non-OK status");
    }

    private static JSONObject toV2Payload(JSONObject sdJson, String templateId) {
        JSONObject payload = new JSONObject();
        payload.put(CommonConstants.Enrich.PAYLOAD_SD_FIELD_NAME, sdJson);
        payload.put(
                "properties",
                new JSONObject()
                        .put(
                                "resourceAddress",
                                new JSONObject()
                                        .put("templateId", templateId)
                                        .put(
                                                "value",
                                                "{\"type\":\"MinioS3\", \"endpoint\":\"https://minio.simpl-europe.eu\", \"bucketName\":\"provider-bucket\", \"objectName\":\"example-s3.txt\"}")));
        return payload;
    }
}

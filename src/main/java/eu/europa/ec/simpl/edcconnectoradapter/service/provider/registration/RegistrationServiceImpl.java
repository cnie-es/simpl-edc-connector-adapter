package eu.europa.ec.simpl.edcconnectoradapter.service.provider.registration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import eu.europa.ec.simpl.data1.common.constant.CommonConstants;
import eu.europa.ec.simpl.data1.common.enumeration.OfferType;
import eu.europa.ec.simpl.data1.common.exception.InvalidSDJsonException;
import eu.europa.ec.simpl.data1.common.exception.RemoteServiceUnexpectedResponseException;
import eu.europa.ec.simpl.data1.common.model.ld.odrl.OdrlPermission;
import eu.europa.ec.simpl.data1.common.model.ld.odrl.OdrlPolicy;
import eu.europa.ec.simpl.data1.common.util.JsonPathUtil;
import eu.europa.ec.simpl.data1.common.util.JsonUtil;
import eu.europa.ec.simpl.data1.common.util.RemoteServiceUtil;
import eu.europa.ec.simpl.data1.common.util.SDUtil;
import eu.europa.ec.simpl.edcconnectoradapter.client.edcconnector.EDCConnectorManagementClient;
import eu.europa.ec.simpl.edcconnectoradapter.enumeration.EDCErrorType;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.management.request.EdcAssetDefinition;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.management.request.EdcContractDefinition;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.management.request.EdcPolicyDefinition;
import feign.FeignException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Profile({"provider", "build", "test"})
@Service
@Log4j2
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private static final String OPERATION_FAILED_FORMAT = "%s failed";

    private static final String RESPONSE_ID_PATH = "$.@id";

    private static final String NS = "{NS}";

    private static final String ASSET_DATA_ADRESS_JSON_PATH =
            "$." + NS + "{propertiesName}." + NS + "providerDataAddress";
    private static final String ACCESS_POLICY_PATH = "$." + NS + "servicePolicy." + NS + "access-policy";
    private static final String USAGE_POLICY_PATH = "$." + NS + "servicePolicy." + NS + "usage-policy";

    private static final String REG_PROPS_TEMPLATE_ID_NAME = "templateId";
    private static final String REG_PROPS_ASSET_TITLE_NAME = "assetTitle";
    private static final String REG_PROPS_ASSET_DESCRIPTION_NAME = "assetDescription";
    private static final String REG_PROPS_ASSET_TYPE_ID_NAME = "assetTypeId";
    private static final String REG_PROPS_SD_ID_NAME = "sdId";
    private static final String REG_PROPS_ASSET_TYPE_NAME = "assetType";
    private static final String REG_PROPS_CREATED_AT_NAME = "createdAt";

    private static final String OFFER_PROPERTY_PREFIX = "offer.";

    private static final String OFFER_JSON_OFFER_ID = "offerID";
    private static final String OFFER_JSON_OFFER_NAME = "offer_name";
    private static final String OFFER_JSON_OFFER_DESCRIPTION = "offerDescription";
    private static final String OFFER_JSON_IS_PUBLIC = "isPublic";
    private static final String OFFER_JSON_IS_FREE = "isFree";

    private static final String SD_GENERAL_SERVICE_PROPERTIES = "generalServiceProperties";
    private static final String SD_OFFERING_PRICE = "offeringPrice";
    private static final String SD_DESCRIPTION = "description";
    private static final String SD_NAME = "name";
    private static final String SD_PRICE_TYPE = "priceType";
    private static final String SD_PRICE = "price";
    private static final String SD_LICENSE = "license";
    private static final String SD_CURRENCY = "currency";

    private static final String EDVAL_CORPUS_ASSET = "edval:corpusAsset";
    private static final String EDVAL_LCR_ASSET = "edval:lcrAsset";
    private static final String EDVAL_MODEL_ASSET = "edval:modelAsset";
    private static final String EDVAL_IS_PUBLIC_OFFERING = "edval:isPublicOffering";

    private static final String DCT_TITLE = "dct:title";
    private static final String DCT_DESCRIPTION = "dct:description";
    private static final String RDF_TYPE = "rdf:type";
    private static final String AT_VALUE = "@value";
    private static final String AT_ID = "@id";

    @Value("${edc-connector.tier2-base-url}")
    private String edcConnectorTier2BaseUrl;

    private final EDCConnectorManagementClient edcConnectorClient;
    private final ObjectMapper objectMapper;

    @Override
    public JSONObject enrich(JSONObject sdJsonLd, String ecosystem) {
        JSONObject edcConnectorObj = new JSONObject();
        String ns = SDUtil.toNS(ecosystem);
        sdJsonLd.put(ns + "edcConnector", edcConnectorObj);
        edcConnectorObj.put(ns + "providerEndpointURL", edcConnectorTier2BaseUrl);
        return sdJsonLd;
    }

    @Override
    public JSONObject registerV2(JSONObject payload, String ecosystem, OfferType offerType)
            throws JsonProcessingException {
        log.debug("registerV2() for ecosystem '{}', offerType '{}' and payload {}", ecosystem, offerType, payload);

        JSONObject sdJsonLd = payload.getJSONObject(CommonConstants.Enrich.PAYLOAD_SD_FIELD_NAME);
        JsonNode resourceAddress = JsonUtil.createJsonNodeFromPayload(
                payload.toString(),
                JsonUtil.toJsonPath(CommonConstants.Enrich.PAYLOAD_RESOURCE_ADDRESS_VALUE_PATH)
                        .getPath(),
                objectMapper);
        Map<String, String> registrationProperties =
                createRegistrationProperties(payload, sdJsonLd, ecosystem);

        sdJsonLd = register(sdJsonLd, resourceAddress, registrationProperties, ecosystem, offerType);

        log.debug("registerV2(): returning sdJsonLd {}", sdJsonLd);
        return sdJsonLd;
    }

    @Override
    public JSONObject registerV1(JSONObject sdJsonLd, String ecosystem, OfferType offerType)
            throws JsonProcessingException {
        log.debug("registerV1() for ecosystem '{}', offerType '{}' and sdJsonLd {}", ecosystem, offerType, sdJsonLd);

        String ns = SDUtil.toNS(ecosystem);
        String assetPropertiesName = CommonConstants.SD.ASSET_PROPERTIES;
        String assetPropertiesKey = ns + assetPropertiesName;

        if (!sdJsonLd.has(assetPropertiesKey)) {
            log.error(
                    "registerAssetDefinition() failed cause property '{}' not found in the SD JSON-LD",
                    assetPropertiesName);
            throw new InvalidSDJsonException("property '" + assetPropertiesName + "'not found");
        }

        // get asset providerDataAddress from SD json as json string
        JsonNode providerDataAddress = getDataAddress(CommonConstants.SD.ASSET_PROPERTIES, ns, sdJsonLd);

        sdJsonLd = register(sdJsonLd, providerDataAddress, null, ecosystem, offerType);

        log.debug("registerV1(): returning sdJsonLd {}", sdJsonLd);
        return sdJsonLd;
    }

    private JSONObject register(
            JSONObject sdJsonLd,
            JsonNode providerDataAddress,
            Map<String, String> properties,
            String ecosystem,
            OfferType offerType)
            throws JsonProcessingException {
        log.debug(
                "register() for ecosystem '{}', offerType '{}', sdJsonLd {}, providerDataAddress {} and properties {}",
                ecosystem,
                offerType,
                sdJsonLd,
                providerDataAddress,
                properties);

        String ns = SDUtil.toNS(ecosystem);

        OdrlPolicy accessPolicy = getOdrlPolicy(sdJsonLd, replaceNS(ACCESS_POLICY_PATH, ns));
        OdrlPolicy usagePolicy = getOdrlPolicy(sdJsonLd, replaceNS(USAGE_POLICY_PATH, ns));

        String assetRegistrationId = registerAssetDefinition(sdJsonLd, providerDataAddress, properties);
        setTarget(accessPolicy, assetRegistrationId);
        setTarget(usagePolicy, assetRegistrationId);

        String accessPolicyRegistrationId = registerPolicyDefinition("accessPolicy", accessPolicy);
        String usagePolicyRegistrationId = registerPolicyDefinition("usagePolicy", usagePolicy);
        String registerContractDefinitionId =
                registerContractDefinition(assetRegistrationId, accessPolicyRegistrationId, usagePolicyRegistrationId);

        Map<String, String> map = Map.of(
                ns + "assetId", assetRegistrationId,
                ns + "accessPolicyId", accessPolicyRegistrationId,
                ns + "servicePolicyId", usagePolicyRegistrationId,
                ns + "contractDefinitionId", registerContractDefinitionId);
        sdJsonLd.put(ns + "edcRegistration", map);

        JSONObject servicePolicyObj = sdJsonLd.getJSONObject(ns + "servicePolicy");
        servicePolicyObj.put(ns + "access-policy", objectMapper.writeValueAsString(accessPolicy));
        servicePolicyObj.put(ns + "usage-policy", objectMapper.writeValueAsString(usagePolicy));

        return sdJsonLd;
    }

    private String registerAssetDefinition(
            JSONObject sdJsonLd, JsonNode providerDataAddress, Map<String, String> properties) {

        EdcAssetDefinition assetDefinition = createAssetDefinition(sdJsonLd, providerDataAddress, properties);

        String operation = "register (asset)";
        try {
            log.debug("registerAssetDefinition() invoking edcConnectorClient.register() for {}", assetDefinition);
            ResponseEntity<String> response = edcConnectorClient.register(assetDefinition);
            String responseBody = getRegistrationResponseBody(operation, response);
            log.debug("registerAssetDefinition() received response body {}", responseBody);

            return JsonPath.read(responseBody, RESPONSE_ID_PATH);

        } catch (FeignException e) {
            log.error("registerAssetDefinition() failed cause FeignException", e);
            throw RemoteServiceUtil.toRemoteServiceErrorException(
                    EDCErrorType.REMOTE_EDC_CONNECTOR_ERROR, String.format(OPERATION_FAILED_FORMAT, operation), e);
        } catch (JsonPathException e) {
            log.error("registerAssetDefinition() failed cause JsonPathException", e);
            throw new RemoteServiceUnexpectedResponseException(
                    EDCErrorType.REMOTE_EDC_CONNECTOR_ERROR, String.format(OPERATION_FAILED_FORMAT, operation), e);
        }
    }

    private String registerPolicyDefinition(String type, OdrlPolicy odrlPolicy) {
        String operation = "register (" + type + ")";
        odrlPolicy.setType("Set");
        EdcPolicyDefinition policyDefinition = new EdcPolicyDefinition(odrlPolicy);
        try {
            log.debug("registerPolicyDefinition(): invoking edcConnectorClient.register() for {}", policyDefinition);
            ResponseEntity<String> response = edcConnectorClient.register(policyDefinition);
            String responseBody = getRegistrationResponseBody(operation, response);
            log.debug("registerPolicyDefinition(): received response body {}", responseBody);

            return JsonPath.read(responseBody, RESPONSE_ID_PATH);

        } catch (FeignException e) {
            log.error("registerPolicyDefinition() failed cause FeignException", e);
            throw RemoteServiceUtil.toRemoteServiceErrorException(
                    EDCErrorType.REMOTE_EDC_CONNECTOR_ERROR, String.format(OPERATION_FAILED_FORMAT, operation), e);
        } catch (JsonPathException e) {
            log.error("registerPolicyDefinition() failed cause JsonPathException", e);
            throw new RemoteServiceUnexpectedResponseException(
                    EDCErrorType.REMOTE_EDC_CONNECTOR_ERROR, String.format(OPERATION_FAILED_FORMAT, operation), e);
        }
    }

    private String registerContractDefinition(
            String assetRegistrationId, String accessPolicyRegistrationId, String usagePolicyRegistrationId) {
        String operation = "register (contract)";
        EdcContractDefinition contractDefinition =
                new EdcContractDefinition(assetRegistrationId, accessPolicyRegistrationId, usagePolicyRegistrationId);
        try {
            log.debug(
                    "registerContractDefinition(): invoking edcConnectorClient.register() for assetRegistrationId '{}', accessPolicyRegistrationId '{}' and usagePolicyRegistrationId '{}'",
                    assetRegistrationId,
                    accessPolicyRegistrationId,
                    usagePolicyRegistrationId);
            ResponseEntity<String> response = edcConnectorClient.register(contractDefinition);
            String responseBody = getRegistrationResponseBody(operation, response);
            log.debug("registerContractDefinition(): received response body {}", responseBody);

            return JsonPath.read(responseBody, RESPONSE_ID_PATH);
        } catch (FeignException e) {
            log.error("registerContractDefinition() failed cause FeignException", e);
            throw RemoteServiceUtil.toRemoteServiceErrorException(
                    EDCErrorType.REMOTE_EDC_CONNECTOR_ERROR, String.format(OPERATION_FAILED_FORMAT, operation), e);
        } catch (JsonPathException e) {
            log.error("registerContractDefinition() failed cause JsonPathException", e);
            throw new RemoteServiceUnexpectedResponseException(
                    EDCErrorType.REMOTE_EDC_CONNECTOR_ERROR, String.format(OPERATION_FAILED_FORMAT, operation), e);
        }
    }

    private EdcAssetDefinition createAssetDefinition(
            JSONObject sdJsonLd, JsonNode dataAddress, Map<String, String> properties) throws InvalidSDJsonException {
        EdcAssetDefinition assetDefinition = new EdcAssetDefinition();
        assetDefinition.setDataAddress(dataAddress);
        assetDefinition.setProperties(properties);
        return assetDefinition;
    }

    /**
     * Builds EDC asset {@code properties} for V2 registration. Includes {@code templateId} from the payload and, when
     * present in {@code sdJson}, {@code {ns}description} from {@code {ns}generalServiceProperties} (e.g. {@code
     * simpl:description}) so keys align with the self-description. Manual check: after register, {@code GET
     * /management/v3/assets/{id}} on the provider connector should list the same property keys/values.
     */
    private Map<String, String> createRegistrationProperties(
            JSONObject payload, JSONObject sdJsonLd, String ecosystem) {
        Map<String, String> result = new HashMap<>();
        result.put(
                REG_PROPS_TEMPLATE_ID_NAME,
                JsonUtil.getStringValue(
                        payload,
                        JsonUtil.toJsonPath(CommonConstants.Enrich.PAYLOAD_RESOURCE_ADDRESS_TEMPLATE_ID_PATH)
                                .getPath(),
                        true));

        String ns = SDUtil.toNS(ecosystem);
        putIfNotBlank(result, REG_PROPS_SD_ID_NAME, sdJsonLd.optString("@id", ""));
        result.put(REG_PROPS_CREATED_AT_NAME, Instant.now().toString());

        String generalServicePropertiesKey = ns + SD_GENERAL_SERVICE_PROPERTIES;
        if (sdJsonLd.has(generalServicePropertiesKey)) {
            JSONObject generalServiceProperties = sdJsonLd.getJSONObject(generalServicePropertiesKey);
            putIfNotBlank(result, ns + SD_DESCRIPTION, generalServiceProperties.optString(ns + SD_DESCRIPTION, ""));
            putIfNotBlank(result, ns + SD_NAME, generalServiceProperties.optString(ns + SD_NAME, ""));
        }

        String offeringPriceKey = ns + SD_OFFERING_PRICE;
        if (sdJsonLd.has(offeringPriceKey)) {
            JSONObject offeringPrice = sdJsonLd.getJSONObject(offeringPriceKey);
            putIfNotBlank(result, ns + SD_PRICE_TYPE, offeringPrice.optString(ns + SD_PRICE_TYPE, ""));
            putIfNotBlank(result, ns + SD_PRICE, getNestedString(offeringPrice, ns + SD_PRICE, AT_VALUE));
        }

        putIfNotBlank(
                result,
                REG_PROPS_ASSET_TITLE_NAME,
                getFirstNonBlank(
                        sdJsonLd,
                        new String[] {EDVAL_CORPUS_ASSET, DCT_TITLE, AT_VALUE},
                        new String[] {EDVAL_LCR_ASSET, DCT_TITLE, AT_VALUE},
                        new String[] {EDVAL_MODEL_ASSET, DCT_TITLE, AT_VALUE}));
        putIfNotBlank(
                result,
                REG_PROPS_ASSET_DESCRIPTION_NAME,
                getFirstNonBlank(
                        sdJsonLd,
                        new String[] {EDVAL_CORPUS_ASSET, DCT_DESCRIPTION, AT_VALUE},
                        new String[] {EDVAL_LCR_ASSET, DCT_DESCRIPTION, AT_VALUE},
                        new String[] {EDVAL_MODEL_ASSET, DCT_DESCRIPTION, AT_VALUE}));

        String assetTypeIri = getFirstNonBlank(
                sdJsonLd,
                new String[] {EDVAL_CORPUS_ASSET, RDF_TYPE, AT_ID},
                new String[] {EDVAL_LCR_ASSET, RDF_TYPE, AT_ID},
                new String[] {EDVAL_MODEL_ASSET, RDF_TYPE, AT_ID});
        putIfNotBlank(result, REG_PROPS_ASSET_TYPE_ID_NAME, assetTypeIri);
        putIfNotBlank(result, REG_PROPS_ASSET_TYPE_NAME, assetTypeIri);

        putOfferFlatProperties(result, sdJsonLd, ns);

        return result;
    }

    /**
     * Writes offer-related metadata as flat {@code offer.*} string properties (EDC asset properties are {@code
     * Map<String, String>} only).
     */
    private static void putOfferFlatProperties(Map<String, String> result, JSONObject sdJsonLd, String ns) {
        putIfNotBlank(result, offerPropertyKey(OFFER_JSON_OFFER_ID), sdJsonLd.optString("@id", ""));

        String generalServicePropertiesKey = ns + SD_GENERAL_SERVICE_PROPERTIES;
        if (sdJsonLd.has(generalServicePropertiesKey)) {
            JSONObject generalServiceProperties = sdJsonLd.getJSONObject(generalServicePropertiesKey);
            putIfNotBlank(
                    result,
                    offerPropertyKey(OFFER_JSON_OFFER_NAME),
                    generalServiceProperties.optString(ns + SD_NAME, ""));
            putIfNotBlank(
                    result,
                    offerPropertyKey(OFFER_JSON_OFFER_DESCRIPTION),
                    generalServiceProperties.optString(ns + SD_DESCRIPTION, ""));
        }

        Boolean isPublic = parseEdvalIsPublicOffering(sdJsonLd);
        if (isPublic != null) {
            result.put(offerPropertyKey(OFFER_JSON_IS_PUBLIC), Boolean.toString(isPublic));
        }

        Boolean isFree = computeIsFree(sdJsonLd, ns);
        if (isFree != null) {
            result.put(offerPropertyKey(OFFER_JSON_IS_FREE), Boolean.toString(isFree));
        }

        if (sdJsonLd.has(offeringPriceKey(ns))) {
            JSONObject offeringPrice = sdJsonLd.getJSONObject(offeringPriceKey(ns));
            putIfNotBlank(result, offerPropertyKey(SD_PRICE_TYPE), offeringPrice.optString(ns + SD_PRICE_TYPE, ""));
            putIfNotBlank(result, offerPropertyKey(SD_CURRENCY), offeringPrice.optString(ns + SD_CURRENCY, ""));
            putIfNotBlank(result, offerPropertyKey(SD_PRICE), getNestedString(offeringPrice, ns + SD_PRICE, AT_VALUE));
            putIfNotBlank(
                    result, offerPropertyKey(SD_LICENSE), getNestedString(offeringPrice, ns + SD_LICENSE, AT_VALUE));
        }
    }

    private static String offerPropertyKey(String suffix) {
        return OFFER_PROPERTY_PREFIX + suffix;
    }

    private static String offeringPriceKey(String ns) {
        return ns + SD_OFFERING_PRICE;
    }

    private static Boolean parseEdvalIsPublicOffering(JSONObject sdJsonLd) {
        if (!sdJsonLd.has(EDVAL_IS_PUBLIC_OFFERING)) {
            return null;
        }
        Object raw = sdJsonLd.get(EDVAL_IS_PUBLIC_OFFERING);
        if (raw instanceof JSONObject jo && jo.has(AT_VALUE)) {
            Object v = jo.get(AT_VALUE);
            if (v instanceof Boolean b) {
                return b;
            }
            if (v instanceof String s) {
                return Boolean.parseBoolean(s);
            }
        }
        return null;
    }

    private static Boolean computeIsFree(JSONObject sdJsonLd, String ns) {
        if (!sdJsonLd.has(offeringPriceKey(ns))) {
            return null;
        }
        JSONObject offeringPrice = sdJsonLd.getJSONObject(offeringPriceKey(ns));
        String priceType = offeringPrice.optString(ns + SD_PRICE_TYPE, "");
        boolean typeFree = "free".equalsIgnoreCase(priceType.trim());
        String priceVal = getNestedString(offeringPrice, ns + SD_PRICE, AT_VALUE);
        boolean priceZero = false;
        if (priceVal != null && !priceVal.isBlank()) {
            try {
                priceZero = new BigDecimal(priceVal.trim()).compareTo(BigDecimal.ZERO) == 0;
            } catch (NumberFormatException e) {
                priceZero = false;
            }
        }
        return typeFree || priceZero;
    }

    private static void putIfNotBlank(Map<String, String> properties, String key, String value) {
        if (value != null && !value.isBlank()) {
            properties.put(key, value);
        }
    }

    private static String getFirstNonBlank(JSONObject source, String[]... candidates) {
        for (String[] candidate : candidates) {
            String value = getNestedString(source, candidate);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static String getNestedString(JSONObject source, String... path) {
        Object current = source;
        for (int i = 0; i < path.length; i++) {
            if (!(current instanceof JSONObject currentObject) || !currentObject.has(path[i])) {
                return null;
            }
            current = currentObject.get(path[i]);
            if (i < path.length - 1 && !(current instanceof JSONObject)) {
                return null;
            }
        }
        return current == null ? null : String.valueOf(current);
    }

    private JsonNode getDataAddress(String propertiesName, String ns, JSONObject sdJsonLd)
            throws InvalidSDJsonException {
        String message = "no valid asset dataAddress json provided: ";
        try {
            String dataAddressName = ASSET_DATA_ADRESS_JSON_PATH.replace("{propertiesName}", propertiesName);
            String dataAddressAsString = JsonPathUtil.getStringValue(sdJsonLd, replaceNS(dataAddressName, ns), true);
            return objectMapper.readTree(dataAddressAsString);
        } catch (JsonProcessingException e) {
            throw new InvalidSDJsonException(message + e.getMessage(), e);
        }
    }

    private static String getRegistrationResponseBody(String operation, ResponseEntity<String> response) {
        int responseCode = response.getStatusCode().value();
        if (responseCode != HttpStatus.OK.value()) {
            RemoteServiceUtil.toRemoteServiceErrorException(
                    EDCErrorType.REMOTE_EDC_CONNECTOR_ERROR,
                    operation + " operation failed",
                    response.getBody(),
                    responseCode,
                    null);
        }
        return response.getBody();
    }

    private static String replaceNS(String assetUrlPath, String ns) {
        return assetUrlPath.replace(NS, ns);
    }

    private OdrlPolicy getOdrlPolicy(JSONObject sdJsonLd, String path) throws InvalidSDJsonException {
        try {
            String policyJson = JsonPathUtil.getStringValue(sdJsonLd, path, true);
            return objectMapper.readValue(policyJson, OdrlPolicy.class);
        } catch (JsonProcessingException e) {
            throw new InvalidSDJsonException("invalid policy value with path '" + path + "'", e);
        }
    }

    private static void setTarget(OdrlPolicy policy, String value) {
        policy.setTarget(value);
        List<OdrlPermission> permissions = policy.getPermissions();
        if (permissions != null) {
            permissions.forEach(e -> e.setTarget(value));
        }
    }
}

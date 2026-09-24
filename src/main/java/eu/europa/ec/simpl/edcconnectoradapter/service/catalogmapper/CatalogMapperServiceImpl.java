package eu.europa.ec.simpl.edcconnectoradapter.service.catalogmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.catalog.CatalogSearchResult;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.catalog.Dataset;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.catalog.Policy;
import eu.europa.ec.simpl.edcconnectoradapter.model.OfferIdBuilder;
import eu.europa.ec.simpl.edcconnectoradapter.service.policymapper.PolicyMapperService;
import eu.europa.ec.simpl.edcconnectoradapter.util.JsonNodeObjectMapperUtil;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogMapperServiceImpl implements CatalogMapperService {

    private final PolicyMapperService policyMapperService;

    @Override
    public CatalogSearchResult mapEdcCatalog(JsonNode jsonObject, String contractDefinitionId) {

        List<Dataset> datasetList = new LinkedList<>();

        String endpointUrl =
                JsonNodeObjectMapperUtil.getJsonValue(jsonObject, "/" + DCAT_SERVICE + "/" + DCAT_ENDPOINT_URL);

        String participantId = JsonNodeObjectMapperUtil.getJsonValue(jsonObject, "/" + DSPACE_PARTICIPANT_ID);

        if (StringUtils.isBlank(participantId)) {
            participantId = JsonNodeObjectMapperUtil.getJsonValue(jsonObject, "/" + PARTICIPANT_ID);
        }

        JsonNode dataset = jsonObject.get(DCAT_DATASET);
        if (dataset.isArray()) {
            String finalParticipantId = participantId;
            dataset.forEach(offer -> {
                List<Dataset> mapFromDataJson = mapOffer(offer, contractDefinitionId);
                mapFromDataJson.forEach(dataSet -> {
                    dataSet.setProviderParticipantId(finalParticipantId);
                    dataSet.setProviderEndpointUrl(endpointUrl);
                });
                datasetList.addAll(mapFromDataJson);
            });
        } else {
            List<Dataset> mapFromDataJson = mapOffer(dataset, contractDefinitionId);
            String finalParticipantId1 = participantId;
            mapFromDataJson.forEach(dataSet -> {
                dataSet.setProviderParticipantId(finalParticipantId1);
                dataSet.setProviderEndpointUrl(endpointUrl);
            });
            datasetList.addAll(mapFromDataJson);
        }

        return CatalogSearchResult.builder().datasets(datasetList).build();
    }

    private List<Dataset> mapOffer(JsonNode offer, String contractDefinitionId) {
        List<Dataset> datasets = new LinkedList<>();

        JsonNode offerPolicyNode = offer.get(ODRL_HAS_POLICY);
        if (offerPolicyNode.isArray()) {
            ObjectNode[] offerPolicyNodeArray = new ObjectNode[offerPolicyNode.size()];
            for (int i = 0; i < offerPolicyNode.size(); i++) {
                offerPolicyNodeArray[i] = (ObjectNode) offerPolicyNode.get(i);
            }
            JsonNode offerForContractDef = Arrays.stream(offerPolicyNodeArray)
                    .filter(offerPolicy -> isOfferForContractDef(offerPolicy, contractDefinitionId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            NO_OFFER_FOUND_FOR_CONTRACT_DEFINITION + contractDefinitionId));

            String offerId = offerForContractDef.get(JSON_LD_ID).asText();
            OfferIdBuilder offerIdBuilder = OfferIdBuilder.create(offerId);

            Policy usagePolicy = policyMapperService.mapPolicyConstraint(offerForContractDef);
            datasets.add(Dataset.builder()
                    .assetId(offer.get(JSON_LD_ID).asText())
                    .offerId(offerIdBuilder.toString())
                    .policy(usagePolicy)
                    .build());
        } else {
            if (!isOfferForContractDef(offerPolicyNode, contractDefinitionId)) {
                throw new IllegalArgumentException(NO_OFFER_FOUND_FOR_CONTRACT_DEFINITION + contractDefinitionId);
            }

            String offerId = offerPolicyNode.get(JSON_LD_ID).asText();
            OfferIdBuilder offerIdBuilder = OfferIdBuilder.create(offerId);

            Policy usagePolicy = policyMapperService.mapPolicyConstraint(offerPolicyNode);
            datasets.add(Dataset.builder()
                    .assetId(offer.get(JSON_LD_ID).asText())
                    .offerId(offerIdBuilder.toString())
                    .policy(usagePolicy)
                    .build());
        }
        return datasets;
    }

    @Override
    public JsonNode mapEdcCatalogOfferForNegotiation(JsonNode jsonObject, String contractDefinitionId) {

        JsonNode dataset = jsonObject.get(DCAT_DATASET);
        JsonNode offerPolicyNode = dataset.get(ODRL_HAS_POLICY);
        if (offerPolicyNode == null) {
            throw new IllegalArgumentException("No offer found in catalog to negotiate");
        }

        if (offerPolicyNode.isArray()) {
            ObjectNode[] offerPolicyNodeArray = new ObjectNode[offerPolicyNode.size()];
            for (int i = 0; i < offerPolicyNode.size(); i++) {
                offerPolicyNodeArray[i] = (ObjectNode) offerPolicyNode.get(i);
            }
            ObjectNode offerForContractDef = Arrays.stream(offerPolicyNodeArray)
                    .filter(offerPolicy -> isOfferForContractDef(offerPolicy, contractDefinitionId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            NO_OFFER_FOUND_FOR_CONTRACT_DEFINITION + contractDefinitionId));
            // Create @id under odrl:assigner to have a json-ld format requested for
            // negotiation
            JsonNode assignerNode = offerForContractDef.get(ODRL_ASSIGNER);
            ObjectNode newAssignerNode = JsonNodeFactory.instance.objectNode();
            newAssignerNode.put(JSON_LD_ID, assignerNode.asText());
            offerForContractDef.set(ODRL_ASSIGNER, newAssignerNode);
            return offerForContractDef;

        } else {
            if (!isOfferForContractDef(offerPolicyNode, contractDefinitionId)) {
                throw new IllegalArgumentException(NO_OFFER_FOUND_FOR_CONTRACT_DEFINITION + contractDefinitionId);
            }
            // Create @id under odrl:assigner to have a json-ld format requested for
            // negotiation
            JsonNode assignerNode = offerPolicyNode.get(ODRL_ASSIGNER);
            ObjectNode newAssignerNode = JsonNodeFactory.instance.objectNode();
            newAssignerNode.put(JSON_LD_ID, assignerNode.asText());
            ((ObjectNode) offerPolicyNode).set(ODRL_ASSIGNER, newAssignerNode);
            return offerPolicyNode;
        }
    }

    private static boolean isOfferForContractDef(JsonNode offerPolicy, String contractDefinitionId) {
        String offerId = offerPolicy.get(JSON_LD_ID).asText();
        OfferIdBuilder offerIdBuilder = OfferIdBuilder.create(offerId);
        return offerIdBuilder.getContractDefinitionId().equals(contractDefinitionId);
    }
}

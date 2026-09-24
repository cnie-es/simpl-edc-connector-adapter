package eu.europa.ec.simpl.edcconnectoradapter.service.catalogmapper;

import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.catalog.CatalogSearchResult;

public interface CatalogMapperService {

    String DCAT_DATASET = "dcat:dataset";
    String DCAT_ENDPOINT_URL = "dcat:endpointUrl";
    String DCAT_SERVICE = "dcat:service";
    String DSPACE_PARTICIPANT_ID = "dspace:participantId";
    String JSON_LD_ID = "@id";
    String NO_OFFER_FOUND_FOR_CONTRACT_DEFINITION = "No offer found for contract definition ";
    String ODRL_ASSIGNER = "odrl:assigner";
    String ODRL_HAS_POLICY = "odrl:hasPolicy";
    String PARTICIPANT_ID = "participantId";

    CatalogSearchResult mapEdcCatalog(JsonNode jsonObject, String contractDefinitionId);

    JsonNode mapEdcCatalogOfferForNegotiation(JsonNode jsonObject, String contractDefinitionId);
}

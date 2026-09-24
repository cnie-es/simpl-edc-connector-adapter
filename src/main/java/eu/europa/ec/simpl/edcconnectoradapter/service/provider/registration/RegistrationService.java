package eu.europa.ec.simpl.edcconnectoradapter.service.provider.registration;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europa.ec.simpl.data1.common.enumeration.OfferType;
import org.json.JSONObject;

public interface RegistrationService {

    /**
     * @param sdJsonLd
     * @param ecosystem
     * @return enriched result
     */
    JSONObject enrich(JSONObject sdJsonLd, String ecosystem);

    /**
     * @param sdJsonLd
     * @param ecosystem
     * @param offerType
     * @return registered result
     * @throws JsonProcessingException
     */
    JSONObject registerV1(JSONObject sdJsonLd, String ecosystem, OfferType offerType) throws JsonProcessingException;

    /**
     * @param payload
     * @param ecosystem
     * @param offerType
     * @return registered result
     * @throws JsonProcessingException
     */
    JSONObject registerV2(JSONObject payload, String ecosystem, OfferType offerType) throws JsonProcessingException;
}

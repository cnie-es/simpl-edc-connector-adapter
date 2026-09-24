package eu.europa.ec.simpl.edcconnectoradapter.model;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OfferIdBuilder {

    private static final String CONTRACT_DEF_ASSET_ID_PREFIX = "contractDefinitionId:assetId|";
    private static final int OFFER_ID_PARTS = 3;

    private static final char COLON = ':';
    private static final Pattern COLON_PATTERN = Pattern.compile(String.valueOf(COLON));

    private static final int CONTRACT_DEFINITION_ID_INDEX = 0;
    private static final int ASSET_ID_INDEX = 1;

    @Getter
    private final String contractDefinitionId;

    @Getter
    private final String assetId;

    /**
     *
     * @param offerId expected 3 base64 encoded parts separated by colon (:) where first part is
     * the contractDefinitionId and second part is the assetId,
     * example: ZWI5NjU3MTktZmVkMy00MjBhLThlODMtMzEyZWFhZDk5ZDRh:MDBlODgzNTgtYzRmZi00MTJkLWI2ZmItNWQ1NGIyOTA5YjAy:ZmRjMWNhMDctYmZjNy00YWMzLTg2YWQtZjQ2ZTJjOThkYmEy
     * @return
     */
    public static OfferIdBuilder create(String offerId) {
        String[] offerIdParts = COLON_PATTERN.split(offerId);
        if (offerIdParts.length != OFFER_ID_PARTS) {
            throw new IllegalArgumentException(
                    "Invalid offerId format in provider catalog, expected 3 parts but received " + offerIdParts.length);
        }
        return new OfferIdBuilder(
                decode(offerIdParts, CONTRACT_DEFINITION_ID_INDEX), decode(offerIdParts, ASSET_ID_INDEX));
    }

    /**
     * returns a string in the format contractDefinitionId:assetId|{contractDefinitionId}:{assetId}
     * @return
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(CONTRACT_DEF_ASSET_ID_PREFIX);
        sb.append(contractDefinitionId);
        sb.append(COLON);
        sb.append(assetId);
        return sb.toString();
    }

    private static String decode(String[] offerIdParts, int assetIdIndex) {
        return new String(Base64.getDecoder().decode(offerIdParts[assetIdIndex]), StandardCharsets.UTF_8);
    }
}

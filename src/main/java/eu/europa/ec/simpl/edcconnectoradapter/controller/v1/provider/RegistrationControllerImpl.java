package eu.europa.ec.simpl.edcconnectoradapter.controller.v1.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europa.ec.simpl.data1.common.constant.CommonConstants;
import eu.europa.ec.simpl.data1.common.controller.AbstractController;
import eu.europa.ec.simpl.data1.common.enumeration.OfferType;
import eu.europa.ec.simpl.data1.common.logging.LogRequest;
import eu.europa.ec.simpl.data1.common.util.JsonUtil;
import eu.europa.ec.simpl.edcconnectoradapter.service.provider.registration.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Profile({"provider", "build", "test"})
@RestController("registrationControllerV1")
@Log4j2
@RequiredArgsConstructor
public class RegistrationControllerImpl extends AbstractController implements RegistrationController {

    private final RegistrationService registrationService;

    @LogRequest
    @Override
    @Deprecated(since = "v1.14.0", forRemoval = true)
    /**
     * @deprecated to be removed when enrichAndValidate on SdTooling SD Controller V2 will be removed
     */
    public ResponseEntity<String> register(String sdJsonLd, OfferType offeringType) throws JsonProcessingException {
        log.debug(
                "register(): invoking registrationService.registerV1() for offeringType '{}' and sdJsonLd {}",
                offeringType,
                sdJsonLd);

        JSONObject sdJsonLdObj = JsonUtil.createJSONObjectFromSD(sdJsonLd);
        sdJsonLdObj = registrationService.registerV1(sdJsonLdObj, CommonConstants.ECOSYSTEM, offeringType);
        log.debug("register(): invoking registrationService.enrich() for sdJsonLd {}", sdJsonLd);

        sdJsonLdObj = registrationService.enrich(sdJsonLdObj, CommonConstants.ECOSYSTEM);

        log.debug("register(): returning OK response with sdJsonLd {}", sdJsonLdObj);
        return ResponseEntity.ok(sdJsonLdObj.toString());
    }
}

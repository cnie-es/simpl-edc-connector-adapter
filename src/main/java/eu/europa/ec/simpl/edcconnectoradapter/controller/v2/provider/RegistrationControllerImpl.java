package eu.europa.ec.simpl.edcconnectoradapter.controller.v2.provider;

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
@RestController("registrationControllerV2")
@Log4j2
@RequiredArgsConstructor
public class RegistrationControllerImpl extends AbstractController implements RegistrationController {

    private final RegistrationService registrationService;

    @LogRequest
    @Override
    public ResponseEntity<String> register(String payload, OfferType offeringType) throws JsonProcessingException {
        log.debug(
                "register(): invoking registrationService.registerV2() for offeringType '{}' and payload {}",
                offeringType,
                payload);

        JSONObject payloadObj = JsonUtil.createJSONObjectFromPayload(payload, null);
        JSONObject sdJsonLdObj = registrationService.registerV2(payloadObj, CommonConstants.ECOSYSTEM, offeringType);
        log.debug("register(): invoking registrationService.enrich() for sdJsonLd {}", sdJsonLdObj);

        sdJsonLdObj = registrationService.enrich(sdJsonLdObj, CommonConstants.ECOSYSTEM);

        log.debug("register(): returning OK response with sdJsonLd {}", sdJsonLdObj);
        return ResponseEntity.ok(sdJsonLdObj.toString());
    }
}

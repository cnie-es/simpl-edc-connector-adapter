package eu.europa.ec.simpl.edcconnectoradapter.controller.v1.provider;

import eu.europa.ec.simpl.data1.common.adapter.connector.model.configuration.Participant;
import eu.europa.ec.simpl.data1.common.controller.AbstractController;
import eu.europa.ec.simpl.data1.common.logging.LogRequest;
import eu.europa.ec.simpl.edcconnectoradapter.service.provider.configuration.ConfigurationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Profile({"provider", "build", "test"})
@RestController("configurationControllerV1")
@Log4j2
@RequiredArgsConstructor
public class ConfigurationControllerImpl extends AbstractController implements ConfigurationController {

    private final ConfigurationService configurationService;

    @LogRequest
    @Override
    public ResponseEntity<Participant> getParticipant() {
        log.debug("getParticipant(): invoking configurationService.getParticipant()");
        Participant result = configurationService.getParticipant();

        log.debug("getParticipant(): returning {}", result);
        return ResponseEntity.ok(result);
    }
}

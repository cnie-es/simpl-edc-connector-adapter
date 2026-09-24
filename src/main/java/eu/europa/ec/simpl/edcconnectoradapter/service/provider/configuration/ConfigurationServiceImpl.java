package eu.europa.ec.simpl.edcconnectoradapter.service.provider.configuration;

import eu.europa.ec.simpl.data1.common.adapter.connector.model.configuration.Participant;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile({"provider", "build", "test"})
@Service
@Log4j2
public class ConfigurationServiceImpl implements ConfigurationService {

    @Value("${edc-connector.participant.id}")
    private String edcConnectorParticipantId;

    @Override
    @Cacheable("EDCConnectorService.getParticipant()")
    public Participant getParticipant() {
        log.debug("getParticipant(): returning configured edcConnectorParticipantId '{}'", edcConnectorParticipantId);
        return Participant.builder()
                // hoping in the future we can get this value from an EDC connector API
                .id(edcConnectorParticipantId)
                .build();
    }
}

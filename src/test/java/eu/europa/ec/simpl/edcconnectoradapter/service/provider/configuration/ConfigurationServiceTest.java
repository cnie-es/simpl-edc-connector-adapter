package eu.europa.ec.simpl.edcconnectoradapter.service.provider.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europa.ec.simpl.data1.common.adapter.connector.model.configuration.Participant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ConfigurationServiceTest {

    private static final String PARTICIPANT_ID = "test-participant-id";

    // using the impl type to access protected fiels and methods
    private ConfigurationServiceImpl configurationService;

    @BeforeEach
    void setUp() {
        configurationService = new ConfigurationServiceImpl();
        ReflectionTestUtils.setField(configurationService, "edcConnectorParticipantId", PARTICIPANT_ID);
    }

    @Test
    void getParticipant() {
        Participant participant = configurationService.getParticipant();

        assertNotNull(participant, "Participant should not be null");
        assertEquals(PARTICIPANT_ID, participant.getId(), "Participant ID should match configured value");
    }
}

package eu.europa.ec.simpl.edcconnectoradapter.service.provider.configuration;

import eu.europa.ec.simpl.data1.common.adapter.connector.model.configuration.Participant;

public interface ConfigurationService {

    /**
     * Returns the connector participant (id) configured in the EDC connector.
     * @return
     */
    Participant getParticipant();
}

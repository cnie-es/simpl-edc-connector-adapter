package eu.europa.ec.simpl.edcconnectoradapter.logging;

import static eu.europa.ec.simpl.common_logging.MessageBuilder.buildMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.simpl.common_logging.levels.BusinessLevel;
import eu.europa.ec.simpl.common_logging.types.MessageType;
import eu.europa.ec.simpl.common_logging.types.StructuredLogMessageBuilder;
import eu.europa.ec.simpl.edcconnectoradapter.constant.ContractBusinessOperation;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract.ContractRecordLog;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Emits an enriched contract-negotiation record as a structured business-level log. The record is meant to be
 * collected into Elasticsearch and consumed by the Clearing House, mirroring {@link TransferBusinessLogger} for the
 * transfer flow.
 *
 * <p>The message is crafted so the Clearing House can extract two independent signals from a single log line:
 *
 * <ul>
 *   <li>a negotiation <b>state transition</b>, via the {@code "ContractNegotiation <id> is now in state <STATE>"}
 *       prefix (only when a state is present), which is the same shape the EDC connector emits natively and the
 *       Clearing House already parses; and
 *   <li>the <b>enriched record</b>, via the {@link #RECORD_MARKER} marker followed by the JSON blob, so a downstream
 *       parser can resolve who was involved (consumer, provider) and over which asset/offer the negotiation ran.
 * </ul>
 *
 * <p>The enriched fields are also exposed as structured {@code customFields} so each value is independently
 * queryable in Elasticsearch.
 */
@Component
@RequiredArgsConstructor
public class ContractBusinessLogger {

    /** Marker prefix in the log message, used by downstream parsers to identify enriched contract records. */
    public static final String RECORD_MARKER = "ContractRecord";

    private static final Logger LOGGER = LogManager.getLogger(ContractBusinessLogger.class);
    private static final String ORIGIN = "eu.europa.ec.simpl.edcconnectoradapter";

    private final ObjectMapper objectMapper;

    public void log(ContractBusinessOperation businessOperation, ContractRecordLog record) {
        LOGGER.log(
                BusinessLevel.BUSINESS,
                buildMessage(StructuredLogMessageBuilder.builder()
                        .origin(ORIGIN)
                        .destination(ORIGIN)
                        .businessOperation(businessOperation.description())
                        .messageType(MessageType.REQUEST)
                        .correlationId(record.getContractNegotiationId())
                        .customFields(record.toCustomFields())
                        .msg(buildMessageBody(record))
                        .build()));
    }

    /**
     * Builds the human/parseable message body combining the EDC-style state-transition prefix (when a state is
     * present) with the enriched record marker and its JSON representation.
     */
    private String buildMessageBody(ContractRecordLog record) {
        StringBuilder body = new StringBuilder("ContractNegotiation ").append(record.getContractNegotiationId());
        if (record.getState() != null && !record.getState().isBlank()) {
            body.append(" is now in state ").append(record.getState());
        }
        body.append(' ').append(RECORD_MARKER).append(' ').append(toJson(record));
        return body.toString();
    }

    private String toJson(ContractRecordLog record) {
        try {
            return objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            // Never let logging serialization break the caller; fall back to the object's representation.
            return String.valueOf(record);
        }
    }
}

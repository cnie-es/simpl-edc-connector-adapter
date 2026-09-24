package eu.europa.ec.simpl.edcconnectoradapter.logging;

import static eu.europa.ec.simpl.common_logging.MessageBuilder.buildMessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.simpl.common_logging.levels.BusinessLevel;
import eu.europa.ec.simpl.common_logging.types.MessageType;
import eu.europa.ec.simpl.common_logging.types.StructuredLogMessageBuilder;
import eu.europa.ec.simpl.edcconnectoradapter.constant.TransferBusinessOperation;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.transfer.TransferRecordLog;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Emits an enriched transfer record as a structured business-level technical log. The log is collected into
 * Elasticsearch (technical-logs-*) and is meant to be consumed by the Clearing House, mirroring how the contract
 * module logs {@code contractAgreementCreateTO}.
 *
 * <p>The message is crafted so the Clearing House can extract two independent signals from a single log line:
 *
 * <ul>
 *   <li>a transfer <b>state transition</b>, via the {@code "TransferProcess <id> is now in state <STATE>"} prefix,
 *       which is the same shape the EDC connector emits natively and the Clearing House already parses; and
 *   <li>the <b>enriched record</b>, via the {@link #RECORD_MARKER} marker followed by the JSON blob, so a downstream
 *       parser can resolve who was involved (consumer, provider) and what asset/agreement was transferred.
 * </ul>
 *
 * <p>The enriched fields are also exposed as structured {@code customFields} so each value is independently
 * queryable in Elasticsearch.
 */
@Component
@RequiredArgsConstructor
public class TransferBusinessLogger {

    /** Marker prefix in the log message, used by downstream parsers to identify enriched transfer records. */
    public static final String RECORD_MARKER = "TransferRecord";

    private static final Logger LOGGER = LogManager.getLogger(TransferBusinessLogger.class);
    private static final String ORIGIN = "eu.europa.ec.simpl.edcconnectoradapter";

    private final ObjectMapper objectMapper;

    public void log(TransferBusinessOperation businessOperation, TransferRecordLog record) {
        LOGGER.log(
                BusinessLevel.BUSINESS,
                buildMessage(StructuredLogMessageBuilder.builder()
                        .origin(ORIGIN)
                        .destination(ORIGIN)
                        .businessOperation(businessOperation.description())
                        .messageType(MessageType.REQUEST)
                        .correlationId(record.getTransferProcessId())
                        .customFields(record.toCustomFields())
                        .msg(buildMessageBody(record))
                        .build()));
    }

    /**
     * Builds the human/parseable message body combining the EDC-style state-transition prefix (when a state is
     * present) with the enriched record marker and its JSON representation.
     */
    private String buildMessageBody(TransferRecordLog record) {
        StringBuilder body = new StringBuilder("TransferProcess ").append(record.getTransferProcessId());
        if (record.getState() != null && !record.getState().isBlank()) {
            body.append(" is now in state ").append(record.getState());
        }
        body.append(' ').append(RECORD_MARKER).append(' ').append(toJson(record));
        return body.toString();
    }

    private String toJson(TransferRecordLog record) {
        try {
            return objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            // Never let logging serialization break the caller; fall back to the object's representation.
            return String.valueOf(record);
        }
    }
}

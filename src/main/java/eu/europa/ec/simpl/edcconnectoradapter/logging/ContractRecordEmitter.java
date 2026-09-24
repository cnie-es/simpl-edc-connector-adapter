package eu.europa.ec.simpl.edcconnectoradapter.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.simpl.edcconnectoradapter.constant.ContractBusinessOperation;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract.ContractRecordLog;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Emits an enriched contract-negotiation record. It always writes a BUSINESS-level log (kept as a local audit trace)
 * and, when a Kafka template and a dedicated topic are available, also publishes the record to that topic — the
 * federation-ready ingestion path consumed by the monitoring module's Logstash pipeline, which persists the record
 * into the {@code contract-records-*} index that the Clearing House reads.
 *
 * <p>Unlike the transfer flow (which splits a base service and a Kafka-enabled subclass), the contract negotiation
 * service is a single bean shared across profiles, so the optional Kafka path is handled here: the {@link KafkaTemplate}
 * is resolved lazily via an {@link ObjectProvider} (absent in profiles without Kafka) and the topic defaults to empty
 * (publishing disabled). Publishing is best-effort and never breaks the negotiation flow.
 */
@Component
@Log4j2
public class ContractRecordEmitter {

    // dedicated topic for enriched contract records, ingested by the monitoring module into contract-records-*
    @Value("${kafka.contract-record.topic:}")
    private String contractRecordTopic;

    private final ContractBusinessLogger contractBusinessLogger;
    private final ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider;
    private final ObjectMapper objectMapper;

    public ContractRecordEmitter(
            ContractBusinessLogger contractBusinessLogger,
            ObjectProvider<KafkaTemplate<String, Object>> kafkaTemplateProvider,
            ObjectMapper objectMapper) {
        this.contractBusinessLogger = contractBusinessLogger;
        this.kafkaTemplateProvider = kafkaTemplateProvider;
        this.objectMapper = objectMapper;
    }

    public void emit(ContractBusinessOperation businessOperation, ContractRecordLog record) {
        contractBusinessLogger.log(businessOperation, record);
        publishToKafka(record);
    }

    private void publishToKafka(ContractRecordLog record) {
        if (contractRecordTopic == null || contractRecordTopic.isBlank()) {
            return;
        }
        KafkaTemplate<String, Object> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        if (kafkaTemplate == null) {
            return;
        }
        try {
            ProducerRecord<String, Object> producerRecord =
                    new ProducerRecord<>(contractRecordTopic, objectMapper.writeValueAsString(record));
            log.debug("publishToKafka(): invoking kafkaTemplate.send() for {}", producerRecord);
            kafkaTemplate.send(producerRecord);
        } catch (Exception e) {
            log.warn(
                    "publishToKafka(): could not publish contract record to topic {}: {}",
                    contractRecordTopic,
                    e.toString());
        }
    }
}

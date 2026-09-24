package eu.europa.ec.simpl.edcconnectoradapter.config;

import eu.europa.ec.simpl.data1.common.exception.KafkaNotRetryableException;
import eu.europa.ec.simpl.data1.common.exception.KafkaRetryableException;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.plain.PlainLoginModule;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;

@Profile("consumer")
@Configuration
@EnableKafka
@Log4j2
public class KafkaConfig {

    public static final String SASL_MECHANISM_PLAIN = "PLAIN";

    private static final String SASL_JAAS_CONFIG_FORMAT = "%s required username=\"%s\" password=\"%s\";";

    private static final int ADMIN_OPERATION_TIMEOUT_SECS = 5;

    private static final int ADMIN_CLOSE_TIMEOUT_SECS = 5;

    public enum AuthType {
        SASL_PLAINTEXT,
        DO_NOT_USE_THIS_TYPE // just to avoid sonar blocking issue about enum with single value
    }

    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapAddress;

    @Value(value = "${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value(value = "${kafka.transfer-status.backoff.max-attempts}")
    private int transferStatusBackoffMaxAttempts;

    @Value(value = "${kafka.transfer-status.backoff.interval}")
    private long transferStatusBackoffInterval;

    @Value(value = "${kafka.transfer-status.backoff.multiplier}")
    private double transferStatusBackoffMultiplier;

    @Value(value = "${kafka.transfer-deprovisioning.backoff.max-attempts}")
    private int transferDeprovisioningBackoffMaxAttempts;

    @Value(value = "${kafka.transfer-deprovisioning.backoff.interval}")
    private long transferDeprovisioningBackoffInterval;

    @Value(value = "${kafka.transfer-deprovisioning.backoff.multiplier}")
    private double transferDeprovisioningBackoffMultiplier;

    @Value(value = "${kafka.auth.type}")
    private AuthType authType;

    @Value(value = "${kafka.auth.sasl.username}")
    private String username;

    @Value(value = "${kafka.auth.sasl.password}")
    private String password;

    @Value(value = "${kafka.transfer-status.topic}")
    private String connectionVerificationTopic;

    @Value(value = "${kafka.fatal-if-broker-not-available}")
    private boolean fatalIfBrokerNotAvailable;

    @Bean
    KafkaAdmin kafkaAdmin() {
        Map<String, Object> config = new HashMap<>();
        setupCommons(config);
        config.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, "3000");

        KafkaAdmin kafkaAdmin = new KafkaAdmin(config);
        // this will work only if a NewTopic bean is defined (see newTopic())
        kafkaAdmin.setFatalIfBrokerNotAvailable(fatalIfBrokerNotAvailable);

        // reduce the time for admin operations
        kafkaAdmin.setOperationTimeout(ADMIN_OPERATION_TIMEOUT_SECS);
        kafkaAdmin.setCloseTimeout(ADMIN_CLOSE_TIMEOUT_SECS);

        return kafkaAdmin;
    }

    /**
     * This is the only way to enable the kafka connection verification during the application startup (see kafkaAdmin.fatalIfBrokerNotAvailable)
     * @return
     */
    @Bean
    NewTopic newTopic() {
        return new NewTopic(connectionVerificationTopic, 1, (short) 1);
    }

    @Bean
    KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        setupCommons(config);

        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();

        setupCommons(props);

        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, ErrorHandlingDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, String> transferStatusListenerContainerFactory(
            @Value("${kafka.consumerThreads:1}") int consumerThreads) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(consumerThreads);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setCommonErrorHandler(transferStatusErrorHandler());
        return factory;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, String> transferDeprovisioningListenerContainerFactory(
            @Value("${kafka.consumerThreads:1}") int consumerThreads) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(consumerThreads);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        factory.setCommonErrorHandler(transferDeprovisioningErrorHandler());
        return factory;
    }

    @Bean
    DefaultErrorHandler transferStatusErrorHandler() {
        ExponentialBackOff backOff =
                new ExponentialBackOff(transferStatusBackoffInterval, transferStatusBackoffMultiplier);
        backOff.setMaxAttempts(transferStatusBackoffMaxAttempts);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (consumerRecord, exception) -> log.error(
                        "transferStatusErrorHandler(): backoff retry attempts ({}) exceed processing record: {}",
                        transferStatusBackoffMaxAttempts,
                        consumerRecord.value()),
                backOff);
        errorHandler.addRetryableExceptions(KafkaRetryableException.class);
        errorHandler.addNotRetryableExceptions(KafkaNotRetryableException.class);
        return errorHandler;
    }

    @Bean
    DefaultErrorHandler transferDeprovisioningErrorHandler() {
        ExponentialBackOff backOff =
                new ExponentialBackOff(transferDeprovisioningBackoffInterval, transferDeprovisioningBackoffMultiplier);
        backOff.setMaxAttempts(transferDeprovisioningBackoffMaxAttempts);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (consumerRecord, exception) -> {
                    Throwable cause = exception.getCause();
                    if (cause instanceof KafkaNotRetryableException) {
                        log.error(
                                "transferDeprovisioningErrorHandler(): not retryable exception for record {}: {}",
                                consumerRecord.value(),
                                cause);
                    } else {
                        log.error(
                                "transferDeprovisioningErrorHandler(): backoff retry attempts ({}) exceed processing record {}",
                                transferDeprovisioningBackoffMaxAttempts,
                                consumerRecord.value());
                    }
                },
                backOff);
        errorHandler.addRetryableExceptions(KafkaRetryableException.class);
        errorHandler.addNotRetryableExceptions(KafkaNotRetryableException.class);
        return errorHandler;
    }

    private void setupCommons(Map<String, Object> config) {
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);

        if (authType == AuthType.SASL_PLAINTEXT) {
            setupSASL(config);
        }
    }

    private void setupSASL(Map<String, Object> config) {
        log.info("setupSASL() with PLAIN mechanism");
        config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        config.put(SaslConfigs.SASL_MECHANISM, SASL_MECHANISM_PLAIN);
        config.put(
                SaslConfigs.SASL_JAAS_CONFIG,
                String.format(SASL_JAAS_CONFIG_FORMAT, PlainLoginModule.class.getName(), username, password));
    }
}

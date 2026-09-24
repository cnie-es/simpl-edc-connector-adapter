package eu.europa.ec.simpl.edcconnectoradapter.service.consumer.transferprocess;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferProcessId;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferRequest;
import eu.europa.ec.simpl.data1.common.exception.KafkaNotRetryableException;
import eu.europa.ec.simpl.data1.common.exception.KafkaRetryableException;
import eu.europa.ec.simpl.edcconnectoradapter.client.edcconnector.EDCConnectorTransferClient;
import eu.europa.ec.simpl.edcconnectoradapter.logging.TransferBusinessLogger;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.common.response.EdcAcknowledgementId;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.transfer.request.EdcTransferRequest;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.transfer.response.EdcTransferProcess;
import eu.europa.ec.simpl.edcconnectoradapter.service.consumer.contractnegotiation.ContractNegotiationService;
import feign.FeignException;
import feign.FeignException.FeignClientException;
import java.util.stream.Stream;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TransferProcessServiceKafkaTest {

    private static final String EDC_CONNECTOR_BASE_URL = "http://connectorUrl";

    private static final String DATA_DESTINATION_MINIO_S3 =
            """
    {
        "providerEndpoint": "https://edc-provider.endpoint.eu/protocol",
        "contractId": "contract-id-0001",
        "templateId": "5",
        "dataDestination": {
            "objectName": "objectname.txt",
            "type": "MinioS3",
            "endpoint": "http://minio.endpoint.eu",
            "bucketName": "bucket-name",
            "consumerEmail": "consumer@email.com",
            "deprovisioningEnabled": false
        }
    }
    """;

    private static final String DATA_DESTINATION_IONOS_S3 =
            """
    {
        "providerEndpoint": "https://edc-provider.endpoint.eu/protocol",
        "contractId": "contract-id-0001",
        "templateId": "1",
        "dataDestination": {
            "type": "IonosS3",
            "region": "de",
            "storage": "ionoss3.storage.com",
            "bucketName": "bucket-name",
            "objectName": "objectname.txt",
            "path": "folder1/",
            "keyName": "test-key-name",
            "consumerEmail": "consumer@email.com",
            "deprovisioningEnabled": true
        }
    }
    """;

    private static final String DATA_DESTINATION_MINIO_S3_NO_DEPROVISIONING_ENABLED_FLAG =
            """
	{
	    "providerEndpoint": "https://edc-provider.endpoint.eu/protocol",
	    "contractId": "contract-id-0001",
	    "templateId": "5",
	    "dataDestination": {
	        "objectName": "objectname.txt",
	        "type": "MinioS3",
	        "endpoint": "http://minio.endpoint.eu",
	        "bucketName": "bucket-name",
	        "consumerEmail": "consumer@email.com"
	    }
	}
	""";

    @Mock
    private EDCConnectorTransferClient edcConnectorTransferClient;

    @Mock
    private ContractNegotiationService contractNegotiationService;

    @Mock
    private TransferBusinessLogger transferBusinessLogger;

    @Mock
    private eu.europa.ec.simpl.edcconnectoradapter.client.authprovider.ParticipantPingClient participantPingClient;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    // using the impl type to access protected fiels and methods
    private TransferProcessServiceKafkaImpl transferProcessService;

    @Mock
    private ObjectMapper objectMapperMock;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUpEach() {
        transferProcessService = new TransferProcessServiceKafkaImpl(
                edcConnectorTransferClient,
                contractNegotiationService,
                transferBusinessLogger,
                participantPingClient,
                kafkaTemplate,
                objectMapper);
        ReflectionTestUtils.setField(transferProcessService, "edcConnectorBaseUrl", EDC_CONNECTOR_BASE_URL);
        ReflectionTestUtils.setField(transferProcessService, "edcConnectorApiKeyValue", "apikeydefaultvalue");

        ReflectionTestUtils.setField(transferProcessService, "transferStatusTopic", "transferStatusTopic");
        ReflectionTestUtils.setField(
                transferProcessService, "transferDeprovisioningTopic", "transferDeprovisioningTopic");
        ReflectionTestUtils.setField(transferProcessService, "transferRecordTopic", "transferRecordTopic");
    }

    private static Stream<Arguments> testStartTransferParameters() {
        return Stream.of(
                Arguments.of(DATA_DESTINATION_IONOS_S3),
                Arguments.of(DATA_DESTINATION_MINIO_S3),
                Arguments.of(DATA_DESTINATION_MINIO_S3_NO_DEPROVISIONING_ENABLED_FLAG));
    }

    @ParameterizedTest
    @MethodSource("testStartTransferParameters")
    void testStartTransfer(String transferRequestJson) throws JsonProcessingException {
        TransferRequest transferRequest = objectMapper.readValue(transferRequestJson, TransferRequest.class);

        ArgumentCaptor<EdcTransferRequest> edcTransferRequestCaptor = ArgumentCaptor.forClass(EdcTransferRequest.class);

        when(edcConnectorTransferClient.startTransferProcess(any(), any(), any()))
                .thenReturn(EdcAcknowledgementId.builder().id("transfer-id").build());
        TransferProcessId result = transferProcessService.startTransfer(transferRequest);
        assertNotNull(result);
        assertEquals("transfer-id", result.getTransferId());

        // deprovisioningEnabled must be removed if present in the dataDestination
        verify(edcConnectorTransferClient).startTransferProcess(any(), any(), edcTransferRequestCaptor.capture());
        JsonNode capturedDataDestination = edcTransferRequestCaptor.getValue().getDataDestination();
        assertNotNull(capturedDataDestination);
        assertFalse(
                capturedDataDestination.has("deprovisioningEnabled"),
                "deprovisioningEnabled must be removed from dataDestination before sending to EDC");
    }

    @Test
    void testStartTransferPublishesEnrichedTransferRecord() throws Exception {
        TransferRequest transferRequest = objectMapper.readValue(DATA_DESTINATION_MINIO_S3, TransferRequest.class);

        when(edcConnectorTransferClient.startTransferProcess(any(), any(), any()))
                .thenReturn(EdcAcknowledgementId.builder().id("transfer-id").build());
        when(edcConnectorTransferClient.getTransferProcess(any(), any(), any()))
                .thenReturn(EdcTransferProcess.builder()
                        .transferProcessId("transfer-id")
                        .assetId("asset:dataset:weather-2025")
                        .transferType("HttpData-PULL")
                        .state("STARTED")
                        .stateTimestamp(1749615000000L)
                        .build());

        transferProcessService.startTransfer(transferRequest);

        // MinioS3 destination has deprovisioningEnabled=false, so only the enriched transfer record is produced
        ArgumentCaptor<ProducerRecord> producerRecordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(producerRecordCaptor.capture());

        ProducerRecord<?, ?> producerRecord = producerRecordCaptor.getValue();
        assertEquals("transferRecordTopic", producerRecord.topic());
        String payload = (String) producerRecord.value();
        assertTrue(payload.contains("\"transferProcessId\":\"transfer-id\""));
        assertTrue(payload.contains("\"assetId\":\"asset:dataset:weather-2025\""));
        assertTrue(payload.contains("\"state\":\"STARTED\""));
    }

    private static Stream<Arguments> testKafkaTranferStatusConsumerParameters() {
        return Stream.of(Arguments.of("COMPLETED"), Arguments.of("NOT_COMPLETED"));
    }

    @ParameterizedTest
    @MethodSource("testKafkaTranferStatusConsumerParameters")
    void testKafkaTranferStatusConsumer(String state) throws Exception {
        ConsumerRecord<String, String> consumerRecord = mock(ConsumerRecord.class);
        when(consumerRecord.value()).thenReturn("{\"transferProcessId\":\"1234\"}");

        when(edcConnectorTransferClient.getTransferProcess(any(), any(), any()))
                .thenReturn(EdcTransferProcess.builder()
                        .transferProcessId("transfer-id")
                        .state(state)
                        .build());

        if (state.equals("COMPLETED")) {
            assertDoesNotThrow(() -> transferProcessService.kafkaTranferStatusConsumer(consumerRecord));
            // two messages are produced: the enriched transfer record and the deprovisioning trigger
            verify(kafkaTemplate, times(2)).send(any(ProducerRecord.class));
        } else {
            assertThrows(
                    KafkaRetryableException.class,
                    () -> transferProcessService.kafkaTranferStatusConsumer(consumerRecord));
        }
    }

    @Test
    void testKafkaTranferDeprovisioningConsumer() throws Exception {
        ConsumerRecord<String, String> consumerRecord = mock(ConsumerRecord.class);
        when(consumerRecord.value()).thenReturn("{\"transferProcessId\":\"1234\"}");

        when(edcConnectorTransferClient.deprovisioningTransferProcess(any(), any(), any()))
                .thenReturn(new Object());

        transferProcessService.kafkaTranferDeprovisioningConsumer(consumerRecord);

        verify(edcConnectorTransferClient).deprovisioningTransferProcess(any(), any(), eq("1234"));
    }

    private static Stream<Arguments> testKafkaTranferDeprovisioningConsumerWithFeignExceptionParameters() {
        return Stream.of(Arguments.of(FeignException.Conflict.class), Arguments.of(FeignException.NotFound.class));
    }

    @ParameterizedTest
    @MethodSource("testKafkaTranferDeprovisioningConsumerWithFeignExceptionParameters")
    void testKafkaTranferDeprovisioningConsumerWithFeignException(
            Class<? extends FeignClientException> exceptionClass) {
        ConsumerRecord<String, String> consumerRecord = mock(ConsumerRecord.class);
        when(consumerRecord.value()).thenReturn("{\"transferProcessId\":\"1234\"}");

        when(edcConnectorTransferClient.deprovisioningTransferProcess(any(), any(), any()))
                .thenThrow(exceptionClass);

        assertThrows(
                KafkaNotRetryableException.class,
                () -> transferProcessService.kafkaTranferDeprovisioningConsumer(consumerRecord));
    }

    @Test
    void testKafkaTranferDeprovisioningConsumerWithException() {
        ConsumerRecord<String, String> consumerRecord = mock(ConsumerRecord.class);
        when(consumerRecord.value()).thenReturn("{\"transferProcessId\":\"1234\"}");

        when(edcConnectorTransferClient.deprovisioningTransferProcess(any(), any(), any()))
                .thenThrow(new RuntimeException("test"));

        assertThrows(
                KafkaRetryableException.class,
                () -> transferProcessService.kafkaTranferDeprovisioningConsumer(consumerRecord));
    }

    @Test
    void testStartTransferWithJsonProcessingException() throws JsonProcessingException {
        ReflectionTestUtils.setField(transferProcessService, "objectMapper", objectMapperMock);

        TransferRequest transferRequest = objectMapper.readValue(DATA_DESTINATION_IONOS_S3, TransferRequest.class);

        when(edcConnectorTransferClient.startTransferProcess(any(), any(), any()))
                .thenReturn(EdcAcknowledgementId.builder().id("transfer-id").build());

        when(objectMapperMock.writeValueAsString(any())).thenThrow(JsonProcessingException.class);

        // @SneakyThrows di JsonProcessingException
        assertThrows(JsonProcessingException.class, () -> transferProcessService.startTransfer(transferRequest));
    }
}

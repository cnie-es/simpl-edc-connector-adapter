package eu.europa.ec.simpl.edcconnectoradapter.service.consumer.transferprocess;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferProcess;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferProcessId;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferRequest;
import eu.europa.ec.simpl.data1.common.exception.RemoteServiceErrorException;
import eu.europa.ec.simpl.edcconnectoradapter.client.edcconnector.EDCConnectorTransferClient;
import eu.europa.ec.simpl.edcconnectoradapter.constant.TransferBusinessOperation;
import eu.europa.ec.simpl.edcconnectoradapter.logging.TransferBusinessLogger;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.common.response.EdcAcknowledgementId;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract.response.EdcContractAgreement;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.transfer.TransferRecordLog;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.transfer.request.EdcTransferRequest;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.transfer.response.EdcTransferProcess;
import eu.europa.ec.simpl.edcconnectoradapter.service.consumer.contractnegotiation.ContractNegotiationService;
import feign.FeignException;
import java.util.stream.Stream;
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
class TransferProcessServiceTest {

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
    private TransferProcessServiceImpl transferProcessService;

    @Mock
    private ObjectMapper objectMapperMock;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUpEach() {
        transferProcessService = new TransferProcessServiceImpl(
                edcConnectorTransferClient, contractNegotiationService, transferBusinessLogger, participantPingClient);
        ReflectionTestUtils.setField(transferProcessService, "edcConnectorBaseUrl", EDC_CONNECTOR_BASE_URL);
        ReflectionTestUtils.setField(transferProcessService, "edcConnectorApiKeyValue", "apikeydefaultvalue");
        // configured participant id acts only as a fallback; the EDC contract agreement takes precedence
        ReflectionTestUtils.setField(transferProcessService, "participantId", "urn:participant:fallback");
    }

    @Test
    void testInit() {
        assertDoesNotThrow(() -> transferProcessService.init());
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
    void testStartTransferEmitsEnrichedBusinessLog() throws JsonProcessingException {
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

        when(contractNegotiationService.getContractAgreement(anyString()))
                .thenReturn(EdcContractAgreement.builder()
                        .contractAgreementId("agreement-001")
                        .providerId("urn:participant:telefonica")
                        .consumerId("urn:participant:telefonica")
                        .build());

        when(participantPingClient.resolveByEndpoint(anyString()))
                .thenReturn(new eu.europa.ec.simpl.edcconnectoradapter.client.authprovider.ParticipantPingClient
                        .ParticipantIdentity("019e3a90-7322-7c5f-b152-5819e6ff64cc", "Telefonica"));
        when(participantPingClient.resolveOwn())
                .thenReturn(new eu.europa.ec.simpl.edcconnectoradapter.client.authprovider.ParticipantPingClient
                        .ParticipantIdentity("019e3a8c-13eb-707e-a88b-226c641f44ed", "Telefonica"));

        transferProcessService.startTransfer(transferRequest);

        ArgumentCaptor<TransferRecordLog> recordCaptor = ArgumentCaptor.forClass(TransferRecordLog.class);
        verify(transferBusinessLogger)
                .log(eq(TransferBusinessOperation.TRANSFER_INITIATED), recordCaptor.capture());

        TransferRecordLog record = recordCaptor.getValue();
        assertNotNull(record);
        assertEquals("transfer-id", record.getTransferProcessId());
        assertEquals("asset:dataset:weather-2025", record.getAssetId());
        assertEquals("HttpData-PULL", record.getTransferType());
        assertEquals("STARTED", record.getState());
        assertEquals("agreement-001", record.getContractAgreementId());
        assertEquals("urn:participant:telefonica", record.getProviderId());
        assertEquals("019e3a90-7322-7c5f-b152-5819e6ff64cc", record.getProviderParticipantId());
        assertEquals("Telefonica", record.getProviderName());
        assertEquals("urn:participant:telefonica", record.getConsumerId());
        assertEquals("019e3a8c-13eb-707e-a88b-226c641f44ed", record.getConsumerParticipantId());
        assertEquals("Telefonica", record.getConsumerName());
        assertEquals("contract-id-0001", record.getContractId());
    }

    @Test
    void testGetTransferProcessWithCompletedStatus() {
        when(edcConnectorTransferClient.getTransferProcess(any(), any(), any()))
                .thenReturn(EdcTransferProcess.builder()
                        .transferProcessId("transfer-id")
                        .state("COMPLETED")
                        .build());

        TransferProcess response = transferProcessService.getTransferProcess("transfer-id");
        assertNotNull(response);
        assertEquals("transfer-id", response.getTransferProcessId());
        assertEquals("COMPLETED", response.getState());
        assertEquals("COMPLETED", response.getFinalState());
    }

    @Test
    void testStartTransferWithFeignException() throws JsonProcessingException {
        TransferRequest transferRequest = objectMapper.readValue(DATA_DESTINATION_IONOS_S3, TransferRequest.class);

        when(edcConnectorTransferClient.startTransferProcess(any(), any(), any()))
                .thenThrow(FeignException.Unauthorized.class);

        assertThrows(RemoteServiceErrorException.class, () -> transferProcessService.startTransfer(transferRequest));
    }

    @Test
    void testGetTransferProcessWithFeignException() {
        when(edcConnectorTransferClient.getTransferProcess(any(), any(), any()))
                .thenThrow(FeignException.Unauthorized.class);

        assertThrows(RemoteServiceErrorException.class, () -> transferProcessService.getTransferProcess("transfer-id"));
    }
}

package eu.europa.ec.simpl.edcconnectoradapter.controller.v1.consumer;

import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferProcess;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferProcessId;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferRequest;
import eu.europa.ec.simpl.data1.common.controller.AbstractController;
import eu.europa.ec.simpl.data1.common.logging.LogRequest;
import eu.europa.ec.simpl.edcconnectoradapter.service.consumer.transferprocess.TransferProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Profile({"consumer", "build", "test"})
@RestController("transferProcessControllerV1")
@Log4j2
@RequiredArgsConstructor
public class TransferProcessControllerImpl extends AbstractController implements TransferProcessController {

    private final TransferProcessService transferProcessService;

    @LogRequest
    @Override
    public ResponseEntity<TransferProcessId> startTransfer(TransferRequest request) {
        log.debug(
                "startTransfer(): invoking transferProcessService.startTransfer() on {} with {}",
                transferProcessService,
                request);
        TransferProcessId transferProcessIdResponse = transferProcessService.startTransfer(request);

        log.debug("startTransfer(): returning response OK with {}", transferProcessIdResponse);
        return ResponseEntity.ok(transferProcessIdResponse);
    }

    @LogRequest
    @Override
    public ResponseEntity<TransferProcess> getTransferStatus(String transferProcessId) {
        log.debug(
                "getTransferStatus(): invoking transferProcessService.getTransferProcess() on {} with transferProcessId '{}'",
                transferProcessService,
                transferProcessId);
        TransferProcess transferProcess = transferProcessService.getTransferProcess(transferProcessId);

        log.debug("startTransfer(): returning response OK with {}", transferProcess);
        return ResponseEntity.ok(transferProcess);
    }
}

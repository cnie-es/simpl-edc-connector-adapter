package eu.europa.ec.simpl.edcconnectoradapter.service.consumer.transferprocess;

import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferProcess;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferProcessId;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferRequest;

public interface TransferProcessService {

    TransferProcessId startTransfer(TransferRequest transferRequest);

    TransferProcess getTransferProcess(String transferProcessId);
}

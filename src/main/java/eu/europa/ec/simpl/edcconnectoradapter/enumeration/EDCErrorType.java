package eu.europa.ec.simpl.edcconnectoradapter.enumeration;

import eu.europa.ec.simpl.data1.common.enumeration.ErrorType;
import lombok.Getter;

public enum EDCErrorType implements ErrorType {
    REMOTE_EDC_CONNECTOR_ERROR("Remote EDC Connector Error"),

    DO_NOT_USE_THIS_TYPE(null) // just to avoid sonar blocking issue about enum with single value
;

    @Getter
    private final String problemTitle;

    EDCErrorType(String problemTitle) {
        this.problemTitle = problemTitle;
    }
}

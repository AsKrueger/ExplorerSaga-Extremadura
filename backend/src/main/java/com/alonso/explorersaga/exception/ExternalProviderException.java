package com.alonso.explorersaga.exception;

public class ExternalProviderException extends ExplorerSagaException {
    public ExternalProviderException(String message) {
        super(message);
    }

    public ExternalProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}

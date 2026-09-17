package com.alonso.explorersaga.exception;

public class ExplorerSagaException extends RuntimeException {
    public ExplorerSagaException(String message) {
        super(message);
    }

    public ExplorerSagaException(String message, Throwable cause) {
        super(message, cause);
    }
}

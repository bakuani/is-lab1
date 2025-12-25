package ru.ani.islab1.exceptions;

/**
 * Thrown when the distributed import (DB + MinIO) fails at any stage.
 */
public class ImportProcessException extends RuntimeException {

    public ImportProcessException(String message) {
        super(message);
    }

    public ImportProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
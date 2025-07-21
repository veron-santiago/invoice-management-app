package io.github.veron_santiago.backend.service.exception;

public class ResourceConflictException extends RuntimeException {
    public ResourceConflictException(String message) {
        super(message);
    }
}
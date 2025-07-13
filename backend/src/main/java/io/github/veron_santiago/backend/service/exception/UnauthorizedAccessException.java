package io.github.veron_santiago.backend.service.exception;

import java.nio.file.AccessDeniedException;

public class UnauthorizedAccessException extends AccessDeniedException {
    public UnauthorizedAccessException(String file) {
        super(file);
    }
}

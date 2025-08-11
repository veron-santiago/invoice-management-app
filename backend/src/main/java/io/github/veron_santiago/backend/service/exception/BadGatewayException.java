package io.github.veron_santiago.backend.service.exception;

public class BadGatewayException extends RuntimeException{

    public BadGatewayException(String message){
        super(message);
    }
}

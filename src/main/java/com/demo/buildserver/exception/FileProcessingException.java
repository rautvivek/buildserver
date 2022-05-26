package com.demo.buildserver.exception;

public class FileProcessingException extends RuntimeException{

    public FileProcessingException(String message,Throwable throwable) {
        super(message,throwable);
    }
    public FileProcessingException(String message) {
        super(message);
    }
}

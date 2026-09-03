package com.unimed.heic.exception;

public class HeicConversionException extends RuntimeException {

    public HeicConversionException(String message) {
        super(message);
    }

    public HeicConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}

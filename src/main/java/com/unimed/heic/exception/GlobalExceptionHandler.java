package com.unimed.heic.exception;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HeicConversionException.class)
    @ApiResponse(responseCode = "422", description = "Falha na conversao HEIC")
    public ResponseEntity<String> handleConversionError(HeicConversionException ex) {
        log.warn("Falha na conversao HEIC: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ex.getMessage());
    }

    @ExceptionHandler(FileTooLargeException.class)
    @ApiResponse(responseCode = "413", description = "Arquivo excede o limite maximo permitido")
    public ResponseEntity<String> handleFileTooLarge(FileTooLargeException ex) {
        log.warn("Upload recusado por limite de tamanho: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ApiResponse(responseCode = "413", description = "Arquivo excede o teto fisico de upload")
    public ResponseEntity<String> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("Upload recusado por exceder o teto fisico de upload: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("Arquivo excede o limite maximo permitido");
    }
}

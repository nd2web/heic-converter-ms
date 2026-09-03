package com.unimed.heic.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void fileTooLargeExceptionRetorna413ComMensagem() {
        FileTooLargeException ex = new FileTooLargeException("Arquivo excede o limite maximo de 15MB");

        ResponseEntity<String> response = handler.handleFileTooLarge(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody()).isEqualTo("Arquivo excede o limite maximo de 15MB");
    }

    @Test
    void maxUploadSizeExceededExceptionRetorna413ComMensagemLegivel() {
        MaxUploadSizeExceededException ex = new MaxUploadSizeExceededException(20 * 1024 * 1024L);

        ResponseEntity<String> response = handler.handleMaxUploadSizeExceeded(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody()).isNotBlank();
        assertThat(response.getBody()).doesNotContain("<html");
    }
}

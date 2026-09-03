package com.unimed.heic.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unimed.heic.config.HeicProperties;
import com.unimed.heic.exception.FileTooLargeException;
import com.unimed.heic.service.HeicConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

class ConvertControllerSizeValidationTest {

    private static final byte[] JPEG_BYTES = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    private HeicConversionService conversionService;
    private HeicProperties properties;
    private ConvertController controller;

    @BeforeEach
    void setUp() {
        conversionService = mock(HeicConversionService.class);
        properties = new HeicProperties();
        properties.setMaxFileSize(DataSize.ofMegabytes(15));
        controller = new ConvertController(conversionService, properties);
        when(conversionService.convert(any())).thenReturn(JPEG_BYTES);
    }

    @Test
    void arquivoAcimaDoLimiteLancaFileTooLargeExceptionSemInvocarConversao() throws Exception {
        MockMultipartFile file = arquivoComTamanho(16 * 1024 * 1024);

        assertThatThrownBy(() -> controller.convert(file)).isInstanceOf(FileTooLargeException.class);

        verify(conversionService, never()).convert(any());
    }

    @Test
    void arquivoNoLimiteExatoNaoLancaExcecao() throws Exception {
        MockMultipartFile file = arquivoComTamanho(15 * 1024 * 1024);

        ResponseEntity<byte[]> response = controller.convert(file);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(conversionService).convert(any());
    }

    @Test
    void arquivoAbaixoDoLimiteNaoLancaExcecao() throws Exception {
        MockMultipartFile file = arquivoComTamanho(10 * 1024 * 1024);

        ResponseEntity<byte[]> response = controller.convert(file);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(conversionService).convert(any());
    }

    @Test
    void limiteSubMbNaoTruncaMensagemParaZeroMb() {
        properties.setMaxFileSize(DataSize.ofKilobytes(500));
        MockMultipartFile file = arquivoComTamanho(600 * 1024);

        assertThatThrownBy(() -> controller.convert(file))
                .isInstanceOf(FileTooLargeException.class)
                .hasMessageContaining("500KB")
                .hasMessageNotContaining("0MB");
    }

    private MockMultipartFile arquivoComTamanho(int bytes) {
        return new MockMultipartFile("file", "arquivo.heic", "image/heic", new byte[bytes]);
    }
}

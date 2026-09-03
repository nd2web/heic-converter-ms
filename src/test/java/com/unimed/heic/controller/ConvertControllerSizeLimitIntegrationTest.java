package com.unimed.heic.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unimed.heic.service.HeicConversionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
class ConvertControllerSizeLimitIntegrationTest {

    private static final byte[] JPEG_BYTES = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HeicConversionService conversionService;

    @Test
    void uploadAcimaDoLimiteRetorna413SemInvocarConversao() throws Exception {
        MockMultipartFile file = arquivoComTamanho(16 * 1024 * 1024);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/convert").file(file))
                .andExpect(MockMvcResultMatchers.status().isPayloadTooLarge())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("limite")));

        verify(conversionService, never()).convert(any());
    }

    @Test
    void uploadNoLimiteExatoSeguePraConversao() throws Exception {
        when(conversionService.convert(any())).thenReturn(JPEG_BYTES);
        MockMultipartFile file = arquivoComTamanho(15 * 1024 * 1024);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/convert").file(file))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(conversionService).convert(any());
    }

    @Test
    void uploadAbaixoDoLimiteSeguePraConversao() throws Exception {
        when(conversionService.convert(any())).thenReturn(JPEG_BYTES);
        MockMultipartFile file = arquivoComTamanho(10 * 1024 * 1024);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/convert").file(file))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(conversionService).convert(any());
    }

    private MockMultipartFile arquivoComTamanho(int bytes) {
        return new MockMultipartFile("file", "arquivo.heic", "image/heic", new byte[bytes]);
    }
}

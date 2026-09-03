package com.unimed.heic.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.unimed.heic.service.HeicConversionServiceTest;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
class ConvertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void uploadValidoRetornaJpeg() throws Exception {
        Assumptions.assumeTrue(
                HeicConversionServiceTest.heifConvertDisponivel(), "heif-convert nao disponivel neste ambiente");

        byte[] heicBytes = Files.readAllBytes(Paths.get("recursos/foto-exame.heic"));
        MockMultipartFile file =
                new MockMultipartFile("file", "foto-exame.heic", "image/heic", heicBytes);

        byte[] responseBytes = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/convert").file(file))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.IMAGE_JPEG))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertThat(responseBytes[0]).isEqualTo((byte) 0xFF);
        assertThat(responseBytes[1]).isEqualTo((byte) 0xD8);
        assertThat(responseBytes[2]).isEqualTo((byte) 0xFF);
    }

    @Test
    void uploadVazioRetorna422() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "vazio.heic", "image/heic", new byte[0]);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/convert").file(file))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("vazio")));
    }

    @Test
    void uploadComAcceptApplicationJsonAindaFuncionaSemProduzirNegotiationFailure() throws Exception {
        // Regressao: a mapping do endpoint nao declara "produces", entao o Accept do cliente
        // nunca deve resultar em 406 - a resposta e sempre image/jpeg (documentado no OpenAPI
        // via @ApiResponse, nao via "produces", justamente para nao acoplar o roteamento ao
        // Accept header enviado pelo consumidor).
        Assumptions.assumeTrue(
                HeicConversionServiceTest.heifConvertDisponivel(), "heif-convert nao disponivel neste ambiente");

        byte[] heicBytes = Files.readAllBytes(Paths.get("recursos/foto-exame.heic"));
        MockMultipartFile file = new MockMultipartFile("file", "foto-exame.heic", "image/heic", heicBytes);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/convert")
                        .file(file)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.IMAGE_JPEG));
    }
}

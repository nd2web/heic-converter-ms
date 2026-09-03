package com.unimed.heic.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unimed.heic.service.HeicConversionServiceTest;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
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
class MetadataControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void uploadValidoRetornaMetadadosConsistentesComOJpegDeConvert() throws Exception {
        Assumptions.assumeTrue(
                HeicConversionServiceTest.heifConvertDisponivel(), "heif-convert nao disponivel neste ambiente");

        byte[] heicBytes = Files.readAllBytes(Paths.get("recursos/foto-exame.heic"));

        byte[] jpegBytes = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/convert")
                        .file(new MockMultipartFile("file", "foto-exame.heic", "image/heic", heicBytes)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();
        BufferedImage jpegImage = ImageIO.read(new ByteArrayInputStream(jpegBytes));

        String responseBody = mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/metadata")
                        .file(new MockMultipartFile("file", "foto-exame.heic", "image/heic", heicBytes)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = new ObjectMapper().readTree(responseBody);
        assertThat(json.path("largura").asInt()).isEqualTo(jpegImage.getWidth());
        assertThat(json.path("altura").asInt()).isEqualTo(jpegImage.getHeight());
        assertThat(json.path("tamanhoBytes").asInt()).isEqualTo(jpegBytes.length);
    }

    @Test
    void respostaNaoContemBytesDaImagem() throws Exception {
        Assumptions.assumeTrue(
                HeicConversionServiceTest.heifConvertDisponivel(), "heif-convert nao disponivel neste ambiente");

        byte[] heicBytes = Files.readAllBytes(Paths.get("recursos/foto-exame.heic"));
        MockMultipartFile file =
                new MockMultipartFile("file", "foto-exame.heic", "image/heic", heicBytes);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/metadata").file(file))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.largura").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.altura").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.tamanhoBytes").exists());
    }

    @Test
    void uploadVazioRetorna422() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "vazio.heic", "image/heic", new byte[0]);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/metadata").file(file))
                .andExpect(MockMvcResultMatchers.status().is(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("vazio")));
    }
}

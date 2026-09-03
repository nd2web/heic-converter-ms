package com.unimed.heic.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "heic.max-file-size=5MB")
class ConvertControllerCustomSizeLimitTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void limiteCustomizadoDe5MbRecusaArquivoDe6Mb() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "arquivo.heic", "image/heic", new byte[6 * 1024 * 1024]);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/convert").file(file))
                .andExpect(MockMvcResultMatchers.status().isPayloadTooLarge())
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("5MB")));
    }
}

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
@TestPropertySource(properties = "heic.convert-command=comando-inexistente-xyz")
class ConvertControllerCommandNotFoundTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void binarioAusenteFalhaDeFormaLimpaCom422() throws Exception {
        MockMultipartFile file =
                new MockMultipartFile("file", "foto-exame.heic", "image/heic", new byte[] {0x01, 0x02, 0x03});

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/convert").file(file))
                .andExpect(MockMvcResultMatchers.status().isUnprocessableEntity())
                .andExpect(
                        MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Falha ao invocar")));
    }
}

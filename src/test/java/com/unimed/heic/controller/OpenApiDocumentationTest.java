package com.unimed.heic.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenApiDocumentationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void apiDocsExpoeEndpointConvert() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        JsonNode root = new ObjectMapper().readTree(response.getBody());
        JsonNode operation = root.path("paths").path("/api/v1/convert").path("post");

        assertThat(operation.isMissingNode()).isFalse();
        assertThat(operation.path("requestBody").path("content").has("multipart/form-data")).isTrue();
        assertThat(operation.path("responses").has("200")).isTrue();
        assertThat(operation.path("responses").has("422")).isTrue();
    }

    @Test
    void metadadosDaApiVemDoPomXml() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/v3/api-docs", String.class);

        JsonNode info = new ObjectMapper().readTree(response.getBody()).path("info");

        assertThat(info.path("title").asText()).isEqualTo("heic-converter-ms");
        assertThat(info.path("description").asText()).contains("HEIC");
        assertThat(info.path("version").asText()).isNotBlank();
    }

    @Test
    void swaggerUiDisponivel() {
        ResponseEntity<String> response = restTemplate.getForEntity("/swagger-ui.html", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType()).isNotNull();
        assertThat(MediaType.TEXT_HTML.isCompatibleWith(response.getHeaders().getContentType()))
                .isTrue();
    }
}

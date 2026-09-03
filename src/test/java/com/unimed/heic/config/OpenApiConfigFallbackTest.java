package com.unimed.heic.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

// Simula rodar a Application fora do ciclo Maven completo (ex.: direto pela IDE), quando
// META-INF/build-info.properties nao existe e o bean BuildProperties nao e criado. O servico
// deve continuar subindo, com metadados de fallback, em vez de falhar o boot por causa de
// documentacao.
@SpringBootTest(properties = "spring.info.build.location=classpath:arquivo-que-nao-existe.properties")
class OpenApiConfigFallbackTest {

    @Autowired
    private OpenAPI openApi;

    @Test
    void contextoSobeComMetadadosDeFallbackQuandoBuildPropertiesNaoExiste() {
        assertThat(openApi.getInfo().getTitle()).isNotBlank();
        assertThat(openApi.getInfo().getVersion()).isNotBlank();
    }
}

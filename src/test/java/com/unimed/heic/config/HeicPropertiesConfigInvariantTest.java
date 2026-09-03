package com.unimed.heic.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.unit.DataSize;

// A spec exige que o teto fisico do Spring (spring.servlet.multipart.max-file-size) permaneca
// SEMPRE maior que o limite de negocio (heic.max-file-size), para que a validacao de negocio
// (com mensagem controlada) dispare antes do MaxUploadSizeExceededException do Spring/Tomcat.
// Este teste protege essa invariante da configuracao real em application.yml contra regressao
// silenciosa (ex.: alguem reduzir spring.servlet.multipart.max-file-size no futuro).
@SpringBootTest
class HeicPropertiesConfigInvariantTest {

    @Autowired
    private HeicProperties heicProperties;

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize springMultipartMaxFileSize;

    @Test
    void tetoFisicoDoSpringPermaneceMaiorQueLimiteDeNegocio() {
        assertThat(springMultipartMaxFileSize.toBytes())
                .isGreaterThan(heicProperties.getMaxFileSize().toBytes());
    }
}

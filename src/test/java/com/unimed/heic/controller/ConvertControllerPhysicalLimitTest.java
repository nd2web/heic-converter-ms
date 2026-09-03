package com.unimed.heic.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.unimed.heic.service.HeicConversionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

// SpringBootTest com servidor real embarcado: a validacao do teto fisico do Spring
// (spring.servlet.multipart.max-file-size) so acontece durante o parsing real do multipart
// pelo container. Com MockMvc, o MultipartHttpServletRequest ja chega pronto e o
// StandardServletMultipartResolver nunca e re-invocado, entao esse cenario nao seria exercitado.
//
// max-swallow-size elevado apenas neste teste: sem isso, o Tomcat aborta a conexao ao rejeitar
// o upload antes de terminar de receber os ~21MB do corpo, e o cliente HTTP do teste recebe um
// erro de escrita em vez do 413 (o comportamento do servidor em si ja esta correto - o handler
// e chamado normalmente; e uma limitacao do cliente de teste, nao do endpoint).
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "server.tomcat.max-swallow-size=-1")
class ConvertControllerPhysicalLimitTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private HeicConversionService conversionService;

    @Test
    void uploadAcimaDoTetoFisicoDoSpringRetorna413ComMensagemLegivelSemInvocarConversao() {
        byte[] arquivoGigante = new byte[21 * 1024 * 1024];

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(arquivoGigante) {
            @Override
            public String getFilename() {
                return "arquivo-gigante.heic";
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/convert", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYLOAD_TOO_LARGE);
        assertThat(response.getBody()).isNotBlank();
        assertThat(response.getBody()).doesNotContain("<html");
        assertThat(response.getBody()).doesNotContain("Whitelabel");
        verify(conversionService, never()).convert(any());
    }
}

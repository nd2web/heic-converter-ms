package com.unimed.heic.controller;

import com.unimed.heic.config.HeicProperties;
import com.unimed.heic.exception.FileTooLargeException;
import com.unimed.heic.service.HeicConversionService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ConvertController {

    private final HeicConversionService conversionService;
    private final HeicProperties properties;

    // Sem "produces" na mapping: isso ativaria content negotiation por Accept header e
    // devolveria 406 para clientes que mandam "Accept: application/json", quebrando o
    // comportamento atual do endpoint. O media type de resposta é documentado via
    // @ApiResponse abaixo, que so afeta o OpenAPI gerado, nao o roteamento em runtime.
    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponse(responseCode = "200", description = "JPEG convertido com sucesso",
            content = @Content(mediaType = MediaType.IMAGE_JPEG_VALUE))
    public ResponseEntity<byte[]> convert(@RequestParam("file") MultipartFile file) throws IOException {
        long maxBytes = properties.getMaxFileSize().toBytes();
        if (file.getSize() > maxBytes) {
            throw new FileTooLargeException(
                    "Arquivo excede o limite maximo de " + formatSize(maxBytes));
        }

        byte[] jpegBytes = conversionService.convert(file.getBytes());
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(jpegBytes);
    }

    // toMegabytes() trunca pra long e vira "0MB" pra limites sub-MB (ex.: 500KB) - formata na
    // maior unidade que representa o valor sem perda, em vez de assumir sempre MB.
    private String formatSize(long bytes) {
        if (bytes % (1024 * 1024) == 0) {
            return (bytes / (1024 * 1024)) + "MB";
        }
        if (bytes % 1024 == 0) {
            return (bytes / 1024) + "KB";
        }
        return bytes + "B";
    }
}

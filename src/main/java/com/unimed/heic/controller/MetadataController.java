package com.unimed.heic.controller;

import com.unimed.heic.dto.ImageMetadataResponse;
import com.unimed.heic.service.HeicConversionService;
import com.unimed.heic.service.ImageMetadataService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class MetadataController {

    private final HeicConversionService conversionService;
    private final ImageMetadataService imageMetadataService;

    @PostMapping(
            value = "/metadata",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponse(
            responseCode = "200",
            description = "Metadados do JPEG convertido (sem os bytes da imagem)",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ImageMetadataResponse.class)))
    public ResponseEntity<ImageMetadataResponse> metadata(@RequestParam("file") MultipartFile file) throws IOException {
        byte[] jpegBytes = conversionService.convert(file.getBytes());
        ImageMetadataResponse metadata = imageMetadataService.extract(jpegBytes);
        return ResponseEntity.ok(metadata);
    }
}

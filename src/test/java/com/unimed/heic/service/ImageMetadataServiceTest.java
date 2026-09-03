package com.unimed.heic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.unimed.heic.dto.ImageMetadataResponse;
import com.unimed.heic.exception.HeicConversionException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

class ImageMetadataServiceTest {

    private final ImageMetadataService service = new ImageMetadataService();

    @Test
    void jpegValidoRetornaDimensoesETamanhoCorretos() throws IOException {
        byte[] jpegBytes = criarJpeg(64, 32);

        ImageMetadataResponse metadata = service.extract(jpegBytes);

        assertThat(metadata.largura()).isEqualTo(64);
        assertThat(metadata.altura()).isEqualTo(32);
        assertThat(metadata.tamanhoBytes()).isEqualTo(jpegBytes.length);
    }

    @Test
    void bytesNaoDecodificaveisLancamHeicConversionException() {
        byte[] bytesInvalidos = {0x01, 0x02, 0x03, 0x04};

        assertThatThrownBy(() -> service.extract(bytesInvalidos))
                .isInstanceOf(HeicConversionException.class)
                .hasMessageContaining("dimensoes");
    }

    private static byte[] criarJpeg(int largura, int altura) throws IOException {
        BufferedImage image = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", out);
        return out.toByteArray();
    }
}

package com.unimed.heic.service;

import com.unimed.heic.dto.ImageMetadataResponse;
import com.unimed.heic.exception.HeicConversionException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.stereotype.Service;

@Service
public class ImageMetadataService {

    private static final String MENSAGEM_ERRO = "Nao foi possivel ler as dimensoes da imagem convertida";

    public ImageMetadataResponse extract(byte[] jpegBytes) {
        try (ImageInputStream inputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(jpegBytes))) {
            if (inputStream == null) {
                throw new HeicConversionException(MENSAGEM_ERRO);
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(inputStream);
            if (!readers.hasNext()) {
                throw new HeicConversionException(MENSAGEM_ERRO);
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(inputStream);
                int largura = reader.getWidth(0);
                int altura = reader.getHeight(0);
                return new ImageMetadataResponse(largura, altura, jpegBytes.length);
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new HeicConversionException(MENSAGEM_ERRO, e);
        }
    }
}

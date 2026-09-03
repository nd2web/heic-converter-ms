package com.unimed.heic.service;

import com.unimed.heic.config.HeicProperties;
import com.unimed.heic.exception.HeicConversionException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeicConversionService {

    private static final byte[] JPEG_MAGIC_BYTES = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    private final HeicProperties properties;

    public byte[] convert(byte[] heicBytes) {
        if (heicBytes == null || heicBytes.length == 0) {
            throw new HeicConversionException("Arquivo HEIC vazio ou ausente");
        }

        Path inputFile = null;
        Path outputFile = null;
        try {
            inputFile = Files.createTempFile("heic-in-", ".heic");
            outputFile = Files.createTempFile("heic-out-", ".jpg");
            Files.write(inputFile, heicBytes);

            Process process = startConversionProcess(inputFile, outputFile);
            waitForCompletion(process);

            byte[] jpegBytes = Files.readAllBytes(outputFile);
            validateJpeg(jpegBytes);
            return jpegBytes;
        } catch (IOException e) {
            throw new HeicConversionException("Erro de I/O durante a conversao: " + e.getMessage(), e);
        } finally {
            deleteQuietly(inputFile);
            deleteQuietly(outputFile);
        }
    }

    private Process startConversionProcess(Path inputFile, Path outputFile) {
        try {
            return new ProcessBuilder(properties.getConvertCommand(), inputFile.toString(), outputFile.toString())
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
        } catch (IOException e) {
            throw new HeicConversionException("Falha ao invocar o conversor HEIC: " + e.getMessage(), e);
        }
    }

    private void waitForCompletion(Process process) {
        boolean finished;
        try {
            finished = process.waitFor(properties.getTimeout().toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new HeicConversionException("Conversao HEIC interrompida", e);
        }

        if (!finished) {
            process.destroyForcibly();
            throw new HeicConversionException(
                    "Tempo limite excedido ao converter HEIC (" + properties.getTimeout() + ")");
        }

        if (process.exitValue() != 0) {
            throw new HeicConversionException("Conversor HEIC retornou erro (exit=" + process.exitValue() + ")");
        }
    }

    private void validateJpeg(byte[] jpegBytes) {
        if (jpegBytes.length < JPEG_MAGIC_BYTES.length
                || jpegBytes[0] != JPEG_MAGIC_BYTES[0]
                || jpegBytes[1] != JPEG_MAGIC_BYTES[1]
                || jpegBytes[2] != JPEG_MAGIC_BYTES[2]) {
            throw new HeicConversionException("Saida da conversao nao e um JPEG valido");
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.debug("Nao foi possivel remover arquivo temporario {}", path, e);
        }
    }
}

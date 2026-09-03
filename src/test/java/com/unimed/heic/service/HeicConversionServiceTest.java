package com.unimed.heic.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.unimed.heic.config.HeicProperties;
import com.unimed.heic.exception.HeicConversionException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public class HeicConversionServiceTest {

    private static final byte[] HEIC_BYTES_FAKE = {0x01, 0x02, 0x03, 0x04};

    @Test
    void entradaVaziaLancaExcecaoSemInvocarProcesso() {
        HeicProperties properties = new HeicProperties();
        properties.setConvertCommand("comando-inexistente-xyz");
        HeicConversionService service = new HeicConversionService(properties);

        assertThatThrownBy(() -> service.convert(new byte[0]))
                .isInstanceOf(HeicConversionException.class)
                .hasMessageContaining("vazio");
    }

    @Test
    void entradaNulaLancaExcecaoSemInvocarProcesso() {
        HeicProperties properties = new HeicProperties();
        properties.setConvertCommand("comando-inexistente-xyz");
        HeicConversionService service = new HeicConversionService(properties);

        assertThatThrownBy(() -> service.convert(null))
                .isInstanceOf(HeicConversionException.class)
                .hasMessageContaining("vazio");
    }

    @Test
    void comandoInexistenteFalhaDeFormaLimpa() {
        HeicProperties properties = new HeicProperties();
        properties.setConvertCommand("comando-inexistente-xyz");
        HeicConversionService service = new HeicConversionService(properties);

        assertThatThrownBy(() -> service.convert(HEIC_BYTES_FAKE))
                .isInstanceOf(HeicConversionException.class)
                .hasMessageContaining("Falha ao invocar");
    }

    @Test
    void binarioPresenteMasComExitCodeDeErroFalhaDeFormaLimpa() throws Exception {
        // Reproduz o incidente real: heif-convert instalado, mas abortando (dyld: lib
        // nativa incompativel) com exit code 134, sem produzir JPEG valido. O servico deve
        // detectar o exit code != 0 e falhar de forma limpa, e nao devolver o "jpeg" corrompido.
        Path script = extractExecutableScript("/scripts/failing-convert.sh");

        HeicProperties properties = new HeicProperties();
        properties.setConvertCommand(script.toString());
        HeicConversionService service = new HeicConversionService(properties);

        assertThatThrownBy(() -> service.convert(HEIC_BYTES_FAKE))
                .isInstanceOf(HeicConversionException.class)
                .hasMessageContaining("exit=134");
    }

    @Test
    void processoLentoRespeitaTimeoutEEncerrado() throws Exception {
        Path script = extractExecutableScript("/scripts/slow-convert.sh");

        HeicProperties properties = new HeicProperties();
        properties.setConvertCommand(script.toString());
        properties.setTimeout(Duration.ofMillis(300));
        HeicConversionService service = new HeicConversionService(properties);

        long start = System.currentTimeMillis();
        assertThatThrownBy(() -> service.convert(HEIC_BYTES_FAKE))
                .isInstanceOf(HeicConversionException.class)
                .hasMessageContaining("Tempo limite excedido");
        long elapsedMs = System.currentTimeMillis() - start;

        // O script dorme 5000ms; se o processo nao fosse realmente encerrado no timeout
        // (destroyForcibly), o teste so retornaria apos esse tempo. Um limite bem abaixo
        // disso prova que o kill aconteceu perto do timeout configurado (300ms).
        assertThat(elapsedMs).isLessThan(2000);
    }

    @Test
    void heicValidoGeraJpegValido() throws IOException {
        Assumptions.assumeTrue(heifConvertDisponivel(), "heif-convert nao disponivel neste ambiente");

        HeicProperties properties = new HeicProperties();
        HeicConversionService service = new HeicConversionService(properties);

        byte[] heicBytes = Files.readAllBytes(Paths.get("recursos/foto-exame.heic"));
        byte[] jpegBytes = service.convert(heicBytes);

        assertThat(jpegBytes).isNotEmpty();
        assertThat(jpegBytes[0]).isEqualTo((byte) 0xFF);
        assertThat(jpegBytes[1]).isEqualTo((byte) 0xD8);
        assertThat(jpegBytes[2]).isEqualTo((byte) 0xFF);
    }

    private static Path extractExecutableScript(String resourcePath) throws URISyntaxException {
        URL resource = HeicConversionServiceTest.class.getResource(resourcePath);
        Path script = Paths.get(resource.toURI());
        script.toFile().setExecutable(true);
        return script;
    }

    public static boolean heifConvertDisponivel() {
        try {
            Process process = new ProcessBuilder("heif-convert", "--version").redirectErrorStream(true).start();
            boolean finished = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            return finished && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}

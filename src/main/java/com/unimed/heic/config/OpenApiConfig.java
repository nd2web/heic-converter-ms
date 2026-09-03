package com.unimed.heic.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String FALLBACK_TITLE = "heic-converter-ms";
    private static final String FALLBACK_DESCRIPTION = "Microservico de conversao HEIC para JPEG";
    private static final String FALLBACK_VERSION = "dev";

    // BuildProperties so existe quando o jar passa pelo goal "build-info" do
    // spring-boot-maven-plugin (ex.: mvn package). Rodando a Application direto pela IDE, sem
    // esse goal, o bean nao existe — usamos ObjectProvider para nao impedir o servico de subir
    // por causa de metadados de documentacao.
    @Bean
    public OpenAPI heicConverterOpenApi(ObjectProvider<BuildProperties> buildPropertiesProvider) {
        BuildProperties buildProperties = buildPropertiesProvider.getIfAvailable();
        if (buildProperties == null) {
            return new OpenAPI().info(new Info()
                    .title(FALLBACK_TITLE)
                    .description(FALLBACK_DESCRIPTION)
                    .version(FALLBACK_VERSION));
        }
        return new OpenAPI().info(new Info()
                .title(buildProperties.getName())
                .description(buildProperties.get("description"))
                .version(buildProperties.getVersion()));
    }
}

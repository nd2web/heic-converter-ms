package com.unimed.heic.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "heic")
public class HeicProperties {

    private String convertCommand = "heif-convert";
    private Duration timeout = Duration.ofSeconds(20);
}

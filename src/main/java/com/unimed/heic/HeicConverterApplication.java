package com.unimed.heic;

import com.unimed.heic.config.HeicProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(HeicProperties.class)
public class HeicConverterApplication {

    public static void main(String[] args) {
        SpringApplication.run(HeicConverterApplication.class, args);
    }
}

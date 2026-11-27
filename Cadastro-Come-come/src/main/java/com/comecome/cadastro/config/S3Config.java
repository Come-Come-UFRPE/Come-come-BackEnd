package com.comecome.cadastro.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3Config {

    @Value("${aws.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        // Sem credenciais explícitas: o SDK usa a cadeia padrão da AWS
        // (IAM Role da instância em produção; variáveis de ambiente ou ~/.aws em dev).
        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }
}

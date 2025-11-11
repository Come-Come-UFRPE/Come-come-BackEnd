package com.comecome.openfoodfacts.service;

import com.comecome.openfoodfacts.dtos.AnamneseResponseDto;
import com.comecome.openfoodfacts.dtos.AnamnesePatchDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Service
public class GetAnamneseService {

    private static final String ANAMNESE_URL = "http://ms-anamnese:8084/api/anamnese";
    private final WebClient webClient;


    public GetAnamneseService(WebClient.Builder webClientBuilder) {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
                .codecs(configurer ->
                        configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
        this.webClient = webClientBuilder
                .baseUrl(ANAMNESE_URL)
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

    public Mono<AnamnesePatchDto> getAnamneseById(UUID userId) {
        return this.webClient.get()
                .uri("/{userID}", userId) // Forma mais segura de passar a URI var
                .retrieve()
                .bodyToMono(AnamneseResponseDto.class)
                .map(getDto -> new AnamnesePatchDto(
                        getDto.objective(),
                        getDto.foodAllergies(),
                        getDto.healthConditions(),
                        getDto.diets()
                ));
    }
}

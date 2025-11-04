package com.comecome.api_gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayConfig { //metodo para obter o ip do usuario para o rate limiter

    @Bean
    public KeyResolver ipKeyResolver(){
        return exchange -> Mono.just(
                exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
        );
    }

    // Bean do RedisRateLimiter
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(5, 10); // 5 requisições por segundo, burst 10
    }

    // Rotas do gateway
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder,
                                     KeyResolver ipKeyResolver,
                                     RedisRateLimiter redisRateLimiter) {
        return builder.routes()
                // Exemplo com limite
                .route("cadastro", r -> r.path("/cadastro/**")
                        .filters(f -> f.requestRateLimiter(c ->
                                        c.setRateLimiter(redisRateLimiter)
                                                .setKeyResolver(ipKeyResolver)
                                )
                                .stripPrefix(1))
                        .uri("lb://cadastro"))


                .route("anamnese", r -> r.path("/anamnese/**")
                        .filters(f -> f.requestRateLimiter(c ->
                                        c.setRateLimiter(redisRateLimiter)
                                                .setKeyResolver(ipKeyResolver)
                                )
                                .stripPrefix(1))
                        .uri("lb://anamnese"))


                .route("openfoodfacts", r -> r.path("/openfoodfacts/**")
                        .filters(f -> f.requestRateLimiter(c ->
                                        c.setRateLimiter(redisRateLimiter)
                                                .setKeyResolver(ipKeyResolver)
                                )
                                .stripPrefix(1))
                        .uri("lb://openfoodfacts"))

                .build();
    }

    @Configuration
    @EnableWebFluxSecurity
    public class SecurityConfig {

        @Bean
        public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
            // Desabilita TUDO de segurança
            http
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)
                    .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                    .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                    .logout(ServerHttpSecurity.LogoutSpec::disable)
                    .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll());

            return http.build();
        }
    }

}


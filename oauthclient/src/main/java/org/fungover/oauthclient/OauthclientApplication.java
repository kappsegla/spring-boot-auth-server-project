package org.fungover.oauthclient;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.reactive.result.view.CsrfRequestDataValueProcessor;
import org.springframework.security.web.server.SecurityWebFilterChain;

import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class OauthclientApplication {

    public static void main(String[] args) {
        SpringApplication.run(OauthclientApplication.class, args);
    }

    @Bean
    RouteLocator gateway(RouteLocatorBuilder rlb) {
        var apiPrefix = "/api/";
        return rlb
                .routes()
                .route(rs -> rs
                        .path(apiPrefix + "**")
                        .filters(f -> f
                                .tokenRelay()
                                .rewritePath(apiPrefix + "(?<segment>.*)", "/$\\{segment}"))
                        .uri("http://localhost:8081"))
                .route(rs -> rs
                        .path("/**")
                        .uri("http://localhost:8020"))
                .build();
    }

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange((authorize) -> authorize.anyExchange().authenticated())
                //.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults());
        return http.build();
    }
}

@RestController
class CsrfController {

    @GetMapping("/csrf")
    public Mono<CsrfToken> getCsrfToken(ServerWebExchange exchange) {
        return exchange.getAttribute(CsrfToken.class.getName());
    }
}

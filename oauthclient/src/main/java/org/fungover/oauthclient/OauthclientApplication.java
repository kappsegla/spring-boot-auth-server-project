package org.fungover.oauthclient;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Principal;

@SpringBootApplication
public class OauthclientApplication {

    public static void main(String[] args) {
        SpringApplication.run(OauthclientApplication.class, args);
    }

    @Bean
    RouteLocator gateway(RouteLocatorBuilder rlb, PreGatewayFilterFactory filter) {
        var apiPrefix = "/api/";
        return rlb
                .routes()
                .route(rs -> rs
                        .path(apiPrefix + "**")
                        .filters(f -> f
                                .filter(new PreGatewayFilterFactory().apply(new PreGatewayFilterFactory.Config()))
                                .tokenRelay()
                                .rewritePath(apiPrefix + "(?<segment>.*)", "/$\\{segment}"))
                        .uri("http://localhost:8081"))
                .route(rs -> rs
                        .path("/**")
                        .uri("http://localhost:8020"))
                .build();
    }

    //https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway/developer-guide.html
//    @Bean
//    public GlobalFilter customGlobalFilter() {
//        return (exchange, chain) -> exchange.getPrincipal()
//                .map(Principal::getName)
//                .defaultIfEmpty("Default User")
//                .map(userName -> {
//                    //adds header to proxied request
//                    exchange.getRequest().mutate().header("userId", userName).build();
//                    return exchange;
//                })
//                .flatMap(chain::filter);
//    }

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange((authorize) -> authorize
                        .pathMatchers("/.well-known/**").permitAll()
                        .anyExchange().authenticated())
                //.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .oauth2Login(Customizer.withDefaults())
                .oauth2Client(Customizer.withDefaults());
        return http.build();
    }
}

@Component
class PreGatewayFilterFactory extends AbstractGatewayFilterFactory<PreGatewayFilterFactory.Config> {

    public PreGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        // grab configuration from Config object
        return (exchange, chain) -> {
            exchange.getPrincipal()
                    .map(Principal::getName)
                    .defaultIfEmpty("Default User")
                    .map(userName -> {
                        //adds header to proxied request
                        exchange.getRequest().mutate().header("userId", userName).build();
                        return exchange;
                    });
            //If you want to build a "pre" filter you need to manipulate the
            //request before calling chain.filter
            ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
            //use builder to manipulate the request
            return chain.filter(exchange.mutate().request(builder.build()).build());
        };
    }

    public static class Config {
        //Put the configuration properties for your filter here
    }

}


@RestController
class CsrfController {

    @GetMapping("/csrf")
    public Mono<CsrfToken> getCsrfToken(ServerWebExchange exchange) {
        return exchange.getAttribute(CsrfToken.class.getName());
    }
}

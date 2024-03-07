package org.fungover.authserver;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import javax.sql.DataSource;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.springframework.security.config.Customizer.withDefaults;

@SpringBootApplication
public class AuthserverApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthserverApplication.class, args);
    }

//	@Bean
//	UserDetailsService inMemoryUserDetailsManager(){
//		var one = User.withDefaultPasswordEncoder().username("one").roles("admin","user").password("pw").build();
//		var two = User.withDefaultPasswordEncoder().username("two").roles("user").password("pw").build();
//		System.out.println(one.getPassword());
//		return new InMemoryUserDetailsManager(one,two);
//	}
}

@Configuration
class UsersConfiguration {
    @Bean
    JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    ApplicationRunner usersRunner(UserDetailsManager userDetailsManager) {
        return args -> {
            var builder = User.builder().roles("USER");
            //Password for both users are pw
            var users = Map.of(
                    "one", "{bcrypt}$2a$12$ZprhztBweQ//xfKXdaMy/uXtTLOS3QZRVc4lysmv7s5CltteD5RRO",
                    "two", "{bcrypt}$2a$12$Oe29YMhk0wMlB/OlasE8Pe5J6HM428dSrzgc94b05k73Q82fflh2m");
            users.forEach((username, password) -> {
                if (!userDetailsManager.userExists(username)) {
                    var user = builder
                            .username(username)
                            .password(password)
                            .build();
                    userDetailsManager.createUser(user);
                }
            });
        };
    }
}

@Configuration
class ClientsConfiguration {

    @Bean
    RegisteredClientRepository registeredClientRepository(JdbcTemplate template) {
        return new JdbcRegisteredClientRepository(template);
    }

    @Bean
    ApplicationRunner clientsRunner(RegisteredClientRepository repository) {
        return args -> {
            var clientId = "client";
            if (repository.findByClientId(clientId) == null) {
                repository.save(
                        RegisteredClient
                                .withId(UUID.randomUUID().toString())
                                .clientId(clientId)
                                .clientSecret("{bcrypt}$2a$12$eMLO7dL.hbIxTdyLHuvSjOdJXwWHlKSotwzhHuJ9RJxQfSdZagVSO")
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantTypes(grantTypes -> grantTypes.addAll(Set.of(
                                        AuthorizationGrantType.CLIENT_CREDENTIALS,
                                        AuthorizationGrantType.AUTHORIZATION_CODE,
                                        AuthorizationGrantType.REFRESH_TOKEN)))
                                .redirectUri("http://127.0.0.1:8082/login/oauth2/code/spring")
                                .scopes(scopes -> scopes.addAll(
                                        Set.of("user.read", "user.write", OidcScopes.OPENID)))
                                .build()
                );
            }
        };
    }
}

@Configuration
class AuthorizationConfiguration {

    @Bean
    JdbcOAuth2AuthorizationConsentService jdbcOAuth2AuthorizationConsentService(
            JdbcOperations jdbcOperations, RegisteredClientRepository repository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcOperations, repository);
    }

    @Bean
    JdbcOAuth2AuthorizationService jdbcOAuth2AuthorizationService(
            JdbcOperations jdbcOperations, RegisteredClientRepository rcr) {
        return new JdbcOAuth2AuthorizationService(jdbcOperations, rcr);
    }
}

package org.fungover.authserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@SpringBootApplication
public class AuthserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthserverApplication.class, args);
	}

	@Bean
	InMemoryUserDetailsManager inMemoryUserDetailsManager(){
		var one = User.withDefaultPasswordEncoder().username("one").roles("admin","user").password("pw").build();
		var two = User.withDefaultPasswordEncoder().username("two").roles("user").password("pw").build();

		return new InMemoryUserDetailsManager(one,two);
	}
}

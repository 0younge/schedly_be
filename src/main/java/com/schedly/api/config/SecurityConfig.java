package com.schedly.api.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.cors(Customizer.withDefaults())
			.csrf(AbstractHttpConfigurer::disable)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.GET, "/api/health").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/auth/signup", "/api/auth/login").permitAll()
				.anyRequest().authenticated())
			.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource(AppCorsProperties properties) {
		final var configuration = new CorsConfiguration();
		configuration.setAllowedOriginPatterns(properties.allowedOriginPatterns());
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		configuration.setExposedHeaders(List.of("Authorization"));
		configuration.setAllowCredentials(false);

		final var source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		return source;
	}

}

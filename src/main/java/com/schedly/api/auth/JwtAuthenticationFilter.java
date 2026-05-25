package com.schedly.api.auth;

import java.io.IOException;
import java.util.List;

import com.schedly.api.common.ApiErrorResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;
	private final ObjectMapper objectMapper;

	public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper) {
		this.jwtTokenProvider = jwtTokenProvider;
		this.objectMapper = objectMapper;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		final var authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization == null || authorization.isBlank()) {
			filterChain.doFilter(request, response);
			return;
		}

		if (!authorization.startsWith(BEARER_PREFIX)) {
			writeUnauthorized(response);
			return;
		}

		final var token = authorization.substring(BEARER_PREFIX.length()).trim();
		final var user = jwtTokenProvider.authenticate(token);
		if (user.isEmpty()) {
			writeUnauthorized(response);
			return;
		}

		final var authentication = new UsernamePasswordAuthenticationToken(user.get(), null, List.of());
		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request, response);
	}

	private void writeUnauthorized(HttpServletResponse response) throws IOException {
		SecurityContextHolder.clearContext();
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), ApiErrorResponse.of("UNAUTHORIZED", "Authentication is required"));
	}

}

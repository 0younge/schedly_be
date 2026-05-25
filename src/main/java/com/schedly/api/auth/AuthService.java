package com.schedly.api.auth;

import java.util.Locale;

import com.schedly.api.user.User;
import com.schedly.api.user.UserRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;

	public AuthService(
		UserRepository userRepository,
		PasswordEncoder passwordEncoder,
		JwtTokenProvider jwtTokenProvider
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenProvider = jwtTokenProvider;
	}

	@Transactional
	public AuthResponse signup(SignupRequest request) {
		final var email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new EmailAlreadyUsedException();
		}

		try {
			final var user = userRepository.saveAndFlush(
				User.create(email, passwordEncoder.encode(request.password()), request.name())
			);
			return issueResponse(user);
		}
		catch (DataIntegrityViolationException exception) {
			throw new EmailAlreadyUsedException();
		}
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		final var email = normalizeEmail(request.email());
		final var user = userRepository.findByEmail(email)
			.orElseThrow(InvalidCredentialsException::new);

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}

		return issueResponse(user);
	}

	private AuthResponse issueResponse(User user) {
		return AuthResponse.bearer(jwtTokenProvider.issue(user), user);
	}

	private static String normalizeEmail(String value) {
		return value.trim().toLowerCase(Locale.ROOT);
	}

}

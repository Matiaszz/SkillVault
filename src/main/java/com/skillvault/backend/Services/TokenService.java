package com.skillvault.backend.Services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.skillvault.backend.Domain.User;
import com.skillvault.backend.Repositories.UserRepository;
import com.skillvault.backend.Utils.CookieOptions;
import com.skillvault.backend.dtos.Responses.UserResponseDTO;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

@Slf4j
@Service
public class TokenService {
    @Value("${api.security.jwt.secret}")
    private String secret;

    @Autowired
    private CookieOptions cookieOptions;

    @Autowired
    private UserRepository userRepository;


    private String generateToken(String subject, String id, String role) {
        if (secret == null || secret.isBlank()) {
            throw new RuntimeException("Secret is not set! Check the environment variables.");
        }

        try {
            log.info("Generating token for subject: {}", subject);
            Algorithm algorithm = Algorithm.HMAC256(this.secret);
            return JWT.create()
                    .withSubject(subject)
                    .withClaim("id", id)
                    .withClaim("role", role)
                    .withExpiresAt(new Date(System.currentTimeMillis() + 86400000 * 2))
                    .sign(algorithm);
        } catch (Exception e) {
            log.error("Error generating token: {}", e.getMessage());
            return null;
        }
    }

    public Object getLoggedEntity() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info("Getting logged entity...");

        if (principal instanceof UserDetails userDetails) {
            String username = userDetails.getUsername();

            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null)
                return user;
        }

        log.info("User not authenticated.");
        return null;
    }

    public String generateUserToken(User user) {
        return generateToken(user.getEmail(), user.getId().toString(), user.getRole().toString());
    }

    public ResponseEntity<UserResponseDTO> generateUserTokenAndCreateCookie(User user) {
        String token = generateUserToken(user);
        ResponseCookie cookie = createCookie(token, "userToken", cookieOptions);
        log.info("token {} generated.", token);
        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString()).body(new UserResponseDTO(user));
    }

    public String validateToken(String token) {
        try {
            log.info("Validating token...");
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .build()
                    .verify(token)
                    .getSubject();

        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return null;
        }
    }

    public ResponseCookie createCookie(String token, String name, CookieOptions opts) {
        return ResponseCookie.from(name, token)
                .httpOnly(opts.httpOnly)
                .secure(opts.secure)
                .sameSite(opts.sameSite)
                .path(opts.path)
                .maxAge(opts.maxAge)
                .build();
    }


    public boolean hasToken(HttpServletRequest request) {
        if (request.getCookies() == null)
            return false;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equalsIgnoreCase("userToken")) {
                return true;
            }
        }
        return false;
    }

}
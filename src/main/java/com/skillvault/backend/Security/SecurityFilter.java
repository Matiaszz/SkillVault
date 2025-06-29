package com.skillvault.backend.Security;

import com.skillvault.backend.Repositories.UserRepository;
import com.skillvault.backend.Services.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

@Component
@Slf4j
public class SecurityFilter extends OncePerRequestFilter {

    @Autowired
    TokenService tokenService;

    @Autowired
    UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userToken = recoverUserLoginToken(request);

        try {
            String userEmail = tokenService.validateToken(userToken);
            UserDetails user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found (Security Filter)"));

            var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            log.info("Authenticated user: {}", user.getUsername());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            log.error("Authentication error: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String recoverUserLoginToken(HttpServletRequest request) {

        if (request.getCookies() != null){
            for (var cookie : request.getCookies()){
                if ("userToken".equalsIgnoreCase(cookie.getName())){
                    log.info("User Token received from cookie: {}", cookie.getName());
                    return cookie.getValue();
                }
            }
        }
        log.info("User Token in token recovery is null.");
        return null;
    }
}

package com.example.stockexchange.config;

import com.example.stockexchange.exception.AuthenticationException;
import com.example.stockexchange.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   @Lazy UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        log.info("JwtAuthenticationFilter initialized");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {


        String jwt = extractJwtFromRequest(request);

        if (jwt == null) {
            log.debug("No JWT token found, continuing filter chain");
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authentication set for user: {}", userEmail);

                } else {
                    log.debug("JWT token is not valid");
                }
            }

        } catch (UsernameNotFoundException ex) {
            log.warn("User not found: {}", ex.getMessage());
            sendErrorResponse(response, "User not found", ex.getMessage(), HttpStatus.UNAUTHORIZED);
            return;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token has expired: {}", ex.getMessage());
            sendErrorResponse(response, "Token Expired", "JWT token has expired", HttpStatus.UNAUTHORIZED);
            return;
        } catch (SignatureException ex) {
            log.warn("Invalid JWT signature: {}", ex.getMessage());
            sendErrorResponse(response, "Invalid Token", "Invalid JWT signature", HttpStatus.UNAUTHORIZED);
            return;
        } catch (JwtException ex) {
            log.warn("JWT exception: {}", ex.getMessage());
            sendErrorResponse(response, "Invalid Token", ex.getMessage(), HttpStatus.UNAUTHORIZED);
            return;
        } catch (IllegalArgumentException ex) {
            log.warn("Illegal argument in JWT: {}", ex.getMessage());
            sendErrorResponse(response, "Invalid Token", "Invalid token format", HttpStatus.UNAUTHORIZED);
            return;
        } catch (AuthenticationException ex) {
            log.warn("Authentication exception: {}", ex.getMessage());
            sendErrorResponse(response, "Authentication Error", ex.getMessage(), HttpStatus.UNAUTHORIZED);
            return;
        } catch (Exception ex) {
            log.error("Unexpected error processing JWT", ex);
            sendErrorResponse(response, "Authentication Error", ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            return;
        }


        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, String error, String message, HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Create a simple Map for JSON response
        var errorResponse = new java.util.HashMap<String, Object>();
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("status", status.value());
        errorResponse.put("timestamp", java.time.Instant.now().toString());

        new ObjectMapper().writeValue(response.getWriter(), errorResponse);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        // Check Authorization header
        final String authHeader = request.getHeader("authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.debug("JWT found in Authorization header");
            return authHeader.substring(7);
        }

        // Fallback: check cookies
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    log.debug("JWT found in cookie");
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}

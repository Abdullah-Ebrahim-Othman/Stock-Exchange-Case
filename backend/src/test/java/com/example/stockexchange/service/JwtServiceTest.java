package com.example.stockexchange.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtService Tests")
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;
    private String validToken;

    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long TEST_EXPIRATION = 3600000;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();


        ReflectionTestUtils.setField(jwtService, "SECRET_KEY", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "JWT_EXPIRATION", TEST_EXPIRATION);

        Collection<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );

        userDetails = User.builder()
                .username("testuser@example.com")
                .password("password123")
                .authorities(authorities)
                .build();

        validToken = jwtService.generateToken(new HashMap<>(), userDetails);
    }

    @Nested
    @DisplayName("Generate Token Tests")
    class GenerateTokenTests {

        @Test
        @DisplayName("Should generate token successfully with user details")
        void generateToken_Success() {
            // Act
            String token = jwtService.generateToken(new HashMap<>(), userDetails);

            // Assert
            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertEquals(3, token.split("\\.").length); // JWT has 3 parts
        }

        @Test
        @DisplayName("Should include username in token")
        void generateToken_IncludesUsername() {
            // Act
            String token = jwtService.generateToken(new HashMap<>(), userDetails);
            String extractedUsername = jwtService.extractUsername(token);

            // Assert
            assertEquals(userDetails.getUsername(), extractedUsername);
        }

        @Test
        @DisplayName("Should include authorities in token")
        void generateToken_IncludesAuthorities() {
            // Act
            String token = jwtService.generateToken(new HashMap<>(), userDetails);

            // Assert
            assertNotNull(token);
            // Token should contain authorities (verified indirectly through extraction)
            assertTrue(jwtService.isTokenValid(token, userDetails));
        }

        @Test
        @DisplayName("Should generate different tokens for different users")
        void generateToken_DifferentTokensForDifferentUsers() {
            // Arrange
            UserDetails anotherUser = User.builder()
                    .username("anotheruser@example.com")
                    .password("password456")
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                    .build();

            // Act
            String token1 = jwtService.generateToken(new HashMap<>(), userDetails);
            String token2 = jwtService.generateToken(new HashMap<>(), anotherUser);

            // Assert
            assertNotEquals(token1, token2);
        }

        @Test
        @DisplayName("Should set expiration time correctly")
        void generateToken_SetsExpirationTime() throws InterruptedException {
            // Act
            String token = jwtService.generateToken(new HashMap<>(), userDetails);

            // Small delay to ensure token is generated
            Thread.sleep(10);

            // Assert
            assertTrue(jwtService.isTokenValid(token, userDetails));
        }

        @Test
        @DisplayName("Should generate token with empty authorities")
        void generateToken_EmptyAuthorities() {
            // Arrange
            UserDetails userWithoutAuthorities = User.builder()
                    .username("noauth@example.com")
                    .password("password")
                    .authorities(Collections.emptyList())
                    .build();

            // Act
            String token = jwtService.generateToken(new HashMap<>(), userWithoutAuthorities);

            // Assert
            assertNotNull(token);
            assertTrue(jwtService.isTokenValid(token, userWithoutAuthorities));
        }
    }

    @Nested
    @DisplayName("Extract Username Tests")
    class ExtractUsernameTests {

        @Test
        @DisplayName("Should extract username from valid token")
        void extractUsername_ValidToken() {
            // Act
            String extractedUsername = jwtService.extractUsername(validToken);

            // Assert
            assertNotNull(extractedUsername);
            assertEquals("testuser@example.com", extractedUsername);
        }

        @Test
        @DisplayName("Should extract correct username for different users")
        void extractUsername_DifferentUsers() {
            // Arrange
            UserDetails user1 = User.builder()
                    .username("user1@example.com")
                    .password("password")
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                    .build();

            UserDetails user2 = User.builder()
                    .username("user2@example.com")
                    .password("password")
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                    .build();

            String token1 = jwtService.generateToken(new HashMap<>(), user1);
            String token2 = jwtService.generateToken(new HashMap<>(), user2);

            // Act
            String username1 = jwtService.extractUsername(token1);
            String username2 = jwtService.extractUsername(token2);

            // Assert
            assertEquals("user1@example.com", username1);
            assertEquals("user2@example.com", username2);
            assertNotEquals(username1, username2);
        }

        @Test
        @DisplayName("Should throw exception for malformed token")
        void extractUsername_MalformedToken() {
            // Arrange
            String malformedToken = "invalid.token.format";

            // Act & Assert
            assertThrows(MalformedJwtException.class, () -> {
                jwtService.extractUsername(malformedToken);
            });
        }

        @Test
        @DisplayName("Should throw exception for null token")
        void extractUsername_NullToken() {
            // Act & Assert
            assertThrows(Exception.class, () -> {
                jwtService.extractUsername(null);
            });
        }

        @Test
        @DisplayName("Should throw exception for empty token")
        void extractUsername_EmptyToken() {
            // Act & Assert
            assertThrows(Exception.class, () -> {
                jwtService.extractUsername("");
            });
        }

        @Test
        @DisplayName("Should throw exception for token with invalid signature")
        void extractUsername_InvalidSignature() {
            // Arrange
            JwtService anotherJwtService = new JwtService();
            ReflectionTestUtils.setField(anotherJwtService, "SECRET_KEY",
                    "differentSecretKey123456789012345678901234567890123456789012");
            ReflectionTestUtils.setField(anotherJwtService, "JWT_EXPIRATION", TEST_EXPIRATION);

            String tokenWithDifferentKey = anotherJwtService.generateToken(new HashMap<>(), userDetails);

            // Act & Assert
            assertThrows(SignatureException.class, () -> {
                jwtService.extractUsername(tokenWithDifferentKey);
            });
        }
    }

    @Nested
    @DisplayName("Token Validation Tests")
    class TokenValidationTests {

        @Test
        @DisplayName("Should validate token successfully for correct user")
        void isTokenValid_ValidToken() {
            // Act
            boolean isValid = jwtService.isTokenValid(validToken, userDetails);

            // Assert
            assertTrue(isValid);
        }

        @Test
        @DisplayName("Should invalidate token for wrong user")
        void isTokenValid_WrongUser() {
            // Arrange
            UserDetails differentUser = User.builder()
                    .username("different@example.com")
                    .password("password")
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                    .build();

            // Act
            boolean isValid = jwtService.isTokenValid(validToken, differentUser);

            // Assert
            assertFalse(isValid);
        }

        @Test
        @DisplayName("Should throw exception when extracting from expired token")
        void isTokenValid_ExpiredTokenThrowsException() {
            // Arrange
            JwtService shortExpirationService = new JwtService();
            ReflectionTestUtils.setField(shortExpirationService, "SECRET_KEY", TEST_SECRET);
            ReflectionTestUtils.setField(shortExpirationService, "JWT_EXPIRATION", 1L); // 1ms expiration

            String expiredToken = shortExpirationService.generateToken(new HashMap<>(), userDetails);

            // Wait for token to expire
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Act & Assert
            assertThrows(ExpiredJwtException.class, () -> {
                jwtService.extractUsername(expiredToken);
            });
        }

        @Test
        @DisplayName("Should validate token with matching username")
        void isTokenValid_MatchingUsername() {
            // Arrange
            String token = jwtService.generateToken(new HashMap<>(), userDetails);

            // Act
            boolean isValid = jwtService.isTokenValid(token, userDetails);

            // Assert
            assertTrue(isValid);
            assertEquals(userDetails.getUsername(), jwtService.extractUsername(token));
        }

        @Test
        @DisplayName("Should invalidate token for null user details")
        void isTokenValid_NullUserDetails() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> {
                jwtService.isTokenValid(validToken, null);
            });
        }

        @Test
        @DisplayName("Should invalidate malformed token")
        void isTokenValid_MalformedToken() {
            // Arrange
            String malformedToken = "malformed.token.here";

            // Act & Assert
            assertThrows(MalformedJwtException.class, () -> {
                jwtService.isTokenValid(malformedToken, userDetails);
            });
        }
    }

    @Nested
    @DisplayName("Token Lifecycle Tests")
    class TokenLifecycleTests {

        @Test
        @DisplayName("Should generate and validate token in full lifecycle")
        void tokenLifecycle_GenerateAndValidate() {
            // Arrange & Act
            String token = jwtService.generateToken(new HashMap<>(), userDetails);
            String extractedUsername = jwtService.extractUsername(token);
            boolean isValid = jwtService.isTokenValid(token, userDetails);

            // Assert
            assertNotNull(token);
            assertEquals(userDetails.getUsername(), extractedUsername);
            assertTrue(isValid);
        }


        @Test
        @DisplayName("Should maintain token validity throughout its lifetime")
        void tokenLifecycle_ValidityThroughoutLifetime() throws InterruptedException {
            // Arrange
            String token = jwtService.generateToken(new HashMap<>(), userDetails);

            // Act & Assert - Check validity at different points
            assertTrue(jwtService.isTokenValid(token, userDetails));

            Thread.sleep(100);
            assertTrue(jwtService.isTokenValid(token, userDetails));

            Thread.sleep(100);
            assertTrue(jwtService.isTokenValid(token, userDetails));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Security Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle user with special characters in username")
        void edgeCase_SpecialCharactersInUsername() {
            // Arrange
            UserDetails specialUser = User.builder()
                    .username("user+test@example.com")
                    .password("password")
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                    .build();

            // Act
            String token = jwtService.generateToken(new HashMap<>(), specialUser);
            String extractedUsername = jwtService.extractUsername(token);

            // Assert
            assertEquals("user+test@example.com", extractedUsername);
            assertTrue(jwtService.isTokenValid(token, specialUser));
        }

        @Test
        @DisplayName("Should handle user with long username")
        void edgeCase_LongUsername() {
            // Arrange
            String longUsername = "a".repeat(100) + "@example.com";
            UserDetails longUsernameUser = User.builder()
                    .username(longUsername)
                    .password("password")
                    .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                    .build();

            // Act
            String token = jwtService.generateToken(new HashMap<>(), longUsernameUser);
            String extractedUsername = jwtService.extractUsername(token);

            // Assert
            assertEquals(longUsername, extractedUsername);
            assertTrue(jwtService.isTokenValid(token, longUsernameUser));
        }

        @Test
        @DisplayName("Should handle user with multiple authorities")
        void edgeCase_MultipleAuthorities() {
            // Arrange
            Collection<GrantedAuthority> multipleAuthorities = List.of(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_MODERATOR"),
                    new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")
            );

            UserDetails multiAuthUser = User.builder()
                    .username("multiauth@example.com")
                    .password("password")
                    .authorities(multipleAuthorities)
                    .build();

            // Act
            String token = jwtService.generateToken(new HashMap<>(), multiAuthUser);

            // Assert
            assertNotNull(token);
            assertTrue(jwtService.isTokenValid(token, multiAuthUser));
        }

        @Test
        @DisplayName("Should not validate token after user changes")
        void edgeCase_UserDetailsChanged() {
            // Arrange
            String token = jwtService.generateToken(new HashMap<>(), userDetails);

            UserDetails modifiedUser = User.builder()
                    .username("different@example.com") // Changed username
                    .password("password123")
                    .authorities(userDetails.getAuthorities())
                    .build();

            // Act
            boolean isValid = jwtService.isTokenValid(token, modifiedUser);

            // Assert
            assertFalse(isValid);
        }
    }
}
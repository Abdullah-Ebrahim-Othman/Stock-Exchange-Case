package com.example.stockexchange.service;

import com.example.stockexchange.entity.Authority;
import com.example.stockexchange.entity.User;
import com.example.stockexchange.entity.UserCredintials;
import com.example.stockexchange.exception.InvalidCredentialException;
import com.example.stockexchange.repository.UserRepository;
import com.example.stockexchange.request.AuthenticationRequest;
import com.example.stockexchange.request.RegisterRequest;
import com.example.stockexchange.response.AuthenticationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Tests")
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;
    private User user;

    @BeforeEach
    void setUp() {

        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPassword("password123");


        authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail("john.doe@example.com");
        authenticationRequest.setPassword("password123");


        user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("encodedPassword");
        user.setAuthorities(List.of(new Authority("ROLE_USER")));
    }

    @Nested
    @DisplayName("Register Tests")
    class RegisterTests {

        @Test
        @DisplayName("Should register new user successfully")
        void register_Success() {
            // Arrange
            when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
            when(userRepository.count()).thenReturn(1L); // Not first user
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);

            // Act
            assertDoesNotThrow(() -> authenticationService.register(registerRequest));

            // Assert
            verify(userRepository).findByEmail(registerRequest.getEmail());
            verify(passwordEncoder).encode(registerRequest.getPassword());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email already taken")
        void register_EmailAlreadyTaken() {
            // Arrange
            when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.of(user));

            // Act & Assert
            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> authenticationService.register(registerRequest)
            );

            assertEquals("Email already taken", exception.getMessage());

            verify(userRepository).findByEmail(registerRequest.getEmail());
            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(anyString());
        }

        @Test
        @DisplayName("Should assign ROLE_USER to new user")
        void register_AssignsUserRole() {
            // Arrange
            when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
            when(userRepository.count()).thenReturn(1L); // Not first user
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

            // Act
            authenticationService.register(registerRequest);

            // Assert
            verify(userRepository).save(argThat(savedUser -> {
                boolean hasUserRole = savedUser.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"));
                return hasUserRole && savedUser.getAuthorities().size() == 1;
            }));
        }

        @Test
        @DisplayName("Should assign ROLE_USER and ROLE_ADMIN to first user")
        void register_FirstUserGetsAdminRole() {
            // Arrange
            when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
            when(userRepository.count()).thenReturn(0L); // First user
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

            // Act
            authenticationService.register(registerRequest);

            // Assert
            verify(userRepository).save(argThat(savedUser -> {
                boolean hasUserRole = savedUser.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"));
                boolean hasAdminRole = savedUser.getAuthorities().stream()
                        .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
                return hasUserRole && hasAdminRole && savedUser.getAuthorities().size() == 2;
            }));
        }

        @Test
        @DisplayName("Should encode password before saving")
        void register_EncodesPassword() {
            // Arrange
            when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
            when(userRepository.count()).thenReturn(1L);
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword123");

            // Act
            authenticationService.register(registerRequest);

            // Assert
            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(argThat(savedUser ->
                    savedUser.getPassword().equals("encodedPassword123")
            ));
        }

        @Test
        @DisplayName("Should save user with correct details")
        void register_SavesCorrectUserDetails() {
            // Arrange
            when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
            when(userRepository.count()).thenReturn(1L);
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

            // Act
            authenticationService.register(registerRequest);

            // Assert
            verify(userRepository).save(argThat(savedUser ->
                    savedUser.getFirstName().equals("John") &&
                            savedUser.getLastName().equals("Doe") &&
                            savedUser.getEmail().equals("john.doe@example.com") &&
                            savedUser.getPassword().equals("encodedPassword")
            ));
        }

        @Test
        @DisplayName("Should handle special characters in name")
        void register_SpecialCharactersInName() {
            // Arrange
            registerRequest.setFirstName("José");
            registerRequest.setLastName("O'Brien");

            when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
            when(userRepository.count()).thenReturn(1L);
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

            // Act
            assertDoesNotThrow(() -> authenticationService.register(registerRequest));

            // Assert
            verify(userRepository).save(argThat(savedUser ->
                    savedUser.getFirstName().equals("José") &&
                            savedUser.getLastName().equals("O'Brien")
            ));
        }

        @Test
        @DisplayName("Should handle email with plus sign")
        void register_EmailWithPlusSign() {
            // Arrange
            registerRequest.setEmail("john+test@example.com");

            when(userRepository.findByEmail("john+test@example.com")).thenReturn(Optional.empty());
            when(userRepository.count()).thenReturn(1L);
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

            // Act
            assertDoesNotThrow(() -> authenticationService.register(registerRequest));

            // Assert
            verify(userRepository).findByEmail("john+test@example.com");
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_Success() {
            // Arrange
            String expectedToken = "jwt.token.here";

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmail(authenticationRequest.getEmail()))
                    .thenReturn(Optional.of(user));
            when(jwtService.generateToken(any(), any(UserCredintials.class)))
                    .thenReturn(expectedToken);

            // Act
            AuthenticationResponse response = authenticationService.login(authenticationRequest);

            // Assert
            assertNotNull(response);
            assertEquals(expectedToken, response.getToken());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByEmail(authenticationRequest.getEmail());
            verify(jwtService).generateToken(any(), any(UserCredintials.class));
        }

        @Test
        @DisplayName("Should throw exception when email not found")
        void login_EmailNotFound() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmail(authenticationRequest.getEmail()))
                    .thenReturn(Optional.empty());

            // Act & Assert
            InvalidCredentialException exception = assertThrows(
                    InvalidCredentialException.class,
                    () -> authenticationService.login(authenticationRequest)
            );

            assertEquals("Invalid email or password", exception.getMessage());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository).findByEmail(authenticationRequest.getEmail());
            verify(jwtService, never()).generateToken(any(), any());
        }

        @Test
        @DisplayName("Should throw exception when password is incorrect")
        void login_IncorrectPassword() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            // Act & Assert
            InvalidCredentialException exception = assertThrows(
                    InvalidCredentialException.class,
                    () -> authenticationService.login(authenticationRequest)
            );

            assertEquals("Invalid email or password", exception.getMessage());

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(userRepository, never()).findByEmail(anyString());
            verify(jwtService, never()).generateToken(any(), any());
        }

        @Test
        @DisplayName("Should authenticate with correct credentials")
        void login_AuthenticatesWithCorrectCredentials() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmail(authenticationRequest.getEmail()))
                    .thenReturn(Optional.of(user));
            when(jwtService.generateToken(any(), any(UserCredintials.class)))
                    .thenReturn("token");

            // Act
            authenticationService.login(authenticationRequest);

            // Assert
            verify(authenticationManager).authenticate(argThat(auth ->
                    auth.getPrincipal().equals("john.doe@example.com") &&
                            auth.getCredentials().equals("password123")
            ));
        }

        @Test
        @DisplayName("Should generate JWT token with user credentials")
        void login_GeneratesJwtToken() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmail(authenticationRequest.getEmail()))
                    .thenReturn(Optional.of(user));
            when(jwtService.generateToken(any(), any(UserCredintials.class)))
                    .thenReturn("generated.jwt.token");

            // Act
            AuthenticationResponse response = authenticationService.login(authenticationRequest);

            // Assert
            assertEquals("generated.jwt.token", response.getToken());
            verify(jwtService).generateToken(any(), any(UserCredintials.class));
        }

        @Test
        @DisplayName("Should handle authentication manager exceptions")
        void login_HandlesAuthenticationManagerExceptions() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new RuntimeException("Authentication failed"));

            // Act & Assert
            InvalidCredentialException exception = assertThrows(
                    InvalidCredentialException.class,
                    () -> authenticationService.login(authenticationRequest)
            );

            assertEquals("Invalid email or password", exception.getMessage());
            verify(jwtService, never()).generateToken(any(), any());
        }

        @Test
        @DisplayName("Should return authentication response with token")
        void login_ReturnsAuthenticationResponse() {
            // Arrange
            String expectedToken = "sample.jwt.token";

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmail(authenticationRequest.getEmail()))
                    .thenReturn(Optional.of(user));
            when(jwtService.generateToken(any(), any(UserCredintials.class)))
                    .thenReturn(expectedToken);

            // Act
            AuthenticationResponse response = authenticationService.login(authenticationRequest);

            // Assert
            assertNotNull(response);
            assertInstanceOf(AuthenticationResponse.class, response);
            assertEquals(expectedToken, response.getToken());
        }

        @Test
        @DisplayName("Should handle null authentication result")
        void login_HandlesNullAuthenticationResult() {
            // Arrange
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmail(authenticationRequest.getEmail()))
                    .thenReturn(Optional.of(user));
            when(jwtService.generateToken(any(), any(UserCredintials.class)))
                    .thenReturn("token");

            // Act
            AuthenticationResponse response = authenticationService.login(authenticationRequest);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getToken());
        }
    }

    @Nested
    @DisplayName("Integration Scenarios Tests")
    class IntegrationScenariosTests {

        @Test
        @DisplayName("Should register and then login successfully")
        void registerThenLogin_Success() {
            // Arrange - Register
            when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
            when(userRepository.count()).thenReturn(1L);
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);

            // Act - Register
            authenticationService.register(registerRequest);

            // Arrange - Login
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmail(authenticationRequest.getEmail()))
                    .thenReturn(Optional.of(user));
            when(jwtService.generateToken(any(), any(UserCredintials.class)))
                    .thenReturn("jwt.token");

            // Act - Login
            AuthenticationResponse response = authenticationService.login(authenticationRequest);

            // Assert
            assertNotNull(response);
            assertNotNull(response.getToken());
            verify(userRepository).save(any(User.class));
            verify(jwtService).generateToken(any(), any(UserCredintials.class));
        }

        @Test
        @DisplayName("Should prevent duplicate registration")
        void preventDuplicateRegistration() {
            // Arrange
            when(userRepository.findByEmail(registerRequest.getEmail()))
                    .thenReturn(Optional.empty())
                    .thenReturn(Optional.of(user));
            when(userRepository.count()).thenReturn(1L);
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);

            // Act - First registration
            authenticationService.register(registerRequest);

            // Act & Assert - Second registration attempt
            assertThrows(RuntimeException.class, () ->
                    authenticationService.register(registerRequest)
            );

            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should handle case-sensitive email")
        void caseSensitiveEmail() {
            // Arrange
            RegisterRequest upperCaseRequest = new RegisterRequest();
            upperCaseRequest.setFirstName("John");
            upperCaseRequest.setLastName("Doe");
            upperCaseRequest.setEmail("JOHN.DOE@EXAMPLE.COM");
            upperCaseRequest.setPassword("password123");

            when(userRepository.findByEmail("JOHN.DOE@EXAMPLE.COM")).thenReturn(Optional.empty());
            when(userRepository.count()).thenReturn(1L);
            when(passwordEncoder.encode(upperCaseRequest.getPassword())).thenReturn("encodedPassword");

            // Act
            assertDoesNotThrow(() -> authenticationService.register(upperCaseRequest));

            // Assert
            verify(userRepository).save(argThat(savedUser ->
                    savedUser.getEmail().equals("JOHN.DOE@EXAMPLE.COM")
            ));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle very long password")
        void register_VeryLongPassword() {
            // Arrange
            String longPassword = "a".repeat(100);
            registerRequest.setPassword(longPassword);

            when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
            when(userRepository.count()).thenReturn(1L);
            when(passwordEncoder.encode(longPassword)).thenReturn("encodedLongPassword");

            // Act
            assertDoesNotThrow(() -> authenticationService.register(registerRequest));

            // Assert
            verify(passwordEncoder).encode(longPassword);
        }

        @Test
        @DisplayName("Should handle empty first name")
        void register_EmptyFirstName() {
            // Arrange
            registerRequest.setFirstName("");

            when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
            when(userRepository.count()).thenReturn(1L);
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

            // Act
            assertDoesNotThrow(() -> authenticationService.register(registerRequest));

            // Assert
            verify(userRepository).save(argThat(savedUser ->
                    savedUser.getFirstName().isEmpty()
            ));
        }

        @Test
        @DisplayName("Should handle user with multiple authorities")
        void login_UserWithMultipleAuthorities() {
            // Arrange
            user.setAuthorities(List.of(
                    new Authority("ROLE_USER"),
                    new Authority("ROLE_ADMIN")
            ));

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmail(authenticationRequest.getEmail()))
                    .thenReturn(Optional.of(user));
            when(jwtService.generateToken(any(), any(UserCredintials.class)))
                    .thenReturn("token");

            // Act
            AuthenticationResponse response = authenticationService.login(authenticationRequest);

            // Assert
            assertNotNull(response);
            verify(jwtService).generateToken(any(), argThat(userCreds ->
                    userCreds.getAuthorities().size() == 2
            ));
        }

        @Test
        @DisplayName("Should handle concurrent first user registration")
        void register_ConcurrentFirstUser() {
            // Arrange
            when(userRepository.findByEmail(registerRequest.getEmail())).thenReturn(Optional.empty());
            when(userRepository.count()).thenReturn(0L); // Both think they're first
            when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

            // Act
            authenticationService.register(registerRequest);

            // Assert
            verify(userRepository).save(argThat(savedUser ->
                    savedUser.getAuthorities().stream()
                            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))
            ));
        }
    }
}
package me.geetprince.Dr.appoinment.service;

import me.geetprince.Dr.appoinment.config.JwtTokenProvider;
import me.geetprince.Dr.appoinment.dto.AuthResponse;
import me.geetprince.Dr.appoinment.dto.LoginRequest;
import me.geetprince.Dr.appoinment.dto.RegisterRequest;
import me.geetprince.Dr.appoinment.dto.UserResponse;
import me.geetprince.Dr.appoinment.exception.DuplicateResourceException;
import me.geetprince.Dr.appoinment.exception.ResourceNotFoundException;
import me.geetprince.Dr.appoinment.models.User;
import me.geetprince.Dr.appoinment.models.UserRole;
import me.geetprince.Dr.appoinment.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("patient@example.com")
                .password("hashedPassword123")
                .role(UserRole.PATIENT)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void register_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .email("patient@example.com")
                .password("plainSecret123")
                .role(UserRole.PATIENT)
                .build();

        when(userRepository.existsByEmailIgnoreCase("patient@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainSecret123")).thenReturn("hashedPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(tokenProvider.generateToken(any(User.class))).thenReturn("mockJwtToken123");
        when(tokenProvider.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mockJwtToken123", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("patient@example.com", response.getEmail());
        assertEquals(UserRole.PATIENT, response.getRole());
        assertEquals(1L, response.getUserId());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsDuplicateResourceException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("patient@example.com")
                .password("plainSecret123")
                .role(UserRole.PATIENT)
                .build();

        when(userRepository.existsByEmailIgnoreCase("patient@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        LoginRequest request = LoginRequest.builder()
                .email("patient@example.com")
                .password("plainSecret123")
                .build();

        when(userRepository.findByEmailIgnoreCase("patient@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("plainSecret123", "hashedPassword123")).thenReturn(true);
        when(tokenProvider.generateToken(testUser)).thenReturn("mockJwtToken123");
        when(tokenProvider.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mockJwtToken123", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals(UserRole.PATIENT, response.getRole());
    }

    @Test
    void login_UserNotFound_ThrowsBadCredentialsException() {
        LoginRequest request = LoginRequest.builder()
                .email("unknown@example.com")
                .password("plainSecret123")
                .build();

        when(userRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_WrongPassword_ThrowsBadCredentialsException() {
        LoginRequest request = LoginRequest.builder()
                .email("patient@example.com")
                .password("wrongPassword")
                .build();

        when(userRepository.findByEmailIgnoreCase("patient@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword123")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_DisabledUser_ThrowsDisabledException() {
        testUser.setEnabled(false);
        LoginRequest request = LoginRequest.builder()
                .email("patient@example.com")
                .password("plainSecret123")
                .build();

        when(userRepository.findByEmailIgnoreCase("patient@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("plainSecret123", "hashedPassword123")).thenReturn(true);

        assertThrows(DisabledException.class, () -> authService.login(request));
    }

    @Test
    void getCurrentUser_Success() {
        when(userRepository.findByEmailIgnoreCase("patient@example.com")).thenReturn(Optional.of(testUser));

        UserResponse response = authService.getCurrentUser("patient@example.com");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("patient@example.com", response.getEmail());
        assertEquals(UserRole.PATIENT, response.getRole());
        assertTrue(response.getEnabled());
    }

    @Test
    void getCurrentUser_NotFound_ThrowsResourceNotFoundException() {
        when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getCurrentUser("missing@example.com"));
    }
}

package me.geetprince.Dr.appoinment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.geetprince.Dr.appoinment.dto.AuthResponse;
import me.geetprince.Dr.appoinment.dto.LoginRequest;
import me.geetprince.Dr.appoinment.dto.RegisterRequest;
import me.geetprince.Dr.appoinment.dto.UserResponse;
import me.geetprince.Dr.appoinment.exception.GlobalExceptionHandler;
import me.geetprince.Dr.appoinment.models.UserRole;
import me.geetprince.Dr.appoinment.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
    private AuthResponse testAuthResponse;
    private UserResponse testUserResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        testAuthResponse = AuthResponse.builder()
                .token("mockJwtToken123")
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .userId(1L)
                .email("patient@example.com")
                .role(UserRole.PATIENT)
                .build();

        testUserResponse = UserResponse.builder()
                .id(1L)
                .email("patient@example.com")
                .role(UserRole.PATIENT)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void register_Success_Returns201() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("patient@example.com")
                .password("plainSecret123")
                .role(UserRole.PATIENT)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(testAuthResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mockJwtToken123"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("patient@example.com"))
                .andExpect(jsonPath("$.role").value("PATIENT"));
    }

    @Test
    void register_InvalidEmail_Returns400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("not-an-email")
                .password("plainSecret123")
                .role(UserRole.PATIENT)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    void register_PasswordTooShort_Returns400() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("patient@example.com")
                .password("123") // Less than 6 characters
                .role(UserRole.PATIENT)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void login_Success_Returns200() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("patient@example.com")
                .password("plainSecret123")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(testAuthResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockJwtToken123"))
                .andExpect(jsonPath("$.userId").value(1L));
    }

    @Test
    void login_InvalidCredentials_Returns401() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("patient@example.com")
                .password("wrongSecret")
                .build();

        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }
}

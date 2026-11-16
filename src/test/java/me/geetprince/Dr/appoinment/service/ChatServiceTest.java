package me.geetprince.Dr.appoinment.service;

import me.geetprince.Dr.appoinment.dto.ChatRequest;
import me.geetprince.Dr.appoinment.dto.ChatResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.CallResponseSpec callResponseSpec;

    @InjectMocks
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.advisors(any(Consumer.class))).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
    }

    @Test
    void chat_WithConversationId_ReturnsResponse() {
        when(callResponseSpec.content()).thenReturn("Dr. Watson is available at 10:00 AM on Monday.");

        ChatRequest request = ChatRequest.builder()
                .message("Check Dr. Watson availability")
                .conversationId("custom-session-1")
                .build();

        ChatResponse response = chatService.chat(request);

        assertNotNull(response);
        assertEquals("Dr. Watson is available at 10:00 AM on Monday.", response.getResponse());
        assertEquals("custom-session-1", response.getConversationId());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void chat_WithoutConversationId_GeneratesNewId() {
        when(callResponseSpec.content()).thenReturn("Here is your appointment confirmation.");

        ChatRequest request = ChatRequest.builder()
                .message("Book appointment")
                .build();

        ChatResponse response = chatService.chat(request);

        assertNotNull(response);
        assertEquals("Here is your appointment confirmation.", response.getResponse());
        assertNotNull(response.getConversationId());
        assertFalse(response.getConversationId().isEmpty());
    }

    @Test
    void chat_EmptyAiResponse_ReturnsFallbackMessage() {
        when(callResponseSpec.content()).thenReturn("");

        ChatRequest request = ChatRequest.builder()
                .message("Hello")
                .conversationId("sess-2")
                .build();

        ChatResponse response = chatService.chat(request);

        assertNotNull(response);
        assertTrue(response.getResponse().contains("I apologize, but I could not generate a response"));
        assertEquals("sess-2", response.getConversationId());
    }
}

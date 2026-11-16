package me.geetprince.Dr.appoinment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.geetprince.Dr.appoinment.dto.ChatRequest;
import me.geetprince.Dr.appoinment.dto.ChatResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    public static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";

    private final ChatClient chatClient;

    public ChatResponse chat(ChatRequest request) {
        String conversationId = (request.getConversationId() != null && !request.getConversationId().trim().isEmpty())
                ? request.getConversationId().trim()
                : UUID.randomUUID().toString();

        log.info("Processing chat request for conversationId='{}'", conversationId);

        String reply = chatClient.prompt()
                .user(request.getMessage())
                .advisors(advisorSpec -> advisorSpec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, conversationId))
                .call()
                .content();

        if (reply == null || reply.trim().isEmpty()) {
            reply = "I apologize, but I could not generate a response. How else may I assist you with your hospital appointment?";
        }

        return ChatResponse.builder()
                .response(reply)
                .conversationId(conversationId)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

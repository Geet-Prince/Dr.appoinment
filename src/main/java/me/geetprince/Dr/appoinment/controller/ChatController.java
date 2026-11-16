package me.geetprince.Dr.appoinment.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.geetprince.Dr.appoinment.dto.ChatRequest;
import me.geetprince.Dr.appoinment.dto.ChatResponse;
import me.geetprince.Dr.appoinment.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = chatService.chat(request);
        return ResponseEntity.ok(response);
    }
}

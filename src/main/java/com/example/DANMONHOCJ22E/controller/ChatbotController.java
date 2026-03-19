package com.example.DANMONHOCJ22E.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.DANMONHOCJ22E.service.InternalChatbotService;

@Controller
@RequestMapping
public class ChatbotController {

    private final InternalChatbotService internalChatbotService;

    public ChatbotController(InternalChatbotService internalChatbotService) {
        this.internalChatbotService = internalChatbotService;
    }

    @GetMapping("/chatbot")
    public String chatbotPage() {
        return "chatbot";
    }

    @PostMapping("/api/chatbot/ask")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> ask(@RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(internalChatbotService.ask(payload.get("message")));
    }
}

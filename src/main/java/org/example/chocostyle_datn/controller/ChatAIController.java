package org.example.chocostyle_datn.controller;

import org.example.chocostyle_datn.model.Request.ChatMessageRequest;
import org.example.chocostyle_datn.service.ChatAIService;
import org.example.chocostyle_datn.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatAIController {

    private final ChatAIService chatAIService;
    private final ChatService chatService; // Inject thêm ChatService

    public ChatAIController(ChatAIService chatAIService, ChatService chatService) {
        this.chatAIService = chatAIService;
        this.chatService = chatService;
    }

    @PostMapping
    public Map<String, Object> chat(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        String message = (String) request.get("message");
        Integer conversationId = (Integer) request.get("conversationId");
        Integer senderId = (Integer) request.get("senderId");

        if (message == null || message.trim().isEmpty()) {
            response.put("success", false);
            response.put("reply", "Bạn chưa nhập nội dung.");
            return response;
        }

        // 1. Lưu tin nhắn của Khách hàng vào Database
        if (conversationId != null && senderId != null) {
            ChatMessageRequest userReq = new ChatMessageRequest(conversationId, senderId, message, "KHACH_HANG");
            chatService.saveIncomingMessage(userReq);
        }

        // 2. AI sinh ra câu trả lời
        String reply = chatAIService.chat(message);

        // 3. Lưu tin nhắn của AI vào Database (Dùng senderId = 0 đại diện cho AI)
        if (conversationId != null) {
            ChatMessageRequest aiReq = new ChatMessageRequest(conversationId, 0, reply, "AI");
            chatService.saveIncomingMessage(aiReq);
        }

        response.put("success", true);
        response.put("reply", reply);

        return response;
    }
}
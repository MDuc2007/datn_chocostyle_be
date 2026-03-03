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
    private final ChatService chatService; // Thêm ChatService

    public ChatAIController(ChatAIService chatAIService, ChatService chatService) {
        this.chatAIService = chatAIService;
        this.chatService = chatService;
    }

    @PostMapping
    public Map<String, Object> chat(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        String message = (String) request.get("message");
        Integer khachHangId = (Integer) request.get("khachHangId");
        Integer conversationId = (Integer) request.get("conversationId");

        // 1. Lưu tin nhắn của khách hàng vào DB trước
        if (khachHangId != null && conversationId != null) {
            ChatMessageRequest msgReq = new ChatMessageRequest();
            msgReq.setConversationId(conversationId);
            msgReq.setSenderId(khachHangId);
            msgReq.setSenderType("KHACH_HANG");
            msgReq.setContent(message);
            chatService.saveIncomingMessage(msgReq); // Gọi hàm lưu của bạn
        }

        // 2. Gọi AI xử lý
        String reply = chatAIService.chat(message);

        response.put("success", true);
        response.put("reply", reply);
        return response;
    }
}


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

        Number conversationIdNum = (Number) request.get("conversationId");
        Number senderIdNum = (Number) request.get("senderId");

        Integer conversationId = conversationIdNum != null ? conversationIdNum.intValue() : null;
        Integer senderId = senderIdNum != null ? senderIdNum.intValue() : null;

        if (message == null || message.trim().isEmpty()) {
            response.put("success", false);
            response.put("reply", "Bạn chưa nhập nội dung.");
            return response;
        }

        if (conversationId != null && senderId != null) {
            ChatMessageRequest userReq =
                    new ChatMessageRequest(conversationId, senderId, message, "KHACH_HANG");
            chatService.saveIncomingMessage(userReq);
        }

        String reply = chatAIService.chat(message, conversationId);

        if (conversationId != null) {
            ChatMessageRequest aiReq =
                    new ChatMessageRequest(conversationId, 0, reply, "AI");
            chatService.saveIncomingMessage(aiReq);
        }

        response.put("success", true);
        response.put("reply", reply);

        return response;
    }
}

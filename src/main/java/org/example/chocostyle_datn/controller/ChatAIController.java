package org.example.chocostyle_datn.controller;


import org.example.chocostyle_datn.service.ChatAIService;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatAIController {


    private final ChatAIService chatAIService;


    public ChatAIController(ChatAIService chatAIService) {
        this.chatAIService = chatAIService;
    }


    @PostMapping
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {


        Map<String, Object> response = new HashMap<>();


        String message = request.get("message");


        if (message == null || message.trim().isEmpty()) {
            response.put("success", false);
            response.put("reply", "Bạn chưa nhập nội dung.");
            return response;
        }


        String reply = chatAIService.chat(message);


        response.put("success", true);
        response.put("reply", reply);


        return response;
    }
}


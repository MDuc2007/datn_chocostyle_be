package org.example.chocostyle_datn.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.List;
import java.util.Map;


@Service
public class ChatAIService {


    @Value("${gemini.api.key}")
    private String apiKey;


    // Không để v1 hay v1beta ở đây để tránh nhầm lẫn path
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();


    public String chat(String message) {
        try {
            Map<String, Object> body = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", "Bạn là chatbot ChocoStyle.\n" + message)))
                    )
            );


            Map response = webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            // Viết đầy đủ path từ gốc v1beta
                            .path("/v1beta/models/gemini-3-flash-preview:generateContent")
                            .queryParam("key", apiKey)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();


            // Xử lý lấy text (đã rút gọn để tránh lỗi ép kiểu)
            if (response != null && response.containsKey("candidates")) {
                List candidates = (List) response.get("candidates");
                Map firstCandidate = (Map) candidates.get(0);
                Map content = (Map) firstCandidate.get("content");
                List parts = (List) content.get("parts");
                Map firstPart = (Map) parts.get(0);
                return firstPart.get("text").toString();
            }
            return "AI không trả về kết quả.";


        } catch (Exception e) {
            System.err.println("Lỗi gọi Gemini: " + e.getMessage());
            return "AI đang bận, bạn thử lại sau nhé!";
        }
    }
}




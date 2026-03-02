package org.example.chocostyle_datn.model.Request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRequest {
    private Integer conversationId;
    private Integer senderId;
    private String content;
    private String senderType; // "KHACH_HANG" hoặc "NHAN_VIEN"
}

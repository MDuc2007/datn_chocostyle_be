package org.example.chocostyle_datn.model.Response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {
    private Integer id;
    private Integer conversationId;
    private Integer senderId;
    private String senderName; // Tên của Khách hàng hoặc Nhân viên
    private String senderType;
    private String content;
    private LocalDateTime sentAt;
}

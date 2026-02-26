package org.example.chocostyle_datn.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages", indexes = {@Index(name = "idx_msg_conv_time", columnList = "conversation_id,sent_at")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "conversation_id") // Đảm bảo khớp với cột trong bảng SQL
    private Conversation conversation;

    @Column(name = "sender_id") // Ánh xạ senderId (Java) -> sender_id (SQL)
    private Integer senderId;

    @Column(name = "sender_type") // Ánh xạ senderType (Java) -> sender_type (SQL)
    private String senderType;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @PrePersist
    public void onCreate() {
        if (this.sentAt == null) {
            this.sentAt = LocalDateTime.now();
        }
    }
}
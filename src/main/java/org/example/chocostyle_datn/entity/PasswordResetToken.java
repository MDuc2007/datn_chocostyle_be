package org.example.chocostyle_datn.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, length = 6)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private ResetAccountType accountType;

    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;
}
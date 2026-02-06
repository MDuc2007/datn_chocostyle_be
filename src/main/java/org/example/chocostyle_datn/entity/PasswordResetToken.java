package org.example.chocostyle_datn.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;




import java.time.LocalDateTime;


@Entity
@Data
@NoArgsConstructor
public class PasswordResetToken {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String token;


    private LocalDateTime expiryDate;


    // Liên kết 1-1 với User: Một token chỉ thuộc về 1 người dùng
    @OneToOne(targetEntity = KhachHang.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private KhachHang user;


    // Constructor để tạo nhanh token hết hạn sau 24h (hoặc 30 phút tùy bạn)
    public PasswordResetToken(String token, KhachHang user) {
        this.token = token;
        this.user = user;
        // Token hết hạn sau 30 phút
        this.expiryDate = LocalDateTime.now().plusMinutes(30);
    }
}


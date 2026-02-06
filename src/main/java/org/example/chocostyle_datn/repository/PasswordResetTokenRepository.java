package org.example.chocostyle_datn.repository;


import org.example.chocostyle_datn.entity.KhachHang;
import org.example.chocostyle_datn.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;


@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {


    // Tìm kiếm token trong DB
    Optional<PasswordResetToken> findByToken(String token);


    // Tìm token theo người dùng (để xóa token cũ nếu họ yêu cầu nhiều lần)
    Optional<PasswordResetToken> findByUser(KhachHang user);


    // (Tùy chọn) Xóa các token đã hết hạn
    void deleteByExpiryDateBefore(java.time.LocalDateTime now);
}


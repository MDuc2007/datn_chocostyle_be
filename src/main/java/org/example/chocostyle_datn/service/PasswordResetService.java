package org.example.chocostyle_datn.service;


import org.example.chocostyle_datn.entity.KhachHang;
import org.example.chocostyle_datn.entity.PasswordResetToken;
import org.example.chocostyle_datn.repository.PasswordResetTokenRepository;
import org.example.chocostyle_datn.repository.KhachHangRepository; // Thay bằng Repository User của bạn
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Random;


@Service
public class PasswordResetService {


    @Autowired
    private KhachHangRepository userRepository;


    @Autowired
    private PasswordResetTokenRepository tokenRepository;


    @Autowired
    private EmailService emailService;


    @Autowired
    private PasswordEncoder passwordEncoder;


    public void sendOtp(String email) {
        KhachHang user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không tồn tại trong hệ thống!"));


        // Xóa token cũ nếu có
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);


        // --- TẠO MÃ OTP 6 SỐ ---
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        String otp = String.format("%06d", number); // Ví dụ: 012345
        // ------------------------


        // Lưu OTP vào DB
        PasswordResetToken myToken = new PasswordResetToken(otp, user);
        tokenRepository.save(myToken);


        // Gửi Email chứa mã
        String subject = "Mã xác thực đổi mật khẩu - ChocoStyle";
        String content = "Xin chào " + user.getTenKhachHang() + ",\n\n"
                + "Mã xác thực (OTP) của bạn là: " + otp + "\n\n"
                + "Mã này sẽ hết hạn sau 30 phút. Vui lòng không chia sẻ mã này cho ai khác.";


        emailService.sendSimpleMessage(email, subject, content);
    }


    // 2. Xác thực OTP và Đổi mật khẩu
    public void verifyOtpAndResetPassword(String email, String otp, String newPassword) {
        // Tìm user trước
        KhachHang user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email không hợp lệ"));


        // Tìm token trong DB dựa trên User (thay vì tìm theo token)
        PasswordResetToken resetToken = tokenRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Bạn chưa yêu cầu gửi mã xác thực!"));


        // Kiểm tra khớp mã OTP
        if (!resetToken.getToken().equals(otp)) {
            throw new RuntimeException("Mã xác thực không đúng!");
        }


        // Kiểm tra hết hạn
        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã xác thực đã hết hạn!");
        }


        // Đổi mật khẩu
        user.setMatKhau(passwordEncoder.encode(newPassword));
        userRepository.save(user);


        // Xóa token
        tokenRepository.delete(resetToken);
    }
}


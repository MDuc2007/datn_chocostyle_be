package org.example.chocostyle_datn.service;




import org.example.chocostyle_datn.entity.*;
import org.example.chocostyle_datn.repository.NhanVienRepository;
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
    private KhachHangRepository khachHangRepository;




    @Autowired
    private NhanVienRepository nhanVienRepository;




    @Autowired
    private PasswordResetTokenRepository tokenRepository;




    @Autowired
    private EmailService emailService;




    @Autowired
    private PasswordEncoder passwordEncoder;




    // =========================
    // 1. GỬI OTP
    // =========================
    public void sendOtp(String email, ResetAccountType type) {




        Integer accountId;
        String fullName;




        if (type == ResetAccountType.KHACH_HANG) {
            KhachHang kh = khachHangRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Email khách hàng không tồn tại"));
            accountId = kh.getId();
            fullName = kh.getTenKhachHang();
        } else {
            NhanVien nv = nhanVienRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Email nhân viên không tồn tại"));
            accountId = nv.getId();
            fullName = nv.getHoTen();
        }




        tokenRepository.findByAccountTypeAndAccountId(type, accountId)
                .ifPresent(tokenRepository::delete);




        String otp = String.format("%06d", new java.util.Random().nextInt(999999));




        PasswordResetToken token = PasswordResetToken.builder()
                .token(otp)
                .accountType(type)
                .accountId(accountId)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .build();




        tokenRepository.save(token);




        emailService.sendSimpleMessage(
                email,
                "Mã xác thực đổi mật khẩu - ChocoStyle",
                "Xin chào " + fullName + ",\n\nMã OTP của bạn là: " + otp
        );
    }










    // =========================
    // 2. VERIFY OTP + ĐỔI MK
    // =========================
    public void verifyOtpAndResetPassword(
            String email,
            String otp,
            String newPassword,
            ResetAccountType type
    ) {




        Integer accountId;




        if (type == ResetAccountType.KHACH_HANG) {
            KhachHang kh = khachHangRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Email khách hàng không hợp lệ"));
            accountId = kh.getId();




            PasswordResetToken token = tokenRepository
                    .findByAccountTypeAndAccountId(type, accountId)
                    .orElseThrow(() -> new RuntimeException("Bạn chưa yêu cầu mã OTP"));




            if (!token.getToken().equals(otp)) {
                throw new RuntimeException("Mã OTP không đúng");
            }




            if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Mã OTP đã hết hạn");
            }




            kh.setMatKhau(passwordEncoder.encode(newPassword));
            khachHangRepository.save(kh);
            tokenRepository.delete(token);




        } else {
            NhanVien nv = nhanVienRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Email nhân viên không hợp lệ"));
            accountId = nv.getId();




            PasswordResetToken token = tokenRepository
                    .findByAccountTypeAndAccountId(type, accountId)
                    .orElseThrow(() -> new RuntimeException("Bạn chưa yêu cầu mã OTP"));




            if (!token.getToken().equals(otp)) {
                throw new RuntimeException("Mã OTP không đúng");
            }




            if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Mã OTP đã hết hạn");
            }




            nv.setMatKhau(passwordEncoder.encode(newPassword));
            nhanVienRepository.save(nv);
            tokenRepository.delete(token);
        }
    }
}

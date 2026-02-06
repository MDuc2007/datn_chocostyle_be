package org.example.chocostyle_datn.controller;

import org.example.chocostyle_datn.Security.oauth2.JwtTokenProvider;
import org.example.chocostyle_datn.entity.AuthenticationProvider;
import org.example.chocostyle_datn.entity.KhachHang;
import org.example.chocostyle_datn.model.Request.LoginRequest;
import org.example.chocostyle_datn.model.Request.RegisterRequest;
import org.example.chocostyle_datn.model.Request.ResetPasswordRequest;
// import org.example.chocostyle_datn.model.Response.JwtAuthenticationResponse; // Tạm tắt để dùng Map trả về cho linh hoạt
import org.example.chocostyle_datn.repository.KhachHangRepository;
import org.example.chocostyle_datn.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    KhachHangRepository khachHangRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenProvider tokenProvider;

    @Autowired
    PasswordResetService passwordResetService;

    // ==========================================================
    // --- API 1: ĐĂNG NHẬP (LOGIN) - ĐÃ SỬA LOGIC ---
    // ==========================================================
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {

        // 1. Xác thực Username/Password (Gọi qua CustomUserDetailsService)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        // 2. Lưu thông tin vào Context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 3. Tạo Token JWT
        String jwt = tokenProvider.generateToken(authentication.getName());

        // 4. LẤY THÔNG TIN USER TỪ AUTHENTICATION (Thay vì query DB lại)
        // CustomUserDetailsService đã nạp đủ thông tin (Role, Username) vào đây rồi
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();// Vì user có thể có nhiều quyền, ta join lại thành chuỗi hoặc lấy quyền đầu tiên
        String roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.joining(","));

        // 6. Trả về JSON (Dùng Map cho nhanh, không cần tạo class Response mới)
        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", jwt);
        response.put("tokenType", "Bearer");
        response.put("username", userDetails.getUsername()); // Email hoặc Mã NV
        response.put("role", roles); // QUAN TRỌNG: Trả về role để Frontend redirect
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }

    // ==========================================================
    // --- API 2: ĐĂNG KÝ (REGISTER) - GIỮ NGUYÊN ---
    // ==========================================================
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest signUpRequest) {
        // Chỉ dành cho Khách Hàng đăng ký mới
        if (khachHangRepository.existsByTenTaiKhoan(signUpRequest.getTenTaiKhoan())) {
            return ResponseEntity.badRequest().body(createMessage("Lỗi: Tên tài khoản đã tồn tại!"));
        }

        if (khachHangRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(createMessage("Lỗi: Email đã được sử dụng!"));
        }

        KhachHang khachHang = new KhachHang();
        khachHang.setTenKhachHang(signUpRequest.getHoTen());
        khachHang.setTenTaiKhoan(signUpRequest.getTenTaiKhoan());
        khachHang.setEmail(signUpRequest.getEmail());
        khachHang.setMatKhau(passwordEncoder.encode(signUpRequest.getMatKhau()));
        khachHang.setVaiTro("USER");
        khachHang.setTrangThai(1);
        khachHang.setAuthProvider(AuthenticationProvider.LOCAL);
        khachHang.setNgayTao(LocalDate.now());
        // Tạo mã ngẫu nhiên tránh trùng
        khachHang.setMaKh("KH" + System.currentTimeMillis() % 100000);

        khachHangRepository.save(khachHang);

        return ResponseEntity.ok(createMessage("Đăng ký tài khoản thành công!"));
    }

    // ==========================================================
    // --- API 3: QUÊN MẬT KHẨU (FORGOT PASSWORD) ---
    // ==========================================================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> sendOtp(@RequestParam String email) {
        try {
            passwordResetService.sendOtp(email);
            return ResponseEntity.ok(createMessage("Mã xác thực (OTP) đã được gửi đến email của bạn!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createMessage(e.getMessage()));
        }
    }// --- API 4: ĐỔI MẬT KHẨU (RESET PASSWORD) ---
    // ==========================================================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.verifyOtpAndResetPassword(
                    request.getEmail(),
                    request.getToken(),
                    request.getNewPassword()
            );
            return ResponseEntity.ok(createMessage("Đổi mật khẩu thành công! Bạn có thể đăng nhập ngay."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(createMessage(e.getMessage()));
        }
    }

    // --- Hàm phụ trợ JSON Message ---
    private Map<String, String> createMessage(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}

// ==========================================================

        // 5. Lấy Role (Ví dụ: ROLE_ADMIN, ROLE_USER)
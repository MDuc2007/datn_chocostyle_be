package org.example.chocostyle_datn.controller;


import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.example.chocostyle_datn.Security.oauth2.JwtTokenProvider;
import org.example.chocostyle_datn.entity.*;
import org.example.chocostyle_datn.model.Request.LoginRequest;
import org.example.chocostyle_datn.model.Request.RegisterRequest;
import org.example.chocostyle_datn.model.Request.ResetPasswordRequest;
import org.example.chocostyle_datn.repository.KhachHangRepository;
import org.example.chocostyle_datn.repository.NhanVienRepository;
import org.example.chocostyle_datn.service.EmailService;
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
    NhanVienRepository nhanVienRepository;


    @Autowired
    PasswordEncoder passwordEncoder;


    @Autowired
    JwtTokenProvider tokenProvider;


    @Autowired
    PasswordResetService passwordResetService;


    @Autowired
    private EmailService emailService;


    // ==========================================================
    // LOGIN CHUNG (KHÁCH HÀNG + NHÂN VIÊN)
    // ==========================================================
    @PostMapping("/login/customer")
    public ResponseEntity<?> loginCustomer(@RequestBody LoginRequest loginRequest) {


        KhachHang kh = khachHangRepository
                .findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Email khách hàng không tồn tại"));


        if (kh.getTrangThai() == 0) {
            return ResponseEntity.badRequest().body(createMessage("Tài khoản bị khóa"));
        }


        if (!passwordEncoder.matches(loginRequest.getPassword(), kh.getMatKhau())) {
            return ResponseEntity.badRequest().body(createMessage("Sai mật khẩu"));
        }


        String role = "ROLE_KHACH_HANG";


        String jwt = tokenProvider.generateToken(
                kh.getEmail(),
                role
        );


        Map<String, Object> response = new HashMap<>();

        response.put("id", kh.getId());
        response.put("accessToken", jwt);
        response.put("tokenType", "Bearer");
        response.put("username", kh.getEmail());
        response.put("role", role);


        return ResponseEntity.ok(response);
    }




    @PostMapping("/login/staff")
    public ResponseEntity<?> loginStaff(@RequestBody LoginRequest loginRequest) {


        NhanVien nv = nhanVienRepository
                .findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Email nhân viên không tồn tại"));


        if (nv.getTrangThai() == 0) {
            return ResponseEntity.badRequest().body(createMessage("Tài khoản bị khóa"));
        }


        if (!passwordEncoder.matches(loginRequest.getPassword(), nv.getMatKhau())) {
            return ResponseEntity.badRequest().body(createMessage("Sai mật khẩu"));
        }


        String role = "ROLE_STAFF";


        if ("admin".equalsIgnoreCase(nv.getVaiTro())) {
            role = "ROLE_ADMIN";
        }


        String jwt = tokenProvider.generateToken(
                nv.getEmail(),
                role
        );


        Map<String, Object> response = new HashMap<>();

        response.put("id", nv.getId());
        response.put("tenNhanVien", nv.getHoTen());
        response.put("accessToken", jwt);
        response.put("tokenType", "Bearer");
        response.put("username", nv.getEmail());
        response.put("role", role);


        return ResponseEntity.ok(response);
    }


    // ==========================================================
    // REGISTER (CHỈ KHÁCH HÀNG)
    // ==========================================================




    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> registerUser(
            @RequestBody RegisterRequest signUpRequest
    )
    {


        // ==============================
        // 1️⃣ Validate dữ liệu đầu vào
        // ==============================


        if (signUpRequest.getEmail() == null || signUpRequest.getEmail().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(createMessage("Email không được để trống!"));
        }


        if (signUpRequest.getSoDienThoai() == null || signUpRequest.getSoDienThoai().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(createMessage("Số điện thoại không được để trống!"));
        }


        if (signUpRequest.getMatKhau() == null || signUpRequest.getMatKhau().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(createMessage("Mật khẩu không được để trống!"));
        }


        if (khachHangRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(createMessage("Email đã được sử dụng!"));
        }


        if (khachHangRepository.existsBySoDienThoai(signUpRequest.getSoDienThoai())) {
            return ResponseEntity.badRequest()
                    .body(createMessage("Số điện thoại đã được sử dụng!"));
        }


        // ==============================
        // 2️⃣ Tạo khách hàng
        // ==============================


        KhachHang khachHang = new KhachHang();
        khachHang.setTenKhachHang(signUpRequest.getHoTen());
        khachHang.setEmail(signUpRequest.getEmail());
        khachHang.setSoDienThoai(signUpRequest.getSoDienThoai());
        khachHang.setMatKhau(
                passwordEncoder.encode(signUpRequest.getMatKhau())
        );


        khachHang.setVaiTro("KHACH_HANG");
        khachHang.setTrangThai(1);
        khachHang.setAuthProvider(AuthenticationProvider.LOCAL);
        khachHang.setNgayTao(LocalDate.now());


        // 🔥 TẠO MÃ TRƯỚC
        long nextId = khachHangRepository.count() + 1;
        String maKh = String.format("KH%02d", nextId);
        khachHang.setMaKh(maKh);


// SAVE 1 LẦN DUY NHẤT
        khachHangRepository.save(khachHang);


        return ResponseEntity.ok(createMessage("Đăng ký thành công!"));
    }




    // ==========================================================
    // FORGOT PASSWORD
    // ==========================================================
    @PostMapping("/forgot-password")
    public ResponseEntity<?> sendOtp(
            @RequestParam String email,
            @RequestParam ResetAccountType type
    ) {
        try {
            passwordResetService.sendOtp(email, type);
            return ResponseEntity.ok(createMessage("OTP đã được gửi đến email!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(createMessage(e.getMessage()));
        }
    }


    // ==========================================================
    // RESET PASSWORD
    // ==========================================================
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestBody ResetPasswordRequest request
    ) {
        try {
            passwordResetService.verifyOtpAndResetPassword(
                    request.getEmail(),
                    request.getOtp(),
                    request.getNewPassword(),
                    request.getType()
            );
            return ResponseEntity.ok(createMessage("Đổi mật khẩu thành công!"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(createMessage(e.getMessage()));
        }
    }


    private Map<String, String> createMessage(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}


package org.example.chocostyle_datn.Security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.example.chocostyle_datn.entity.KhachHang;
import org.example.chocostyle_datn.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtTokenProvider tokenProvider;
    @Autowired
    private KhachHangRepository khachHangRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1. Lấy thông tin từ Google/Facebook trả về
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String avatar = oAuth2User.getAttribute("picture"); // Google lưu ảnh ở biến "picture"

        if (email == null) {
            throw new RuntimeException("Không lấy được email từ OAuth2");
        }

        // 2. 🔥 TÌM HOẶC TẠO MỚI TÀI KHOẢN (ACCOUNT LINKING)
        Optional<KhachHang> existingUser = khachHangRepository.findByEmail(email);
        KhachHang khachHang;

        if (existingUser.isPresent()) {
            // TRƯỜNG HỢP A: Đã có tài khoản bằng Email này
            khachHang = existingUser.get();

            // 👉 BƯỚC 1: KIỂM TRA TRẠNG THÁI KHÓA (Giả sử 0 là bị khóa)
            if (khachHang.getTrangThai() == 0) {
                // Đá thẳng về Frontend kèm theo tham số báo lỗi (error=locked)
                String lockedUrl = "http://localhost:5173/oauth2/redirect?error=locked";
                getRedirectStrategy().sendRedirect(request, response, lockedUrl);
                return; // Dừng luồng, KHÔNG cấp JWT Token nữa
            }

            // Tùy chọn: Nếu tài khoản cũ chưa có Avatar, lấy luôn Avatar của Google đắp vào
            if (khachHang.getAvatar() == null || khachHang.getAvatar().isEmpty()) {
                if (avatar != null) {
                    khachHang.setAvatar(avatar);
                    khachHangRepository.save(khachHang);
                }
            }
        } else {
            // TRƯỜNG HỢP B: Chưa có tài khoản -> Tự động đăng ký mới luôn
            khachHang = new KhachHang();
            khachHang.setEmail(email);
            khachHang.setTenKhachHang(name != null ? name : "Khách hàng");
            khachHang.setAvatar(avatar);

            // Vì đăng nhập Google, không cần mật khẩu
            khachHang.setMatKhau("");
            khachHang.setTrangThai(1); // Mặc định kích hoạt tài khoản

            // Lưu xuống DB
            khachHang = khachHangRepository.save(khachHang);
        }

        String role = "ROLE_KHACH_HANG";

        // 3. 🔥 TẠO JWT (Chỉ chạy đến đây nếu tài khoản KHÔNG BỊ KHÓA)
        String token = tokenProvider.generateToken(email, role);

        // 4. 🔥 TRẢ ĐỦ THÔNG TIN VỀ FRONTEND NẾU THÀNH CÔNG
        String targetUrl = UriComponentsBuilder
                .fromUriString("http://localhost:5173/oauth2/redirect")
                .queryParam("token", token)
                .queryParam("role", role)
                .queryParam("id", khachHang.getId())
                .queryParam("tenKhachHang", khachHang.getTenKhachHang())
                .queryParam("avatar", khachHang.getAvatar() != null ? khachHang.getAvatar() : "")
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
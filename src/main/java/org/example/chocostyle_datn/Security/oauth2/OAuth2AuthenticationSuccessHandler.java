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
        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            throw new RuntimeException("Không lấy được email từ OAuth2");
        }

        // 🔥 LẤY USER TỪ DB
        KhachHang khachHang = khachHangRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        String role = "ROLE_KHACH_HANG";

        // 🔥 TẠO JWT
        String token = tokenProvider.generateToken(email, role);

        // 🔥 TRẢ ĐỦ THÔNG TIN VỀ FRONTEND
        String targetUrl = UriComponentsBuilder
                .fromUriString("http://localhost:5173/oauth2/redirect")
                .queryParam("token", token)
                .queryParam("role", role)
                .queryParam("id", khachHang.getId())
                .queryParam("tenKhachHang", khachHang.getTenKhachHang())
                .queryParam("avatar", khachHang.getAvatar())
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}


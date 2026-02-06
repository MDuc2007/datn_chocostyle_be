package org.example.chocostyle_datn.Security.oauth2;


import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


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


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 1. Lấy thông tin user đã được Google xác thực
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");


        // 2. Tạo Token JWT từ email này
        // (Lưu ý: Hàm generateToken của bạn cần nhận String username/email)
        String token = tokenProvider.generateToken(email);


        // 3. Tạo đường dẫn để chuyển hướng về Frontend (VueJS)
        // Chúng ta sẽ gửi kèm Token trên thanh địa chỉ URL
        // Cổng 5173 là cổng mặc định của Vite (VueJS) mà bạn đang chạy
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth2/redirect")
                .queryParam("token", token)
                .build().toUriString();


        // 4. Thực hiện chuyển hướng (Đá người dùng về trang VueJS kèm theo Token)
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}


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
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {


        // 1️⃣ Lấy email từ Google
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");


        // 2️⃣ Vì chỉ khách hàng được login OAuth -> gán cứng role
        String role = "ROLE_KHACH_HANG";


        // 3️⃣ Tạo JWT
        String token = tokenProvider.generateToken(email, role);


        // 4️⃣ Redirect về Frontend kèm token + role
        String targetUrl = UriComponentsBuilder
                .fromUriString("http://localhost:5173/oauth2/redirect")
                .queryParam("token", token)
                .queryParam("role", role)
                .build()
                .toUriString();


        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}


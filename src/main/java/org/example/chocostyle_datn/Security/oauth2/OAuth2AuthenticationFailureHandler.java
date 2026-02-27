package org.example.chocostyle_datn.Security.oauth2;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // Đây là đường link trang chuyển hướng của Vue (Frontend)
        String targetUrl = "http://localhost:5173/oauth2/redirect";
        String errorType = "unknown";

        // Bắt chính xác loại lỗi do tài khoản bị khóa hoặc vô hiệu hóa
        if (exception instanceof LockedException) {
            errorType = "locked";    // Bị khóa
        } else if (exception instanceof DisabledException) {
            errorType = "disabled";  // Chưa kích hoạt
        } else {
            errorType = "unauthorized"; // Lỗi khác
        }

        // Ghép thêm biến error vào URL (VD: http://localhost:5173/oauth2/redirect?error=locked)
        targetUrl = targetUrl + "?error=" + errorType;

        // Chuyển hướng người dùng về lại Frontend
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
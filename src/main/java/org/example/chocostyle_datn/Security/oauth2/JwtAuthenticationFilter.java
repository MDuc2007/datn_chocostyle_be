package org.example.chocostyle_datn.Security.oauth2;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.chocostyle_datn.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;


@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {


    @Autowired
    private JwtTokenProvider tokenProvider;


    @Autowired
    private CustomUserDetailsService customUserDetailsService;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. Lấy JWT từ request (trong Header)
            String jwt = getJwtFromRequest(request);


            // 2. Kiểm tra xem Token có hợp lệ không
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {


                // 3. Lấy email/username từ chuỗi JWT
                String email = tokenProvider.getEmailFromJWT(jwt);


                // 4. Tải thông tin người dùng từ Database lên
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);


                // 5. Nếu người dùng hợp lệ, set thông tin cho Security Context
                if (userDetails != null) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());


                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));


                    // Lưu thông tin người dùng vào Context để dùng cho các bước sau
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            log.error("Không thể xác thực người dùng", ex);
        }


        // 6. Cho phép request đi tiếp
        filterChain.doFilter(request, response);
    }


    // Hàm phụ để lấy Token từ Header "Authorization"
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Kiểm tra xem header có bắt đầu bằng "Bearer " không
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Cắt bỏ chữ "Bearer " để lấy token
        }
        return null;
    }
}


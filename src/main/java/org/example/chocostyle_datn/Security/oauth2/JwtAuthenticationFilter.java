package org.example.chocostyle_datn.Security.oauth2;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.chocostyle_datn.service.KhachHangUserDetailsService;
import org.example.chocostyle_datn.service.NhanVienUserDetailsService;
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
    private KhachHangUserDetailsService khachHangUserDetailsService;

    @Autowired
    private NhanVienUserDetailsService nhanVienUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        // Bỏ qua filter cho các API công khai dạng tài nguyên
        if (requestURI.startsWith("/uploads/")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // 🔥 CHỈ PARSE TOKEN 1 LẦN DUY NHẤT
                Claims claims = tokenProvider.getClaimsFromJWT(jwt);
                String username = claims.getSubject();
                String role = claims.get("role", String.class);

                UserDetails userDetails = null;

                if ("ROLE_KHACH_HANG".equals(role)) {
                    userDetails = khachHangUserDetailsService.loadUserByUsername(username);
                } else if ("ROLE_STAFF".equals(role) || "ROLE_ADMIN".equals(role)) {
                    userDetails = nhanVienUserDetailsService.loadUserByUsername(username);
                }

                if (userDetails != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (ExpiredJwtException ex) {
            // 🔥 TRẢ LỖI 401 TRỰC TIẾP CHO FRONTEND KHI TOKEN HẾT HẠN
            log.warn("Token đã hết hạn cho request: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"TokenExpired\", \"message\": \"Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại!\"}");
            return; // Ngắt luồng, không đi sâu vào controller nữa

        } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
            log.error("Token không hợp lệ", ex);
        } catch (Exception ex) {
            log.error("Lỗi xác thực hệ thống", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
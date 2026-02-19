package org.example.chocostyle_datn.Security.oauth2;


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
        if (requestURI.startsWith("/uploads/")) {
            filterChain.doFilter(request, response);
            return;
        }


        try {


            String jwt = getJwtFromRequest(request);


            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {


                // 🔥 LẤY USERNAME (KHÔNG DÙNG EMAIL NỮA)
                String username = tokenProvider.getUsernameFromJWT(jwt);
                String role = tokenProvider.getRoleFromJWT(jwt);


                UserDetails userDetails = null;


                // 🔥 LOAD ĐÚNG SERVICE THEO ROLE
                if ("ROLE_KHACH_HANG".equals(role)) {
                    userDetails = khachHangUserDetailsService.loadUserByUsername(username);
                }
                else if ("ROLE_STAFF".equals(role) || "ROLE_ADMIN".equals(role)) {
                    userDetails = nhanVienUserDetailsService.loadUserByUsername(username);
                }




                if (userDetails != null &&
                        SecurityContextHolder.getContext().getAuthentication() == null) {


                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );


                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );


                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }


        } catch (Exception ex) {
            log.error("Không thể xác thực người dùng", ex);
        }


        filterChain.doFilter(request, response);
    }


    private String getJwtFromRequest(HttpServletRequest request) {


        String bearerToken = request.getHeader("Authorization");


        if (StringUtils.hasText(bearerToken) &&
                bearerToken.startsWith("Bearer ")) {


            return bearerToken.substring(7);
        }


        return null;
    }
}


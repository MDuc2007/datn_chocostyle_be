package org.example.chocostyle_datn.Security.oauth2;

import org.example.chocostyle_datn.service.KhachHangUserDetailsService;
import org.example.chocostyle_datn.service.NhanVienUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private KhachHangUserDetailsService khachHangUserDetailsService;

    @Autowired
    private NhanVienUserDetailsService nhanVienUserDetailsService;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    // 👉 THÊM: Tiêm class xử lý lỗi OAuth2 (Báo tài khoản bị khóa)
    @Autowired
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    // =====================================================
    // PASSWORD ENCODER
    // =====================================================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // =====================================================
    // AUTH PROVIDER KHÁCH HÀNG
    // =====================================================
    @Bean
    public DaoAuthenticationProvider khachHangAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(khachHangUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // =====================================================
    // AUTH PROVIDER NHÂN VIÊN
    // =====================================================
    @Bean
    public DaoAuthenticationProvider nhanVienAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(nhanVienUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // =====================================================
    // AUTHENTICATION MANAGER
    // =====================================================
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // =====================================================
    // SECURITY FILTER CHAIN
    // =====================================================
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ============================
                        // KHÁCH HÀNG
                        // ============================
                        .requestMatchers(
                                "/api/auth/login/customer",
                                "/api/auth/register",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password"
                        ).permitAll()

                        // ============================
                        // NHÂN VIÊN
                        // ============================
                        .requestMatchers(
                                "/api/auth/login/staff",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password"
                        ).permitAll()

                        // ============================
                        // 👉 MỞ KHÓA CÁC API PUBLIC (AI CŨNG XEM ĐƯỢC)
                        // ============================
                        .requestMatchers(
                                "/api/san-pham/**",        // Cho phép xem sản phẩm, best-seller, home
                                "/api/don-hang/tra-cuu**", // Cho phép khách lạ tra cứu đơn hàng
                                "/images/**",              // Cho phép tải ảnh avatar/sản phẩm lên giao diện
                                "/oauth2/**",              // Cho phép chạy luồng đăng nhập Google
                                "/ws-chocostyle/**",       // WebSockets cho ứng dụng
                                "/api/conversations/**"    // Chat/Conversations
                        ).permitAll()

                        // Các API còn lại (thêm giỏ hàng, thanh toán, quản lý...) bắt buộc phải đăng nhập
                        .anyRequest().authenticated()
                )

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )

                // ============================
                // 👉 XỬ LÝ OAUTH2 THẤT BẠI
                // ============================
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler) // Ném lỗi về Vue
                )

                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Đăng ký 2 provider
                .authenticationProvider(khachHangAuthenticationProvider())
                .authenticationProvider(nhanVienAuthenticationProvider())

                // Thêm JWT filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // =====================================================
    // CORS CONFIG
    // =====================================================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
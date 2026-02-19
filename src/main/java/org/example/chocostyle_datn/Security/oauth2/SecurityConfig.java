package org.example.chocostyle_datn.Security.oauth2;


import org.example.chocostyle_datn.service.KhachHangUserDetailsService;
import org.example.chocostyle_datn.service.NhanVienUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
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
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;


import java.util.List;


@Configuration
@EnableWebSecurity
public class SecurityConfig {


    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final KhachHangUserDetailsService khachHangUserDetailsService;
    private final NhanVienUserDetailsService nhanVienUserDetailsService;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;


    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            KhachHangUserDetailsService khachHangUserDetailsService,
            NhanVienUserDetailsService nhanVienUserDetailsService,
            OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.khachHangUserDetailsService = khachHangUserDetailsService;
        this.nhanVienUserDetailsService = nhanVienUserDetailsService;
        this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
    }


    // =====================================================
    // PASSWORD ENCODER
    // =====================================================
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public DaoAuthenticationProvider nhanVienAuthenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(nhanVienUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }


    @Bean
    public DaoAuthenticationProvider khachHangAuthenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(khachHangUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }








    // =====================================================
    // AUTHENTICATION MANAGER
    // =====================================================
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
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


                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )


                .exceptionHandling(e -> e
                        .authenticationEntryPoint(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                )


                .authorizeHttpRequests(auth -> auth


                        // Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()


                        // Public files
                        .requestMatchers("/uploads/**").permitAll()


                        // ============================
                        // PUBLIC AUTH API
                        // ============================
                        .requestMatchers(
                                "/api/auth/login/customer",
                                "/api/auth/login/staff",
                                "/api/auth/register",
                                "/api/auth/forgot-password",
                                "/api/auth/reset-password",
                                "/oauth2/**",
                                "/error"
                        ).permitAll()


                        // ============================
                        // ROLE BASED ACCESS
                        // ============================
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/staff/**").hasRole("NHAN_VIEN")
                        .requestMatchers("/api/customer/**").hasRole("KHACH_HANG")


                        // Other requests
                        .anyRequest().authenticated()
                )


                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)


                .oauth2Login(oauth2 ->
                        oauth2.successHandler(oAuth2AuthenticationSuccessHandler)
                )


                // Register providers
                .authenticationProvider(khachHangAuthenticationProvider())
                .authenticationProvider(nhanVienAuthenticationProvider())


                // JWT filter
                .addFilterBefore(jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }


    // =====================================================
    // CORS CONFIG
    // =====================================================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {


        CorsConfiguration configuration = new CorsConfiguration();


        configuration.setAllowedOriginPatterns(
                List.of(
                        "http://localhost:3000",
                        "http://localhost:5173"
                )
        );


        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);


        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();


        source.registerCorsConfiguration("/**", configuration);


        return source;
    }
}


package org.example.chocostyle_datn.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods(
                        "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
                )
                .allowedHeaders("*");
    }


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ánh xạ đường dẫn /uploads/** vào thư mục uploads ngoài thực tế
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}


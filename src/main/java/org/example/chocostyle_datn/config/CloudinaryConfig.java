package org.example.chocostyle_datn.config;

import com.cloudinary.Cloudinary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(Map.of(
                "cloud_name", "dvsqk1vel",
                "api_key", "773512841598354",
                "api_secret", "aVm1JLvwuVwckjnk97lcecjZlzc"
        ));
    }
}


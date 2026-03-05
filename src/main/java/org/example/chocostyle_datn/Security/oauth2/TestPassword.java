package org.example.chocostyle_datn.Security.oauth2;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPassword {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String hash = encoder.encode("RTRNoZ");

        System.out.println(hash);
    }
}

package org.example.chocostyle_datn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync// Thêm dòng này để bật chế độ chạy ngầm
@EnableScheduling
public class ChocoStyleDatnApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChocoStyleDatnApplication.class, args);
    }

}

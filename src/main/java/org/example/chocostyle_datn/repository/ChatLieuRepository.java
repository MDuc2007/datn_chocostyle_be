package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.ChatLieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatLieuRepository extends JpaRepository<ChatLieu, Integer> {
    @Query("select max(c.maChatLieu) from ChatLieu c")
    String findMaxMa();
}

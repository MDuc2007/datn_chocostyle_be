package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.ChatLieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatLieuRepository extends JpaRepository<ChatLieu, Integer> {
    @Query("select max(c.maChatLieu) from ChatLieu c")
    String findMaxMa();

    @Query("""
            SELECT COUNT(cl) > 0
            FROM ChatLieu cl
            WHERE LOWER(REPLACE(cl.tenChatLieu, ' ', ''))
                  = LOWER(REPLACE(:ten, ' ', ''))
            """)
    boolean existsByTenIgnoreSpace(@Param("ten") String ten);


}

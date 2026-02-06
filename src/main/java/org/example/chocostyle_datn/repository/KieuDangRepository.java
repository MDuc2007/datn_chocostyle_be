package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.KieuDang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KieuDangRepository extends JpaRepository<KieuDang,Integer> {
    @Query("select max(k.maKieuDang) from KieuDang k")
    String findMaxMa();
    @Query("""
            SELECT COUNT(kd) > 0
            FROM KieuDang kd
            WHERE LOWER(REPLACE(kd.tenKieuDang, ' ', ''))
                  = LOWER(REPLACE(:ten, ' ', ''))
            """)
    boolean existsByTenIgnoreSpace(@Param("ten") String ten);
}

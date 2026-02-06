package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.PhongCachMac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhongCachMacRepository extends JpaRepository<PhongCachMac,Integer> {
    @Query("select max(p.maPhongCachMac) from PhongCachMac p")
    String findMaxMa();
    @Query("""
            SELECT COUNT(pcm) > 0
            FROM PhongCachMac pcm
            WHERE LOWER(REPLACE(pcm.tenPhongCach, ' ', ''))
                  = LOWER(REPLACE(:ten, ' ', ''))
            """)
    boolean existsByTenIgnoreSpace(@Param("ten") String ten);
}

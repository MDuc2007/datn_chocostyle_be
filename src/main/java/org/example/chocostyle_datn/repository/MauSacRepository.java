package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MauSacRepository extends JpaRepository<MauSac,Integer> {
    @Query("select max(m.maMauSac) from MauSac m")
    String findMaxMa();
    @Query("""
            SELECT COUNT(ms) > 0
            FROM MauSac ms
            WHERE LOWER(REPLACE(ms.tenMauSac, ' ', ''))
                  = LOWER(REPLACE(:ten, ' ', ''))
            """)
    boolean existsByTenIgnoreSpace(@Param("ten") String ten);
}

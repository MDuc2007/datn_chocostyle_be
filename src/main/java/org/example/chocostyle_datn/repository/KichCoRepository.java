package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.KichCo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface KichCoRepository extends JpaRepository<KichCo,Integer> {
    @Query("select max(k.maKichCo) from KichCo k")
    String findMaxMa();
    @Query("""
            SELECT COUNT(ck) > 0
            FROM KichCo ck
            WHERE LOWER(REPLACE(ck.tenKichCo, ' ', ''))
                  = LOWER(REPLACE(:ten, ' ', ''))
            """)
    boolean existsByTenIgnoreSpace(@Param("ten") String ten);
}

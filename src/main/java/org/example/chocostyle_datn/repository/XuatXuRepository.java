package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.XuatXu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface XuatXuRepository extends JpaRepository<XuatXu,Integer> {
    List<XuatXu> findAllByOrderByIdDesc();
    @Query("select max(x.maXuatXu) from XuatXu x")
    String findMaxMa();
    @Query("""
            SELECT COUNT(xx) > 0
            FROM XuatXu xx
            WHERE LOWER(REPLACE(xx.tenXuatXu, ' ', ''))
                  = LOWER(REPLACE(:ten, ' ', ''))
            """)
    boolean existsByTenIgnoreSpace(@Param("ten") String ten);
}

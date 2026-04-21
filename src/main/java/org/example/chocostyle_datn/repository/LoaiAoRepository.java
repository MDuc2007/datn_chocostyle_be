package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.LoaiAo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LoaiAoRepository extends JpaRepository<LoaiAo,Integer> {
    List<LoaiAo> findAllByOrderByIdDesc();
    @Query("select max(l.maLoai) from LoaiAo l")
    String findMaxMa();
    @Query("""
            SELECT COUNT(la) > 0
            FROM LoaiAo la
            WHERE LOWER(REPLACE(la.tenLoai, ' ', ''))
                  = LOWER(REPLACE(:ten, ' ', ''))
            """)
    boolean existsByTenIgnoreSpace(@Param("ten") String ten);
}

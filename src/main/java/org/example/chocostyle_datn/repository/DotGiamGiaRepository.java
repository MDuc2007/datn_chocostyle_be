package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.DotGiamGia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface DotGiamGiaRepository extends JpaRepository<DotGiamGia,Integer> {
    @Query("""
        SELECT d FROM DotGiamGia d
        WHERE (:keyword IS NULL
              OR LOWER(d.maDotGiamGia) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(d.tenDotGiamGia) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:trangThai IS NULL OR d.trangThai = :trangThai)
          AND (:start IS NULL OR d.ngayBatDau >= :start)
          AND (:end IS NULL OR d.ngayKetThuc <= :end)
        ORDER BY d.id DESC
    """)
    Page<DotGiamGia> filter(
            String keyword,
            Integer trangThai,
            LocalDate start,
            LocalDate end,
            Pageable pageable
    );

    DotGiamGia findTopByOrderByIdDesc();
    boolean existsByTenDotGiamGiaIgnoreCase(String tenDotGiamGia);


    @Query("""
        SELECT COUNT(d) > 0
        FROM DotGiamGia d
        WHERE LOWER(d.tenDotGiamGia) = LOWER(:ten)
          AND d.id <> :id
    """)
    boolean existsTenIgnoreCaseForUpdate(String ten, Integer id);
}

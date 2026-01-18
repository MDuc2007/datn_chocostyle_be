package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    @Query("SELECT h FROM HoaDon h WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR h.maHoaDon LIKE %:keyword% OR h.tenKhachHang LIKE %:keyword% OR h.soDienThoai LIKE %:keyword%) " +
            "AND (:loaiDon IS NULL OR h.loaiDon = :loaiDon) " +
            "AND (:trangThai IS NULL OR h.trangThai = :trangThai) " +
            "AND (:startDate IS NULL OR h.ngayTao >= :startDate) " +
            "AND (:endDate IS NULL OR h.ngayTao <= :endDate) " +
            "ORDER BY h.ngayTao DESC")
    Page<HoaDon> findAllByFilter(@Param("keyword") String keyword,
                                 @Param("loaiDon") Integer loaiDon,
                                 @Param("trangThai") Integer trangThai,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,
                                 Pageable pageable);
    HoaDon findTopByOrderByIdDesc();
}
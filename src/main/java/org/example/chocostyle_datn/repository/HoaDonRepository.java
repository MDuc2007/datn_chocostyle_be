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
    @Query("""
               SELECT MAX(h.ngayThanhToan)
               FROM HoaDon h
               WHERE h.idKhachHang.id = :khachHangId
                 AND h.trangThai = 1
            """)
    LocalDate findLanMuaGanNhat(Integer khachHangId);


    @Query("SELECT h FROM HoaDon h WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR h.maHoaDon LIKE %:keyword% OR h.tenKhachHang LIKE %:keyword% OR h.soDienThoai LIKE %:keyword%) " +
            "AND (:loaiDon IS NULL OR h.loaiDon = :loaiDon) " +
            "AND (:trangThai IS NULL OR h.trangThai = :trangThai) " +


            // --- SỬA LỖI NGÀY THÁNG ---
            // Dùng CAST(... AS date) để SQL chỉ so sánh ngày, bỏ qua giờ phút giây
            "AND (:startDate IS NULL OR CAST(h.ngayTao AS date) >= :startDate) " +
            "AND (:endDate IS NULL OR CAST(h.ngayTao AS date) <= :endDate) " +


            // --- SẮP XẾP CŨ NHẤT LÊN ĐẦU (Theo yêu cầu của bạn) ---
            "ORDER BY h.ngayTao ASC, h.id ASC")
    Page<HoaDon> findAllByFilter(@Param("keyword") String keyword,
                                 @Param("loaiDon") Integer loaiDon,
                                 @Param("trangThai") Integer trangThai,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,
                                 Pageable pageable);


    // Lấy hóa đơn có ID lớn nhất để sinh mã tự động (HD001, HD002...)
    HoaDon findTopByOrderByIdDesc();
}
package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
            "AND (:startDate IS NULL OR CAST(h.ngayTao AS date) >= :startDate) " +
            "AND (:endDate IS NULL OR CAST(h.ngayTao AS date) <= :endDate) " +

            // --- THÊM ĐÚNG 1 DÒNG NÀY ĐỂ ẨN ĐƠN NHÁP TẠI QUẦY KHỎI DANH SÁCH ---
            "AND NOT (h.loaiDon = 1 AND h.trangThai = 0) " +

            // Dòng OrderBy bên dưới bạn giữ nguyên theo ý Leader của bạn nhé
            "ORDER BY h.trangThai ASC, h.ngayTao ASC")
    Page<HoaDon> findAllByFilter(@Param("keyword") String keyword,
                                 @Param("loaiDon") Integer loaiDon,
                                 @Param("trangThai") Integer trangThai,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate,
                                 Pageable pageable);

    // Lấy hóa đơn có ID lớn nhất để sinh mã tự động (HD001, HD002...)
    HoaDon findTopByOrderByIdDesc();


    List<HoaDon> findByLoaiDonAndTrangThaiAndNgayTaoBefore(
            Integer loaiDon,
            Integer trangThai,
            LocalDateTime ngay
    );

    List<HoaDon> findAllByTrangThaiAndNgayTaoBefore(Integer trangThai, LocalDateTime time);

    Optional<HoaDon> findByMaHoaDon(String maHoaDon);
    List<HoaDon> findByIdKhachHang_IdOrderByIdAsc(Integer idKhachHang);
}
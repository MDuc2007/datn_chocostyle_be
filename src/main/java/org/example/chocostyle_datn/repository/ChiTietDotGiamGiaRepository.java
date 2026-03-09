package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.ChiTietDotGiamGia;
import org.example.chocostyle_datn.entity.ChiTietDotGiamGiaId;
import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChiTietDotGiamGiaRepository extends JpaRepository<ChiTietDotGiamGia, ChiTietDotGiamGiaId> {

    List<ChiTietDotGiamGia> findById_IdDotGiamGia(Integer idDotGiamGia);

    void deleteById_IdDotGiamGia(Integer idDotGiamGia);

    // 1. Tìm khuyến mãi đang hoạt động của 1 chi tiết sản phẩm (Dùng để tính giá trong Service)
    @Query("""
SELECT c FROM ChiTietDotGiamGia c
WHERE c.idSpct.id = :idSpct
AND c.trangThai = 1
AND c.idDotGiamGia.trangThai = 1
AND c.idDotGiamGia.ngayBatDau <= CURRENT_TIMESTAMP
AND c.idDotGiamGia.ngayKetThuc >= CURRENT_TIMESTAMP
ORDER BY c.idDotGiamGia.giaTriGiam DESC
""")
    List<ChiTietDotGiamGia> findActiveDiscountBySpctId(Integer idSpct);

    // 2. 👉 THÊM MỚI: Tìm TẤT CẢ các Chi tiết sản phẩm đang được Sale (Dùng cho trang Ưu đãi)
    @Query("SELECT DISTINCT c.idSpct FROM ChiTietDotGiamGia c " +
            "WHERE c.trangThai = 1 " +
            "AND c.idDotGiamGia.trangThai = 1 " +
            "AND c.idDotGiamGia.ngayBatDau <= CURRENT_TIMESTAMP " +
            "AND c.idDotGiamGia.ngayKetThuc >= CURRENT_TIMESTAMP")
    Page<ChiTietSanPham> findActiveSaleProducts(Pageable pageable);
}
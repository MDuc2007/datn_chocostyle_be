package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SanPhamRepository extends JpaRepository<SanPham,Integer> {
    @Query("select max(sp.maSp) from SanPham sp")
    String findMaxMa();
    @Query("""
    SELECT sp FROM SanPham sp
    WHERE (:keyword IS NULL OR :keyword = ''
          OR sp.tenSp LIKE %:keyword%)
      AND (:status IS NULL OR sp.trangThai = :status)
      AND (:idChatLieu IS NULL OR sp.idChatLieu.id = :idChatLieu)
      AND (:idXuatXu IS NULL OR sp.idXuatXu.id = :idXuatXu)
    ORDER BY CAST(SUBSTRING(sp.maSp, 3) AS int) ASC
""")
    Page<SanPham> searchSanPham(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("idChatLieu") Integer idChatLieu,
            @Param("idXuatXu") Integer idXuatXu,
            Pageable pageable
    );

}

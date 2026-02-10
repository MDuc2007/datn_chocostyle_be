package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.entity.SanPham;
import org.example.chocostyle_datn.model.Response.SanPhamHomeListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    @Query("select max(sp.maSp) from SanPham sp")
    String findMaxMa();

    @Query("""
SELECT sp FROM SanPham sp
WHERE (
    :keyword IS NULL OR :keyword = ''
    OR sp.tenSp LIKE %:keyword%
    OR sp.maSp LIKE %:keyword%
)
AND (:status IS NULL OR sp.trangThai = :status)
AND (:idChatLieu IS NULL OR sp.idChatLieu.id = :idChatLieu)
AND (:idXuatXu IS NULL OR sp.idXuatXu.id = :idXuatXu)
""")
    Page<SanPham> searchSanPham(
            String keyword,
            Integer status,
            Integer idChatLieu,
            Integer idXuatXu,
            Pageable pageable
    );



    @Query("""
            SELECT COUNT(sp) > 0
            FROM SanPham sp
            WHERE LOWER(REPLACE(sp.tenSp, ' ', ''))
                  = LOWER(REPLACE(:ten, ' ', ''))
            """)
    boolean existsByTenIgnoreSpace(@Param("ten") String ten);

    @Query("""
            SELECT sp FROM SanPham sp
            WHERE (:keyword IS NULL OR sp.tenSp LIKE %:keyword%)
            AND (:status IS NULL OR sp.trangThai = :status)
            AND (:idChatLieu IS NULL OR sp.idChatLieu.id = :idChatLieu)
            AND (:idXuatXu IS NULL OR sp.idXuatXu.id = :idXuatXu)
            """)
    List<SanPham> searchSanPhamNoPage(
            String keyword,
            Integer status,
            Integer idChatLieu,
            Integer idXuatXu
    );

    Optional<SanPham> findByQrCode(String qrCode);

    @Query(value = """
                SELECT TOP 5
                    sp.id_sp           AS id,
                    sp.ten_sp          AS tenSp,
                    sp.hinh_anh        AS hinhAnh,
                    MIN(ctsp.gia_ban)  AS giaMin,
                    MAX(ctsp.gia_ban)  AS giaMax,
                    SUM(hdct.so_luong) AS soLuongDaBan
                FROM hoa_don_chi_tiet hdct
                JOIN hoa_don hd ON hdct.id_hoa_don = hd.id_hoa_don
                JOIN chi_tiet_san_pham ctsp ON hdct.id_spct = ctsp.id_spct
                JOIN san_pham sp ON ctsp.id_san_pham = sp.id_sp
                WHERE hd.trang_thai = 4
                GROUP BY sp.id_sp, sp.ten_sp, sp.hinh_anh
                ORDER BY soLuongDaBan DESC
            """, nativeQuery = true)
    List<SanPhamBanChayProjection> getSanPhamBanChay();


    @Query("""
                SELECT new org.example.chocostyle_datn.model.Response.SanPhamHomeListResponse(
                    sp.id,
                    sp.tenSp,
                    sp.hinhAnh,
                    MIN(ct.giaBan),
                    MAX(ct.giaBan),
                    null
                )
                FROM SanPham sp
                JOIN ChiTietSanPham ct ON ct.idSanPham.id = sp.id
                WHERE sp.trangThai = 1
                GROUP BY sp.id, sp.tenSp, sp.hinhAnh
            """)
    List<SanPhamHomeListResponse> getDanhSachSanPham();

}

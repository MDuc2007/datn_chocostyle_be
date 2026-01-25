package org.example.chocostyle_datn.repository;



import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, Integer> {
    List<ChiTietSanPham> findByIdSanPham(SanPham sanPham);
    void deleteByIdSanPham(SanPham sanPham);
    @Query("select max(ctsp.maChiTietSanPham) from ChiTietSanPham ctsp")
    String findMaxMa();
    List<ChiTietSanPham> findByIdSanPham_Id(Integer idSanPham);
    @Query("""
            SELECT ctsp FROM ChiTietSanPham ctsp
            WHERE ctsp.idSanPham.id = :productId
            AND (:keyword IS NULL OR LOWER(ctsp.maChiTietSanPham) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:mauSacId IS NULL OR ctsp.idMauSac.id = :mauSacId)
            AND (:kichCoId IS NULL OR ctsp.idKichCo.id = :kichCoId)
            """)
    Page<ChiTietSanPham> filterCTSP(
            @Param("productId") Long productId,
            @Param("keyword") String keyword,
            @Param("mauSacId") Long mauSacId,
            @Param("kichCoId") Long kichCoId,
            Pageable pageable
    );

}

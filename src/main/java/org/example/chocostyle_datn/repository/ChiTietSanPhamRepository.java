package org.example.chocostyle_datn.repository;


import jakarta.persistence.LockModeType;
import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, Integer> {
    List<ChiTietSanPham> findByIdSanPham(SanPham sanPham);

    void deleteByIdSanPham(SanPham sanPham);

    @Query("select max(ctsp.maChiTietSanPham) from ChiTietSanPham ctsp")
    String findMaxMa();

    List<ChiTietSanPham> findByIdSanPham_Id(Integer idSanPham);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ChiTietSanPham c WHERE c.id = :id")
    Optional<ChiTietSanPham> findByIdForUpdate(@Param("id") Integer id);

    @Query("""
    SELECT ctsp FROM ChiTietSanPham ctsp
    WHERE (:productId IS NULL OR ctsp.idSanPham.id = :productId)
      AND (
            :keyword IS NULL 
            OR LOWER(ctsp.maChiTietSanPham) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(ctsp.idSanPham.maSp) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
      AND (:mauSacId IS NULL OR ctsp.idMauSac.id = :mauSacId)
      AND (:kichCoId IS NULL OR ctsp.idKichCo.id = :kichCoId)
      AND (:trangThai IS NULL OR ctsp.trangThai = :trangThai)
      AND (:minPrice IS NULL OR ctsp.giaBan >= :minPrice)
      AND (:maxPrice IS NULL OR ctsp.giaBan <= :maxPrice)
""")
    Page<ChiTietSanPham> filter(
            @Param("productId") Integer productId,
            @Param("keyword") String keyword,
            @Param("mauSacId") Integer mauSacId,
            @Param("kichCoId") Integer kichCoId,
            @Param("trangThai") Integer trangThai,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );


    Optional<ChiTietSanPham> findByQrCode(String qrCode);

}
package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {

    // Hàm cũ của bạn (giữ nguyên vì nó đúng)
    List<HoaDonChiTiet> findByIdHoaDon_Id(Integer idHoaDon);

    // Hàm cũ của bạn (giữ nguyên)
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM hoa_don_chi_tiet WHERE id_hoa_don = :idHoaDon", nativeQuery = true)
    void xoaChiTietTheoIdHoaDon(@Param("idHoaDon") Integer idHoaDon);

}
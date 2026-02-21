package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.LichSuHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface LichSuHoaDonRepository extends JpaRepository<LichSuHoaDon, Integer> {
    // Tìm lịch sử theo hóa đơn và sắp xếp thời gian giảm dần (mới nhất lên đầu)
    List<LichSuHoaDon> findByIdHoaDon_IdOrderByThoiGianDesc(Integer idHoaDon);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM lich_su_hoa_don WHERE id_hoa_don = :idHoaDon", nativeQuery = true)
    void xoaLichSuTheoIdHoaDon(@Param("idHoaDon") Integer idHoaDon);
}
package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.LichSuHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LichSuHoaDonRepository extends JpaRepository<LichSuHoaDon, Integer> {
    // Tìm lịch sử theo hóa đơn và sắp xếp thời gian giảm dần (mới nhất lên đầu)
    List<LichSuHoaDon> findByIdHoaDon_IdOrderByThoiGianDesc(Integer idHoaDon);
}
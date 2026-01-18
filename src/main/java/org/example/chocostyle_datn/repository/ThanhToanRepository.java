package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.ThanhToan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ThanhToanRepository extends JpaRepository<ThanhToan, Integer> {
    // Tìm thông tin thanh toán theo hóa đơn
    List<ThanhToan> findByIdHoaDon_Id(Integer idHoaDon);
}
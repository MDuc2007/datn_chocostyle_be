package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {
    // Tìm danh sách chi tiết sản phẩm theo ID hóa đơn
    // Cú pháp: findBy + TênBiếnTrongEntity (idHoaDon) + _ + Id (của bảng HoaDon)
    List<HoaDonChiTiet> findByIdHoaDon_Id(Integer idHoaDon);
}

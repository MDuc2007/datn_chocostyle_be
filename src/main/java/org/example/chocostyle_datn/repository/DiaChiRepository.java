package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaChiRepository extends JpaRepository<DiaChi, Integer> {

    // 1. Lấy danh sách địa chỉ của khách hàng
    List<DiaChi> findByKhachHangId(Integer khachHangId);

    // 2. Tìm địa chỉ đang là MẶC ĐỊNH của khách hàng (Để tắt nó đi khi chọn cái khác)
    // Spring Data tự hiểu: tìm theo KhachHangId VÀ MacDinh = true
    Optional<DiaChi> findByKhachHangIdAndMacDinhTrue(Integer khachHangId);

    // 3. Xóa tất cả địa chỉ của khách hàng (Cần annotation để xác nhận thay đổi DB)
    @Modifying
    @Transactional
    void deleteByKhachHangId(Integer khachHangId);


}
package org.example.chocostyle_datn.repository;


import org.example.chocostyle_datn.entity.LichLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;


@Repository
public interface LichLamViecRepository extends JpaRepository<LichLamViec, Integer> {


    // 1. Lấy tất cả lịch sắp xếp mới nhất
    List<LichLamViec> findAllByOrderByNgayLamViecDesc();


    // 2. Lấy lịch theo khoảng ngày (nếu cần filter)
    @Query("SELECT l FROM LichLamViec l WHERE l.ngayLamViec BETWEEN :from AND :to ORDER BY l.ngayLamViec ASC")
    List<LichLamViec> findByDateRange(LocalDate from, LocalDate to);


    // 3. Tìm lịch của nhân viên trong ngày cụ thể để check trùng
    @Query("SELECT l FROM LichLamViec l WHERE l.nhanVien.id = :idNv AND l.ngayLamViec = :ngay")
    List<LichLamViec> findByNhanVienAndNgay(Integer idNv, LocalDate ngay);
}


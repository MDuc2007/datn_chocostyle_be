package org.example.chocostyle_datn.repository;


import org.example.chocostyle_datn.entity.LichLamViec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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


    // Tìm tất cả lịch của nhân viên X trong ngày Y (để check trùng giờ)
    @Query("SELECT l FROM LichLamViec l WHERE l.nhanVien.id = :idNv AND l.ngayLamViec = :ngay AND l.trangThai = 1")
    List<LichLamViec> findByNhanVienAndDate(@Param("idNv") Integer idNv, @Param("ngay") LocalDate ngay);
    // . Lấy lịch theo khoảng ngày (Phục vụ Filter trong Service)
    // Spring Data JPA sẽ tự động tạo query dựa trên tên hàm này
    List<LichLamViec> findByNgayLamViecBetweenOrderByNgayLamViecDesc(LocalDate from, LocalDate to);
    // Tìm các lịch cùng mã lặp lại mà ngày >= hôm nay (để xóa hàng loạt)
    List<LichLamViec> findByMaLapLaiAndNgayLamViecGreaterThanEqual(String maLapLai, LocalDate today);
    // Thêm vào interface LichLamViecRepository
    @Query("SELECT l FROM LichLamViec l WHERE l.caLamViec.idCa = :idCa AND l.ngayLamViec = :ngay")
    List<LichLamViec> findByCaAndNgay(@Param("idCa") Integer idCa, @Param("ngay") LocalDate ngay);

    // THÊM HÀM SEARCH PHÂN TRANG
    @Query("SELECT l FROM LichLamViec l WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR l.nhanVien.hoTen LIKE %:keyword% OR l.nhanVien.maNv LIKE %:keyword%) AND " +
            "(l.ngayLamViec >= :fromDate) AND " +
            "(l.ngayLamViec <= :toDate) AND " +
            "(:trangThai IS NULL OR l.trangThai = :trangThai)")
    Page<LichLamViec> searchLichLamViec(
            @Param("keyword") String keyword,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("trangThai") Integer trangThai,
            Pageable pageable
    );
    // Lấy tất cả lịch của 1 nhân viên (dùng cho Calendar view)
    List<LichLamViec> findByNhanVien_Id(Integer idNv);

    // Lấy lịch có phân trang của 1 nhân viên (dùng cho Table view)
    @Query("SELECT l FROM LichLamViec l WHERE " +
            "(l.nhanVien.id = :idNv) AND " +
            "(l.ngayLamViec >= :fromDate) AND " +
            "(l.ngayLamViec <= :toDate) AND " +
            "(:trangThai IS NULL OR l.trangThai = :trangThai)")
    Page<LichLamViec> searchMySchedules(
            @Param("idNv") Integer idNv,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("trangThai") Integer trangThai,
            Pageable pageable
    );
}




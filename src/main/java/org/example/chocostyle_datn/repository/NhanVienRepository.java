package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.NhanVien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NhanVienRepository extends JpaRepository<NhanVien,Integer> {
    // Dùng Native Query (SQL Server) để lấy 1 dòng đầu tiên, sắp xếp ID giảm dần
    @Query(value = "SELECT TOP 1 * FROM nhan_vien ORDER BY id_nv DESC", nativeQuery = true)
    NhanVien findNhanVienMoiNhat();
    @Query("SELECT n.maNv FROM NhanVien n WHERE n.maNv LIKE :prefix% ORDER BY LENGTH(n.maNv) DESC, n.maNv DESC LIMIT 1")
    String findMaxMaNvByPrefix(@Param("prefix") String prefix);

    @Query("SELECT n FROM NhanVien n WHERE n.email = :input OR n.maNv = :input")
    Optional<NhanVien> findByEmailOrMaNhanVien(@Param("input") String input);

    Optional<NhanVien> findByEmail(String email);

    // Sử dụng nativeQuery = true để chọc thẳng vào SQL
    @Query(value = "SELECT COUNT(*) FROM khach_hang WHERE email = :email", nativeQuery = true)
    int countEmailInKhachHang(@Param("email") String email);
    // 1. Check trùng khi Thêm mới
    boolean existsByEmail(String email);
    boolean existsBySoDienThoai(String soDienThoai);


    // 2. Check trùng khi Cập nhật (Trừ chính ID đang sửa ra)
    boolean existsByEmailAndIdNot(String email, Integer id);
    boolean existsBySoDienThoaiAndIdNot(String soDienThoai, Integer id);

    @Query("SELECT n FROM NhanVien n WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "n.hoTen LIKE %:keyword% OR n.maNv LIKE %:keyword% OR n.email LIKE %:keyword% OR n.soDienThoai LIKE %:keyword%) " +
            "AND (:trangThai IS NULL OR n.trangThai = :trangThai)")
    Page<NhanVien> searchNhanVien(
            @Param("keyword") String keyword,
            @Param("trangThai") Integer trangThai,
            Pageable pageable
    );
    // Tìm nhân viên đang trong ca trực dựa theo giờ hiện tại và ngày hiện tại
    @Query(value = "SELECT TOP 1 nv.* FROM nhan_vien nv " +
            "JOIN lich_lam_viec lsv ON nv.id_nv = lsv.id_nhan_vien " +
            "JOIN ca_lam_viec clv ON lsv.id_ca = clv.id_ca " +
            "WHERE lsv.ngay_lam_viec = CAST(GETDATE() AS DATE) " +
            "AND CAST(GETDATE() AS TIME) BETWEEN clv.gio_bat_dau AND clv.gio_ket_thuc " +
            "AND nv.trang_thai = 1 AND lsv.trang_thai = 1", nativeQuery = true)
    Optional<NhanVien> findNhanVienDangTrongCa();

    // Nếu không có ai trong ca, lấy nhân viên quản lý hoặc nhân viên bất kỳ đang hoạt động
    @Query(value = "SELECT TOP 1 * FROM nhan_vien WHERE trang_thai = 1 AND vai_tro = 'ADMIN'", nativeQuery = true)
    NhanVien findNhanVienDuPhong();
}

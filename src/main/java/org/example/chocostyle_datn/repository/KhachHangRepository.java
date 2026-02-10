package org.example.chocostyle_datn.repository;


import org.example.chocostyle_datn.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;


@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {


    // =========================================================================
    // PHẦN 1: DÀNH CHO AUTHENTICATION (QUAN TRỌNG NHẤT)
    // =========================================================================


    /**
     * Phương thức này giải quyết vấn đề đăng nhập bằng cả Username hoặc Email.
     * Nó nhận vào 1 chuỗi (input) và kiểm tra xem chuỗi đó có trùng với tenTaiKhoan
     * HOẶC trùng với email trong database không.
     */
    @Query("SELECT k FROM KhachHang k WHERE k.tenTaiKhoan = :input OR k.email = :input")
    Optional<KhachHang> findByTenTaiKhoanOrEmail(@Param("input") String input);


    // Tìm user để đăng nhập thường (Giữ lại nếu cần dùng riêng lẻ)
    Optional<KhachHang> findByTenTaiKhoan(String tenTaiKhoan);


    // Tìm user để đăng nhập Google/Facebook
    Optional<KhachHang> findByEmail(String email);


    // Check trùng
    boolean existsByTenTaiKhoan(String tenTaiKhoan);
    boolean existsBySoDienThoai(String soDienThoai);
    boolean existsByEmail(String email);
    boolean existsByMaKh(String maKh);


    boolean existsBySoDienThoaiAndIdNot(String soDienThoai, Integer id);
    boolean existsByEmailAndIdNot(String email, Integer id);
    boolean existsByTenTaiKhoanAndIdNot(String tenTaiKhoan, Integer id);
    // =========================================================================
    // PHẦN 2: TÌM KIẾM & LỌC (ADMIN)
    // =========================================================================


    @Query("""
       SELECT kh FROM KhachHang kh
       WHERE ( :keyword IS NULL OR :keyword = ''
               OR kh.tenKhachHang LIKE %:keyword%
               OR kh.soDienThoai LIKE %:keyword%
               OR kh.email LIKE %:keyword%
               OR kh.maKh LIKE %:keyword% )
       AND ( :status IS NULL OR kh.trangThai = :status )
   """)
    Page<KhachHang> searchKhachHang(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            Pageable pageable
    );


    // Đếm số lượng theo trạng thái
    long countByTrangThai(Integer trangThai);


    @Query("""
   SELECT kh FROM KhachHang kh
   WHERE (:keyword IS NULL OR
          kh.tenKhachHang LIKE %:keyword% OR
          kh.soDienThoai LIKE %:keyword% OR
          kh.email LIKE %:keyword%)
     AND (:status IS NULL OR kh.trangThai = :status)
   ORDER BY kh.ngayTao DESC
""")
    List<KhachHang> searchKhachHangForExport(
            @Param("keyword") String keyword,
            @Param("status") Integer status
    );

}

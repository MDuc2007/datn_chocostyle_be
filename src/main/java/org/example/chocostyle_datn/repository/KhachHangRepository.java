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
    // PHẦN 1: AUTHENTICATION (Đăng nhập bằng Email)
    // =========================================================================


    /**
     * Tìm kiếm user theo Email (Dùng cho cả Login thường và OAuth2)
     * Vì quy định login bằng email nên hàm này là quan trọng nhất.
     */
    Optional<KhachHang> findByEmail(String email);

    List<KhachHang> findByTrangThai(Integer trangThai);

    // --- CHECK TRÙNG DỮ LIỆU ---


    // Check trùng SĐT
    boolean existsBySoDienThoai(String soDienThoai);


    // Check trùng Email
    boolean existsByEmail(String email);


    // Check trùng Mã KH
    boolean existsByMaKh(String maKh);


    // --- CHECK TRÙNG KHI UPDATE (Trừ ID hiện tại ra) ---
    boolean existsBySoDienThoaiAndIdNot(String soDienThoai, Integer id);
    boolean existsByEmailAndIdNot(String email, Integer id);


    // (Đã XÓA các hàm liên quan đến TenTaiKhoan tại đây)


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


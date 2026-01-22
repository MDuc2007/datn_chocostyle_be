package org.example.chocostyle_datn.repository;


import org.example.chocostyle_datn.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang,Integer> {
    // --- PHẦN 1: TÌM KIẾM & LỌC (Dùng cho trang danh sách) ---

    // Tìm kiếm linh hoạt.
    // COALESCE(:keyword, '') để xử lý null an toàn hơn.
    @Query("""
        SELECT kh FROM KhachHang kh
        WHERE ( :keyword IS NULL OR :keyword = '' 
                OR kh.tenKhachHang LIKE %:keyword% 
                OR kh.soDienThoai LIKE %:keyword% 
                OR kh.email LIKE %:keyword% )
        AND ( :status IS NULL OR kh.trangThai = :status )
    """)
    Page<KhachHang> searchKhachHang(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            Pageable pageable
    );
    // Lưu ý: Sort sẽ được xử lý tự động thông qua biến 'pageable' truyền vào.

    // --- PHẦN 2: VALIDATION (Dùng check trùng khi Thêm/Sửa) ---

    // Kiểm tra trùng Số điện thoại
    boolean existsBySoDienThoai(String soDienThoai);

    // Kiểm tra trùng Email (nếu email không bắt buộc thì check null trước ở service)
    boolean existsByEmail(String email);

    // Kiểm tra trùng Mã khách hàng (nếu tự nhập)
    boolean existsByMaKh(String maKh);

    // Tìm khách hàng theo ID để update (tránh dùng getById bị lỗi Lazy loading)
    Optional<KhachHang> findById(Integer id);

    // --- PHẦN 3: THỐNG KÊ NHANH (Dùng cho Dashboard/Tabs) ---

    // Đếm số lượng theo trạng thái
    long countByTrangThai(Integer trangThai);
}

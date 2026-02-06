package org.example.chocostyle_datn.repository;


import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.model.Response.DoanhThuResponse;
import org.example.chocostyle_datn.model.Response.SanPhamBanChayResponse;
import org.example.chocostyle_datn.model.Response.TrangThaiDonResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.List;


@Repository
public interface ThongKeRepository extends JpaRepository<ChiTietSanPham, Integer> {


    // 1. BIỂU ĐỒ DOANH THU (Đã sửa hàm Date an toàn hơn)
    @Query(value = """
       SELECT
           -- Dùng CONVERT thay cho FORMAT để tránh lỗi phiên bản SQL
           CONVERT(VARCHAR(10), h.ngay_thanh_toan, 120) as thoiGian,
           SUM(h.tong_tien_thanh_toan) as doanhThu,
           COUNT(h.id_hoa_don) as soLuongDon
       FROM hoa_don h
       WHERE h.trang_thai = 4
         AND h.ngay_thanh_toan IS NOT NULL
         AND CAST(h.ngay_thanh_toan AS DATE) >= :startDate
         AND CAST(h.ngay_thanh_toan AS DATE) <= :endDate
       GROUP BY CONVERT(VARCHAR(10), h.ngay_thanh_toan, 120)
       ORDER BY thoiGian ASC
   """, nativeQuery = true)
    List<DoanhThuResponse> getDoanhThuChart(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);


    // 2. TOP SẢN PHẨM BÁN CHẠY (Đã sửa lỗi lấy sai cột Ảnh)
    @Query(value = """
       SELECT TOP 5
           sp.ten_sp as tenSanPham,
           -- FIX LỖI Ở ĐÂY: Join bảng con để lấy ảnh đúng
           (SELECT TOP 1 ha.url_anh
            FROM hinh_anh_san_pham ha
            JOIN chi_tiet_san_pham c ON ha.id_san_pham_chi_tiet = c.id_spct
            WHERE c.id_san_pham = sp.id_sp) as anh,
           MAX(ctsp.gia_ban) as giaBan,
           SUM(hdct.so_luong) as soLuongDaBan
       FROM hoa_don_chi_tiet hdct
       JOIN hoa_don hd ON hdct.id_hoa_don = hd.id_hoa_don
       JOIN chi_tiet_san_pham ctsp ON hdct.id_spct = ctsp.id_spct
       JOIN san_pham sp ON ctsp.id_san_pham = sp.id_sp
       WHERE hd.trang_thai = 4
       GROUP BY sp.id_sp, sp.ten_sp
       ORDER BY soLuongDaBan DESC
   """, nativeQuery = true)
    List<SanPhamBanChayResponse> getTopBanChay();


    // 3. TỶ LỆ TRẠNG THÁI (Giữ nguyên)
    @Query(value = """
       SELECT
           trang_thai as trangThai,
           COUNT(id_hoa_don) as soLuong
       FROM hoa_don
       GROUP BY trang_thai
   """, nativeQuery = true)
    List<TrangThaiDonResponse> getTrangThaiDon();


    // 4. SẢN PHẨM SẮP HẾT (Giữ nguyên)
    @Query("""
   SELECT ct FROM ChiTietSanPham ct
   JOIN FETCH ct.idSanPham sp      
   JOIN FETCH ct.idMauSac ms       
   JOIN FETCH ct.idKichCo kc       
   WHERE ct.soLuongTon <= :limit
   ORDER BY ct.soLuongTon ASC
""")
    List<ChiTietSanPham> getSanPhamSapHetHang(@Param("limit") int limit);




    // Thêm vào interface ThongKeRepository
    @Query(value = """
       SELECT
           h.loai_don,
           COUNT(h.id_hoa_don)
       FROM hoa_don h
       WHERE h.trang_thai != 5 -- (Tuỳ chọn: Loại bỏ đơn đã huỷ nếu muốn)
       GROUP BY h.loai_don
   """, nativeQuery = true)
    List<Object[]> getPhanBoLoaiDonRaw();
// Lưu ý: Có thể tái sử dụng Interface projection ITrangThaiDonResponse hoặc tạo mới nếu cần
}


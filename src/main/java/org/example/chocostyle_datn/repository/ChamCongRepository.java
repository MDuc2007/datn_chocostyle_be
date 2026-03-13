package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.ChamCong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ChamCongRepository extends JpaRepository<ChamCong, Integer> {

    Optional<ChamCong> findByNhanVien_IdAndNgay(Integer idNv, LocalDate ngay);

    @Query(value = "SELECT " +
            "cc.id as id, " +
            "nv.ho_ten as nhanVien, " +
            "ISNULL(clv.ten_ca, N'Không xác định') as ca, " +
            "CONVERT(varchar, cc.ngay, 103) as ngayStr, " +
            "CONVERT(varchar, cc.gio_check_in, 8) as gioCheckInStr, " +
            "CONVERT(varchar, cc.gio_check_out, 8) as gioCheckOutStr, " +
            "cc.trang_thai as trangThai, " +
            "ISNULL(cc.tien_mat_cuoi_ca, 0) as tienMat, " +
            "ISNULL(cc.tien_chuyen_khoan_cuoi_ca, 0) as tienChuyenKhoan, " +
            "ISNULL(cc.tien_mat_dau_ca, 0) as tienMatDauCa, " +
            "ISNULL(cc.tien_chuyen_khoan_dau_ca, 0) as tienChuyenKhoanDauCa, " +
            "ISNULL(cc.tong_doanh_thu, 0) as tongDoanhThu, " +
            "ISNULL(cc.tien_chenh_lech, 0) as tienChenhLech, " +
            "ISNULL(cc.ghi_chu, '') as ghiChu, " +
            "ISNULL(cc.doanh_thu_tien_mat, 0) as doanhThuTienMat, " +
            "ISNULL(cc.doanh_thu_ck, 0) as doanhThuCk, " +
            "ISNULL(cc.chenh_lech_tien_mat, 0) as chenhLechTienMat, " +
            "ISNULL(cc.chenh_lech_ck, 0) as chenhLechCk, " +
            "ISNULL(cc.ten_nguoi_mo_ca, N'Chưa xác định') as tenNguoiMoCa, " +
            "ISNULL(cc.ten_nguoi_dong_ca, N'Chưa đóng') as tenNguoiDongCa, " +
            "ISNULL(cc.so_luong_hoa_don, 0) as soLuongHoaDon " +
            "FROM cham_cong cc " +
            "JOIN nhan_vien nv ON cc.id_nhan_vien = nv.id_nv " +
            "OUTER APPLY ( " +
            "    SELECT TOP 1 c.ten_ca, c.ma_ca " +
            "    FROM lich_lam_viec l " +
            "    JOIN ca_lam_viec c ON l.id_ca = c.id_ca " +
            "    WHERE l.id_nhan_vien = cc.id_nhan_vien AND l.ngay_lam_viec = cc.ngay " +
            "    ORDER BY ABS(DATEDIFF(MINUTE, cc.gio_check_in, c.gio_bat_dau)) " +
            ") clv " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR nv.ho_ten LIKE '%' + :keyword + '%' OR clv.ma_ca LIKE '%' + :keyword + '%') " +
            "AND (:fromDate IS NULL OR :fromDate = '' OR cc.ngay >= CAST(:fromDate AS DATE)) " +
            "AND (:toDate IS NULL OR :toDate = '' OR cc.ngay <= CAST(:toDate AS DATE)) " +
            "ORDER BY cc.ngay DESC, cc.gio_check_in DESC", nativeQuery = true)
    List<Map<String, Object>> getDanhSachGiaoCa(@Param("keyword") String keyword,
                                                @Param("fromDate") String fromDate,
                                                @Param("toDate") String toDate);

    @Query(value = "SELECT ISNULL(SUM(tong_tien_thanh_toan), 0) FROM hoa_don " +
            "WHERE id_nhan_vien = :idNv " +
            "AND ngay_tao >= :startDateTime " +
            "AND ngay_tao <= :endDateTime " +
            "AND trang_thai IN (2, 4)", nativeQuery = true)
    Double calculateDoanhThuTrongCa(@Param("idNv") Integer idNv,
                                    @Param("startDateTime") java.time.LocalDateTime startDateTime,
                                    @Param("endDateTime") java.time.LocalDateTime endDateTime);

    // 1. TÍNH DOANH THU TIỀN MẶT CỦA 1 NHÂN VIÊN
    @Query(value = "SELECT ISNULL(SUM(tt.so_tien), 0) " +
            "FROM hoa_don hd " +
            "JOIN thanh_toan tt ON hd.id_hoa_don = tt.id_hoa_don " +
            "JOIN phuong_thuc_thanh_toan pt ON tt.id_pttt = pt.id_pttt " +
            "WHERE hd.id_nhan_vien = :idNv " +
            "AND hd.ngay_tao >= :startDateTime " +
            "AND hd.ngay_tao <= :endDateTime " +
            "AND hd.trang_thai IN (2, 4) " +
            "AND tt.trang_thai = 1 " +
            "AND pt.ma_pttt = 'TIENMAT'", nativeQuery = true)
    Double calculateDoanhThuTienMat(@Param("idNv") Integer idNv,
                                    @Param("startDateTime") java.time.LocalDateTime startDateTime,
                                    @Param("endDateTime") java.time.LocalDateTime endDateTime);

    // 2. TÍNH DOANH THU CHUYỂN KHOẢN CỦA 1 NHÂN VIÊN
    @Query(value = "SELECT ISNULL(SUM(tt.so_tien), 0) " +
            "FROM hoa_don hd " +
            "JOIN thanh_toan tt ON hd.id_hoa_don = tt.id_hoa_don " +
            "JOIN phuong_thuc_thanh_toan pt ON tt.id_pttt = pt.id_pttt " +
            "WHERE hd.id_nhan_vien = :idNv " +
            "AND hd.ngay_tao >= :startDateTime " +
            "AND hd.ngay_tao <= :endDateTime " +
            "AND hd.trang_thai IN (2, 4) " +
            "AND tt.trang_thai = 1 " +
            "AND pt.ma_pttt != 'TIENMAT'", nativeQuery = true)
    Double calculateDoanhThuChuyenKhoan(@Param("idNv") Integer idNv,
                                        @Param("startDateTime") java.time.LocalDateTime startDateTime,
                                        @Param("endDateTime") java.time.LocalDateTime endDateTime);

    @Query("SELECT c FROM ChamCong c WHERE c.nhanVien.id = :idNv AND c.ngay = :ngay ORDER BY c.id DESC")
    List<ChamCong> findDanhSachChamCongHomNay(@Param("idNv") Integer idNv, @Param("ngay") LocalDate ngay);

    @Query(value = "SELECT TOP 1 * FROM cham_cong WHERE trang_thai = 1 ORDER BY id DESC", nativeQuery = true)
    ChamCong layCaDongGanNhat();

    @Query("SELECT c FROM ChamCong c WHERE c.gioCheckOut IS NULL")
    List<ChamCong> findTatCaCaDangMo();

    @Query(value = "SELECT COUNT(*) FROM hoa_don WHERE id_nhan_vien = :idNv AND ngay_tao >= :start AND ngay_tao <= :end AND trang_thai IN (2, 4)", nativeQuery = true)
    Integer countHoaDonTrongCa(@Param("idNv") Integer idNv, @Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    // =========================================================================
    // CÁC HÀM MỚI PHỤC VỤ TÍNH NĂNG "DÙNG CHUNG KÉT TIỀN TOÀN CỬA HÀNG"
    // =========================================================================

    // Kiểm tra xem toàn bộ cửa hàng có ai đang mở ca không?
    @Query("SELECT c FROM ChamCong c WHERE c.ngay = :ngay AND c.trangThai = 3 ORDER BY c.id DESC")
    List<ChamCong> findCaDangMoCuaCuaHang(@Param("ngay") LocalDate ngay);

    // Tính TỔNG doanh thu tiền mặt của TẤT CẢ nhân viên trong ca
    @Query(value = "SELECT ISNULL(SUM(tt.so_tien), 0) " +
            "FROM hoa_don hd " +
            "JOIN thanh_toan tt ON hd.id_hoa_don = tt.id_hoa_don " +
            "JOIN phuong_thuc_thanh_toan pt ON tt.id_pttt = pt.id_pttt " +
            "WHERE hd.ngay_tao >= :startDateTime " +
            "AND hd.ngay_tao <= :endDateTime " +
            "AND hd.trang_thai IN (2, 4) " +
            "AND tt.trang_thai = 1 " +
            "AND pt.ma_pttt = 'TIENMAT'", nativeQuery = true)
    Double calculateDoanhThuTienMatChung(@Param("startDateTime") java.time.LocalDateTime startDateTime,
                                         @Param("endDateTime") java.time.LocalDateTime endDateTime);

    // Tính TỔNG doanh thu chuyển khoản của TẤT CẢ nhân viên trong ca
    @Query(value = "SELECT ISNULL(SUM(tt.so_tien), 0) " +
            "FROM hoa_don hd " +
            "JOIN thanh_toan tt ON hd.id_hoa_don = tt.id_hoa_don " +
            "JOIN phuong_thuc_thanh_toan pt ON tt.id_pttt = pt.id_pttt " +
            "WHERE hd.ngay_tao >= :startDateTime " +
            "AND hd.ngay_tao <= :endDateTime " +
            "AND hd.trang_thai IN (2, 4) " +
            "AND tt.trang_thai = 1 " +
            "AND pt.ma_pttt != 'TIENMAT'", nativeQuery = true)
    Double calculateDoanhThuChuyenKhoanChung(@Param("startDateTime") java.time.LocalDateTime startDateTime,
                                             @Param("endDateTime") java.time.LocalDateTime endDateTime);

    // Đếm TỔNG số hóa đơn của TẤT CẢ nhân viên
    @Query(value = "SELECT COUNT(*) FROM hoa_don WHERE ngay_tao >= :start AND ngay_tao <= :end AND trang_thai IN (2, 4)", nativeQuery = true)
    Integer countHoaDonTrongCaChung(@Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}
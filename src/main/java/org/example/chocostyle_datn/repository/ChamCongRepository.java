package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.ChamCong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
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
            "ISNULL(cc.ghi_chu, '') as ghiChu " +
            "FROM cham_cong cc " +
            "JOIN nhan_vien nv ON cc.id_nhan_vien = nv.id_nv " +
            "LEFT JOIN lich_lam_viec llv ON llv.id_nhan_vien = cc.id_nhan_vien AND llv.ngay_lam_viec = cc.ngay " +
            "LEFT JOIN ca_lam_viec clv ON llv.id_ca = clv.id_ca " +
            "WHERE (:keyword IS NULL OR :keyword = '' OR nv.ho_ten LIKE '%' + :keyword + '%' OR clv.ma_ca LIKE '%' + :keyword + '%') " +
            "  AND (:fromDate IS NULL OR :fromDate = '' OR cc.ngay >= CAST(:fromDate AS DATE)) " +
            "  AND (:toDate IS NULL OR :toDate = '' OR cc.ngay <= CAST(:toDate AS DATE)) " +
            "ORDER BY cc.ngay DESC, cc.gio_check_in DESC",
            nativeQuery = true)
    List<Map<String, Object>> getDanhSachGiaoCa(
            @Param("keyword") String keyword,
            @Param("fromDate") String fromDate,
            @Param("toDate") String toDate
    );
    @Query(value = "SELECT ISNULL(SUM(tong_tien_thanh_toan), 0) FROM hoa_don " +
            "WHERE id_nhan_vien = :idNv " +
            "AND ngay_tao >= :startDateTime " +
            "AND ngay_tao <= :endDateTime " +
            "AND trang_thai IN (2, 4)", nativeQuery = true)
    Double calculateDoanhThuTrongCa(@Param("idNv") Integer idNv,
                                    @Param("startDateTime") java.time.LocalDateTime startDateTime,
                                    @Param("endDateTime") java.time.LocalDateTime endDateTime);

    // 1. TÍNH DOANH THU TIỀN MẶT
    @Query(value = "SELECT ISNULL(SUM(tt.so_tien), 0) " +
            "FROM hoa_don hd " +
            "JOIN thanh_toan tt ON hd.id_hoa_don = tt.id_hoa_don " +
            "JOIN phuong_thuc_thanh_toan pt ON tt.id_pttt = pt.id_pttt " +
            "WHERE hd.id_nhan_vien = :idNv " +
            "AND hd.ngay_tao >= :startDateTime " +
            "AND hd.ngay_tao <= :endDateTime " +
            "AND hd.trang_thai IN (2, 4) " +
            "AND tt.trang_thai = 1 " + // 1 = Giao dịch thanh toán thành công
            "AND pt.ma_pttt = 'TIENMAT'", nativeQuery = true)
    Double calculateDoanhThuTienMat(@Param("idNv") Integer idNv,
                                    @Param("startDateTime") java.time.LocalDateTime startDateTime,
                                    @Param("endDateTime") java.time.LocalDateTime endDateTime);

    // 2. TÍNH DOANH THU CHUYỂN KHOẢN (Các phương thức khác TIENMAT)
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
}
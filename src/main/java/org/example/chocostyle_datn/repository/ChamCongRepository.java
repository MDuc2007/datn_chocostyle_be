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
            "ISNULL(( " +
            "   SELECT SUM(hd.tong_tien_thanh_toan) " +
            "   FROM hoa_don hd " +
            "   WHERE hd.id_nhan_vien = cc.id_nhan_vien " +
            "     AND CAST(hd.ngay_tao AS DATE) = cc.ngay " +
            "     AND CAST(hd.ngay_tao AS TIME) >= cc.gio_check_in " +
            "     AND (cc.gio_check_out IS NULL OR CAST(hd.ngay_tao AS TIME) <= cc.gio_check_out) " +
            "     AND hd.trang_thai IN (2, 4) " + // Đếm các đơn đã thanh toán/hoàn thành
            "), 0) as tongDoanhThu " +
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

}
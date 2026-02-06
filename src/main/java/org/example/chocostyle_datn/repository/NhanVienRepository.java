package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.NhanVien;
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

}

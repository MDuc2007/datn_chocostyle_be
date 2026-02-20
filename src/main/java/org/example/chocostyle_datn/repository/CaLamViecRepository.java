package org.example.chocostyle_datn.repository;


import org.example.chocostyle_datn.entity.CaLamViec;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalTime;
import java.util.List;


@Repository
public interface CaLamViecRepository extends JpaRepository<CaLamViec, Integer> {
    boolean existsByTenCaAndIdCaNot(String tenCa, Integer idCa); // Check trùng tên khi update
    boolean existsByTenCa(String tenCa); // Check trùng tên khi create

    // BỎ CHỮ 'IS NULL' Ở PHẦN GIỜ ĐI
    // Dùng Native Query để qua mặt cơ chế kiểm tra kiểu dữ liệu khắt khe của Hibernate
    @Query(value = "SELECT * FROM ca_lam_viec WHERE " +
            "(:trangThai IS NULL OR trang_thai = :trangThai) AND " +
            "(:gioBatDau IS NULL OR gio_bat_dau >= :gioBatDau) AND " +
            "(:gioKetThuc IS NULL OR gio_ket_thuc <= :gioKetThuc)",
            nativeQuery = true)
    Page<CaLamViec> searchCaLamViec(
            @Param("trangThai") Integer trangThai,
            @Param("gioBatDau") String gioBatDau,
            @Param("gioKetThuc") String gioKetThuc,
            Pageable pageable
    );
}


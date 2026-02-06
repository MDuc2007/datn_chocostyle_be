package org.example.chocostyle_datn.repository;


import org.example.chocostyle_datn.entity.CaLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;


@Repository
public interface CaLamViecRepository extends JpaRepository<CaLamViec, Integer> {
    // Tìm kiếm ca theo mã hoặc tên để phục vụ search ở frontend
    List<CaLamViec> findByTenCaContainingOrMaCaContaining(String ten, String ma);
}


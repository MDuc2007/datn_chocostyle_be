package org.example.chocostyle_datn.repository;


import org.example.chocostyle_datn.entity.CaLamViec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;


@Repository
public interface CaLamViecRepository extends JpaRepository<CaLamViec, Integer> {
    boolean existsByTenCaAndIdCaNot(String tenCa, Integer idCa); // Check trùng tên khi update
    boolean existsByTenCa(String tenCa); // Check trùng tên khi create
}


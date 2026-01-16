package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SanPhamRepository extends JpaRepository<SanPham,Integer> {
    @Query("select max(sp.maSp) from SanPham sp")
    String findMaxMa();
}

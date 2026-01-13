package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.HinhAnhSanPham;
import org.example.chocostyle_datn.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HinhAnhSanPhamRepository extends JpaRepository<HinhAnhSanPham, Long> {
    List<HinhAnhSanPham> findByIdSanPham(SanPham sanPham);

    void deleteByIdSanPham(SanPham sanPham);
}

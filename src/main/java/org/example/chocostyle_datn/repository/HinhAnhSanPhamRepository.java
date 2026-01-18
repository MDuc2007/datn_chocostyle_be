package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.HinhAnhSanPham;
import org.example.chocostyle_datn.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HinhAnhSanPhamRepository
        extends JpaRepository<HinhAnhSanPham, Integer> {

    List<HinhAnhSanPham> findByChiTietSanPham_Id(Integer idSpct);

    void deleteByChiTietSanPham_Id(Integer idSpct);
}


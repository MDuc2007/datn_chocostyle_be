package org.example.chocostyle_datn.repository;



import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietSanPhamRepository extends JpaRepository<ChiTietSanPham, Integer> {
    List<ChiTietSanPham> findByIdSanPham(SanPham sanPham);
    void deleteByIdSanPham(SanPham sanPham);
    @Query("select max(ctsp.maChiTietSanPham) from ChiTietSanPham ctsp")
    String findMaxMa();
    List<ChiTietSanPham> findByIdSanPham_Id(Integer idSanPham);
}

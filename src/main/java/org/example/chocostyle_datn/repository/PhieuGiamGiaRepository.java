package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia,Integer> {
    List<PhieuGiamGia> findByTrangThaiNot(Integer trangThai);

    Optional<PhieuGiamGia> findByMaPgg(String maPgg);

    @Query("SELECT p FROM PhieuGiamGia p ORDER BY p.id DESC")
    List<PhieuGiamGia> findAllOrderByIdDesc();

    @Query("""
        SELECT p.maPgg 
        FROM PhieuGiamGia p 
        ORDER BY p.id DESC
    """)
    List<String> findLastMaPgg();
}

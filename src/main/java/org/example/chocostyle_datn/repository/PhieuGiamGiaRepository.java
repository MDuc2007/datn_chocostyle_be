package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Integer> {
    // Tìm phiếu giảm giá theo mã code (VD: KM50K)
    Optional<PhieuGiamGia> findByMaPgg(String maPgg);
}
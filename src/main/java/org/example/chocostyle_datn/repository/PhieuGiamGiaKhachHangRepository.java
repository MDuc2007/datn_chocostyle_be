package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.PhieuGiamGiaKhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhieuGiamGiaKhachHangRepository extends JpaRepository<PhieuGiamGiaKhachHang, Integer> {
    List<PhieuGiamGiaKhachHang> findByPhieuGiamGiaId(Integer phieuGiamGiaId);
    Boolean existsByPhieuGiamGiaIdAndKhachHangIdAndDaSuDungFalse(
            Integer pggId,
            Integer khId
    );

    PhieuGiamGiaKhachHang findByPhieuGiamGiaIdAndKhachHangId(Integer id, Integer idKhachHang);
}
package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.ChiTietDotGiamGia;
import org.example.chocostyle_datn.entity.ChiTietDotGiamGiaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChiTietDotGiamGiaRepository extends JpaRepository<ChiTietDotGiamGia, ChiTietDotGiamGiaId> {
    List<ChiTietDotGiamGia> findById_IdDotGiamGia(Integer idDotGiamGia);
    void deleteById_IdDotGiamGia(Integer idDotGiamGia);
}

package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.KieuDang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KieuDangRepository extends JpaRepository<KieuDang,Integer> {
    @Query("select max(k.maKieuDang) from KieuDang k")
    String findMaxMa();
}

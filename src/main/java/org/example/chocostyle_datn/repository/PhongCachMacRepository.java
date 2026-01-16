package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.PhongCachMac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PhongCachMacRepository extends JpaRepository<PhongCachMac,Integer> {
    @Query("select max(p.maPhongCachMac) from PhongCachMac p")
    String findMaxMa();
}

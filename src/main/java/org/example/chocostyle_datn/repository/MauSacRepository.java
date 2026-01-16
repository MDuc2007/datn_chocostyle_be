package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.MauSac;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MauSacRepository extends JpaRepository<MauSac,Integer> {
    @Query("select max(m.maMauSac) from MauSac m")
    String findMaxMa();
}

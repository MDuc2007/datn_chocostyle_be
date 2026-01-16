package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.XuatXu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface XuatXuRepository extends JpaRepository<XuatXu,Integer> {
    @Query("select max(x.maXuatXu) from XuatXu x")
    String findMaxMa();
}

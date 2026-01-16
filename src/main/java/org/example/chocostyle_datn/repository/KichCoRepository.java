package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.KichCo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KichCoRepository extends JpaRepository<KichCo,Integer> {
    @Query("select max(k.maKichCo) from KichCo k")
    String findMaxMa();
}

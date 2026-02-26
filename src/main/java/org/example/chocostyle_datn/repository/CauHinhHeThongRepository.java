package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.CauHinhHeThong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CauHinhHeThongRepository extends JpaRepository<CauHinhHeThong, Integer> {
}
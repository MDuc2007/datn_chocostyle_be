package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.ChamCong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ChamCongRepository extends JpaRepository<ChamCong, Integer> {

    Optional<ChamCong> findByNhanVien_IdAndNgay(Integer idNv, LocalDate ngay);


}
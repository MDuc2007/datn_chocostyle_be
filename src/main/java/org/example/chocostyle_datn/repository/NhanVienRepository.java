package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NhanVienRepository extends JpaRepository<NhanVien,Integer> {
}

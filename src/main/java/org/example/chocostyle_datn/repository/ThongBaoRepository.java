package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.ThongBao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThongBaoRepository extends JpaRepository<ThongBao, Long> {

    List<ThongBao> findAllByOrderByNgayTaoDesc();

    Long countByDaDocFalse();

}
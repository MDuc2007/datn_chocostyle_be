package org.example.chocostyle_datn.repository;

import jakarta.persistence.LockModeType;
import org.example.chocostyle_datn.entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia,Integer> {
    List<PhieuGiamGia> findByTrangThaiNot(Integer trangThai);

    boolean existsByTenPggIgnoreCase(String tenPgg);

    boolean existsByTenPggIgnoreCaseAndIdNot(String tenPgg, Integer id);

    Optional<PhieuGiamGia> findByMaPgg(String maPgg);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.maPgg = :ma")
    Optional<PhieuGiamGia> findByMaPggForUpdate(@Param("ma") String ma);

    @Query("SELECT p FROM PhieuGiamGia p ORDER BY p.id DESC")
    List<PhieuGiamGia> findAllOrderByIdDesc();

    @Query("""
        SELECT p.maPgg 
        FROM PhieuGiamGia p 
        ORDER BY p.id DESC
    """)
    List<String> findLastMaPgg();

    Optional<PhieuGiamGia> findFirstByMaPggOrderByTrangThaiDesc(String maPgg);
}

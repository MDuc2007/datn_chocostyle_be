package org.example.chocostyle_datn.repository;


import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.model.Response.PosSanPhamResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


import java.util.List;


@Repository
public interface PosSanPhamRepository extends JpaRepository <ChiTietSanPham, Integer> {


    @Query("""
       SELECT new org.example.chocostyle_datn.model.Response.PosSanPhamResponse(
           ct.id,
           sp.maSp,
           sp.tenSp,
           sp.hinhAnh,
           ct.giaBan,
           ct.soLuongTon,
           ms.tenMauSac,
           kc.tenKichCo
       )
       FROM ChiTietSanPham ct
       JOIN ct.idSanPham sp
       JOIN ct.idMauSac ms
       JOIN ct.idKichCo kc
       WHERE ct.trangThai = 1
         AND sp.trangThai = 1
   """)
    List<PosSanPhamResponse> getSanPhamBanTaiQuay();
}


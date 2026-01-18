package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.repository.ChiTietSanPhamRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChiTietSanPhamService {

    private final ChiTietSanPhamRepository repo;

    public List<ChiTietSanPham> getAll() {
        return repo.findAll();
    }

    public List<ChiTietSanPham> getBySanPham(Integer idSanPham) {
        return repo.findByIdSanPham_Id(idSanPham);
    }

    public ChiTietSanPham create(ChiTietSanPham e) {
        String max = repo.findMaxMa();
        String ma = max == null ? "CTSP01"
                : "CTSP" + String.format("%02d", Integer.parseInt(max.substring(4)) + 1);
        e.setMaChiTietSanPham(ma);
        e.setNgayTao(LocalDate.now());
        return repo.save(e);
    }

    public ChiTietSanPham update(Integer id, ChiTietSanPham e) {
        ChiTietSanPham old = repo.findById(id).orElseThrow();
        e.setId(id);
        e.setMaChiTietSanPham(old.getMaChiTietSanPham());
        e.setNgayTao(old.getNgayTao());
        e.setNgayCapNhat(LocalDate.now());
        return repo.save(e);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }
}


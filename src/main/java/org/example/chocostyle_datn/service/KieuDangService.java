package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.KieuDang;
import org.example.chocostyle_datn.repository.KieuDangRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class KieuDangService {

    private final KieuDangRepository repo;

    public List<KieuDang> getAll() {
        return repo.findAll();
    }

    public KieuDang create(KieuDang e) {
        String max = repo.findMaxMa();
        String ma = max == null
                ? "KD01"
                : "KD" + String.format("%02d", Integer.parseInt(max.substring(2)) + 1);

        e.setMaKieuDang(ma);

        // audit
        e.setNgayTao(LocalDate.now());
        e.setNguoiTao(e.getNguoiTao());
        e.setNgayCapNhat(null);
        e.setNguoiCapNhat(null);

        return repo.save(e);
    }

    public KieuDang update(Integer id, KieuDang e) {
        KieuDang old = repo.findById(id).orElseThrow();

        e.setId(id);

        // giữ dữ liệu cũ
        e.setMaKieuDang(old.getMaKieuDang());
        e.setNgayTao(old.getNgayTao());
        e.setNguoiTao(old.getNguoiTao());

        // cập nhật
        e.setNgayCapNhat(LocalDate.now());
        e.setNguoiCapNhat(e.getNguoiCapNhat());

        return repo.save(e);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }
}

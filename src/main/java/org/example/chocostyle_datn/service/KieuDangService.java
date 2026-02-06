package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.Exception.DuplicateException;
import org.example.chocostyle_datn.entity.KieuDang;
import org.example.chocostyle_datn.repository.KieuDangRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static org.example.chocostyle_datn.util.TextNormalizeUtil.normalize;

@Service
@RequiredArgsConstructor
@Transactional
public class KieuDangService {

    private final KieuDangRepository repo;

    public List<KieuDang> getAll() {
        return repo.findAll();
    }

    public KieuDang create(KieuDang e) {
        if (repo.existsByTenIgnoreSpace(e.getTenKieuDang())) {
            throw new DuplicateException("Tên kiểu dáng đã tồn tại");
        }
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
        e.setTrangThai(1);

        return repo.save(e);
    }

    public KieuDang update(Integer id, KieuDang e) {
        KieuDang old = repo.findById(id).orElseThrow();

        boolean isDuplicate = repo.existsByTenIgnoreSpace(e.getTenKieuDang());

        String oldCompare = old.getTenKieuDang().replace(" ", "").toLowerCase();
        String newCompare = e.getTenKieuDang().replace(" ", "").toLowerCase();

        if (isDuplicate && !oldCompare.equals(newCompare)) {
            throw new DuplicateException("Tên kiểu dáng đã tồn tại");
        }

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

    public KieuDang doiTrangThai(Integer id, String nguoiCapNhat) {
        KieuDang mauSac = repo.findById(id).orElseThrow();

        // toggle trạng thái
        mauSac.setTrangThai(mauSac.getTrangThai() == 1 ? 0 : 1);

        mauSac.setNgayCapNhat(LocalDate.now());
        mauSac.setNguoiCapNhat(nguoiCapNhat);

        return repo.save(mauSac);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }
}

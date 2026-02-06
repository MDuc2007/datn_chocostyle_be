package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.Exception.DuplicateException;
import org.example.chocostyle_datn.entity.MauSac;
import org.example.chocostyle_datn.entity.PhongCachMac;
import org.example.chocostyle_datn.repository.PhongCachMacRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static org.example.chocostyle_datn.util.TextNormalizeUtil.normalize;

@Service
@RequiredArgsConstructor
@Transactional
public class PhongCachMacService {

    private final PhongCachMacRepository repo;

    public List<PhongCachMac> getAll() {
        return repo.findAll();
    }

    public PhongCachMac create(PhongCachMac e) {
        if (repo.existsByTenIgnoreSpace(e.getTenPhongCach())) {
            throw new DuplicateException("Phong cách mặc đã tồn tại");
        }
        String max = repo.findMaxMa();
        String ma = max == null
                ? "PCM01"
                : "PCM" + String.format("%02d", Integer.parseInt(max.substring(3)) + 1);

        e.setMaPhongCachMac(ma);

        // field mới
        e.setNgayTao(LocalDate.now());
        e.setNguoiTao(e.getNguoiTao()); // lấy từ request
        e.setNgayCapNhat(null);
        e.setNguoiCapNhat(null);
        e.setTrangThai(1);

        return repo.save(e);
    }

    public PhongCachMac update(Integer id, PhongCachMac e) {
        PhongCachMac old = repo.findById(id).orElseThrow();
        boolean isDuplicate = repo.existsByTenIgnoreSpace(e.getTenPhongCach());

        String oldCompare = old.getTenPhongCach().replace(" ", "").toLowerCase();
        String newCompare = e.getTenPhongCach().replace(" ", "").toLowerCase();

        if (isDuplicate && !oldCompare.equals(newCompare)) {
            throw new DuplicateException("Phong cách mặc đã tồn tại");
        }
        e.setId(id);

        // giữ nguyên dữ liệu cũ
        e.setMaPhongCachMac(old.getMaPhongCachMac());
        e.setNgayTao(old.getNgayTao());
        e.setNguoiTao(old.getNguoiTao());

        // cập nhật mới
        e.setNgayCapNhat(LocalDate.now());
        e.setNguoiCapNhat(e.getNguoiCapNhat()); // từ request

        return repo.save(e);
    }

    public PhongCachMac doiTrangThai(Integer id, String nguoiCapNhat) {
        PhongCachMac mauSac = repo.findById(id).orElseThrow();

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

package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.PhongCachMac;
import org.example.chocostyle_datn.repository.PhongCachMacRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PhongCachMacService {

    private final PhongCachMacRepository repo;

    public List<PhongCachMac> getAll() {
        return repo.findAll();
    }

    public PhongCachMac create(PhongCachMac e) {
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

        return repo.save(e);
    }

    public PhongCachMac update(Integer id, PhongCachMac e) {
        PhongCachMac old = repo.findById(id).orElseThrow();

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

    public void delete(Integer id) {
        repo.deleteById(id);
    }
}

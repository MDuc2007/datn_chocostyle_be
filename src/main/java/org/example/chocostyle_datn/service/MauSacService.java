package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.MauSac;
import org.example.chocostyle_datn.repository.MauSacRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MauSacService {

    private final MauSacRepository repo;

    public List<MauSac> getAll() {
        return repo.findAll();
    }

    public MauSac create(MauSac e) {
        e.setMaMauSac(genMa("MS", repo.findMaxMa()));

        // field mới
        e.setNgayTao(LocalDate.now());
        e.setNguoiTao(e.getNguoiTao()); // từ request
        e.setNgayCapNhat(null);
        e.setNguoiCapNhat(null);

        return repo.save(e);
    }

    public MauSac update(Integer id, MauSac e) {
        MauSac old = repo.findById(id).orElseThrow();

        e.setId(id);

        // giữ dữ liệu cũ
        e.setMaMauSac(old.getMaMauSac());
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

    private String genMa(String p, String max) {
        if (max == null) return p + "001";
        return p + String.format("%03d", Integer.parseInt(max.replace(p, "")) + 1);
    }
}


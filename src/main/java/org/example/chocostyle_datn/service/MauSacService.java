package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.Exception.DuplicateException;
import org.example.chocostyle_datn.entity.MauSac;
import org.example.chocostyle_datn.repository.MauSacRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static org.example.chocostyle_datn.util.TextNormalizeUtil.normalize;

@Service
@RequiredArgsConstructor
@Transactional
public class MauSacService {

    private final MauSacRepository repo;

    public List<MauSac> getAll() {
        return repo.findAll();
    }

    public MauSac create(MauSac e) {
        if (repo.existsByTenIgnoreSpace(e.getTenMauSac())) {
            throw new DuplicateException("Tên màu sắc đã tồn tại");
        }
        e.setMaMauSac(genMa("MS", repo.findMaxMa()));

        // field mới
        e.setNgayTao(LocalDate.now());
        e.setNguoiTao(e.getNguoiTao()); // từ request
        e.setNgayCapNhat(null);
        e.setNguoiCapNhat(null);
        e.setTrangThai(1);


        return repo.save(e);
    }

    public MauSac update(Integer id, MauSac e) {
        MauSac old = repo.findById(id).orElseThrow();
        boolean isDuplicate = repo.existsByTenIgnoreSpace(e.getTenMauSac());

        String oldCompare = old.getTenMauSac().replace(" ", "").toLowerCase();
        String newCompare = e.getTenMauSac().replace(" ", "").toLowerCase();

        if (isDuplicate && !oldCompare.equals(newCompare)) {
            throw new DuplicateException("Tên màu sắc đã tồn tại");
        }
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
    public MauSac doiTrangThai(Integer id, String nguoiCapNhat) {
        MauSac mauSac = repo.findById(id).orElseThrow();

        // toggle trạng thái
        mauSac.setTrangThai(mauSac.getTrangThai() == 1 ? 0 : 1);

        mauSac.setNgayCapNhat(LocalDate.now());
        mauSac.setNguoiCapNhat(nguoiCapNhat);

        return repo.save(mauSac);
    }


    public void delete(Integer id) {
        repo.deleteById(id);
    }

    private String genMa(String p, String max) {
        if (max == null) return p + "001";
        return p + String.format("%03d", Integer.parseInt(max.replace(p, "")) + 1);
    }
}


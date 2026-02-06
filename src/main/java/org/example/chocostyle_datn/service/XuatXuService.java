package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.Exception.DuplicateException;
import org.example.chocostyle_datn.entity.MauSac;
import org.example.chocostyle_datn.entity.XuatXu;
import org.example.chocostyle_datn.repository.XuatXuRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static org.example.chocostyle_datn.util.TextNormalizeUtil.normalize;

@Service
@RequiredArgsConstructor
@Transactional
public class XuatXuService {

    private final XuatXuRepository repo;

    public List<XuatXu> getAll() {
        return repo.findAll();
    }

    public XuatXu create(XuatXu e) {
        if (repo.existsByTenIgnoreSpace(e.getTenXuatXu())) {
            throw new DuplicateException("Xuất xứ đã tồn tại");
        }
        e.setMaXuatXu(genMa("XX", repo.findMaxMa()));

        // set field mới
        e.setNgayTao(LocalDate.now());
        e.setNguoiTao(e.getNguoiTao()); // lấy từ request
        e.setNgayCapNhat(null);
        e.setNguoiCapNhat(null);
        e.setTrangThai(1);

        return repo.save(e);
    }

    public XuatXu update(Integer id, XuatXu e) {
        XuatXu old = repo.findById(id).orElseThrow();
        boolean isDuplicate = repo.existsByTenIgnoreSpace(e.getTenXuatXu());

        String oldCompare = old.getTenXuatXu().replace(" ", "").toLowerCase();
        String newCompare = e.getTenXuatXu().replace(" ", "").toLowerCase();

        if (isDuplicate && !oldCompare.equals(newCompare)) {
            throw new DuplicateException("Xuất xứ đã tồn tại");
        }

        e.setId(id);

        // giữ nguyên dữ liệu cũ
        e.setMaXuatXu(old.getMaXuatXu());
        e.setNgayTao(old.getNgayTao());
        e.setNguoiTao(old.getNguoiTao());

        // cập nhật mới
        e.setNgayCapNhat(LocalDate.now());
        e.setNguoiCapNhat(e.getNguoiCapNhat()); // từ request

        return repo.save(e);
    }

    public XuatXu doiTrangThai(Integer id, String nguoiCapNhat) {
        XuatXu mauSac = repo.findById(id).orElseThrow();

        // toggle trạng thái
        mauSac.setTrangThai(mauSac.getTrangThai() == 1 ? 0 : 1);

        mauSac.setNgayCapNhat(LocalDate.now());
        mauSac.setNguoiCapNhat(nguoiCapNhat);

        return repo.save(mauSac);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }

    private String genMa(String prefix, String max) {
        if (max == null) return prefix + "01";
        int next = Integer.parseInt(max.replace(prefix, "")) + 1;
        return prefix + String.format("%02d", next);
    }
}



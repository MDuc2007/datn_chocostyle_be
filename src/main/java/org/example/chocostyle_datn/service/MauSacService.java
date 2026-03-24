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
        e.setMaMauSac(genMa("MS"));
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

        // Cập nhật các trường mới VÀO OBJECT CŨ
        old.setTenMauSac(e.getTenMauSac());
        old.setRgb(e.getRgb());
        old.setNgayCapNhat(LocalDate.now());
        old.setNguoiCapNhat(e.getNguoiCapNhat());

        // Lưu object cũ (lúc này đã chứa dữ liệu mới)
        return repo.save(old);
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

    // THAY THẾ HÀM GEN MÃ BẰNG HÀM NÀY
    private String genMa(String prefix) {
        // Lấy tất cả mã, lọc phần số và tìm số to nhất
        int max = repo.findAll().stream()
                .map(MauSac::getMaMauSac)
                .filter(ma -> ma != null && ma.startsWith(prefix))
                .map(ma -> ma.replaceAll("[^0-9]", "")) // Chỉ giữ lại số
                .filter(s -> !s.isEmpty())
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0); // Nếu chưa có gì thì là 0

        // Cộng 1 và format về 3 chữ số (VD: 3 -> MS003)
        return prefix + String.format("%03d", max + 1);
    }
}


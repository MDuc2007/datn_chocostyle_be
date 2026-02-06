package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.Exception.DuplicateException;
import org.example.chocostyle_datn.entity.LoaiAo;
import org.example.chocostyle_datn.repository.LoaiAoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static org.example.chocostyle_datn.util.TextNormalizeUtil.normalize;

@Service
@RequiredArgsConstructor
@Transactional
public class LoaiAoService {

    private final LoaiAoRepository repo;

    public List<LoaiAo> getAll() {
        return repo.findAll();
    }

    public LoaiAo create(LoaiAo e) {
        if (repo.existsByTenIgnoreSpace(e.getTenLoai())) {
            throw new DuplicateException("Tên loại áo đã tồn tại");
        }
        String max = repo.findMaxMa();
        String ma = max == null
                ? "LA01"
                : "LA" + String.format("%02d", Integer.parseInt(max.substring(2)) + 1);

        e.setMaLoai(ma);

        // field mới
        e.setNgayTao(LocalDate.now());
        e.setNguoiTao(e.getNguoiTao()); // từ request
        e.setNgayCapNhat(null);
        e.setNguoiCapNhat(null);
        e.setTrangThai(1);
        return repo.save(e);
    }

    public LoaiAo update(Integer id, LoaiAo e) {
        LoaiAo old = repo.findById(id).orElseThrow();
        boolean isDuplicate = repo.existsByTenIgnoreSpace(e.getTenLoai());

        String oldCompare = old.getTenLoai().replace(" ", "").toLowerCase();
        String newCompare = e.getTenLoai().replace(" ", "").toLowerCase();

        if (isDuplicate && !oldCompare.equals(newCompare)) {
            throw new DuplicateException("Tên loại áo đã tồn tại");
        }
        e.setId(id);

        // giữ dữ liệu cũ
        e.setMaLoai(old.getMaLoai());
        e.setNgayTao(old.getNgayTao());
        e.setNguoiTao(old.getNguoiTao());

        // cập nhật mới
        e.setNgayCapNhat(LocalDate.now());
        e.setNguoiCapNhat(e.getNguoiCapNhat()); // từ request

        return repo.save(e);
    }

    public LoaiAo doiTrangThai(Integer id, String nguoiCapNhat) {
        LoaiAo mauSac = repo.findById(id).orElseThrow();

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

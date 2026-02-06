package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.Exception.DuplicateException;
import org.example.chocostyle_datn.entity.KichCo;
import org.example.chocostyle_datn.repository.KichCoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static org.example.chocostyle_datn.util.TextNormalizeUtil.normalize;

@Service
@RequiredArgsConstructor
@Transactional
public class KichCoService {

    private final KichCoRepository repo;

    public List<KichCo> getAll() {
        return repo.findAll();
    }

    public KichCo create(KichCo e) {
        if (repo.existsByTenIgnoreSpace(e.getTenKichCo())) {
            throw new DuplicateException("Tên kích cỡ đã tồn tại");
        }
        e.setMaKichCo(genMa("KC", repo.findMaxMa()));

        // audit
        e.setNgayTao(LocalDate.now());
        e.setNguoiTao(e.getNguoiTao());
        e.setNgayCapNhat(null);
        e.setNguoiCapNhat(null);
        e.setTrangThai(1);

        return repo.save(e);
    }

    public KichCo update(Integer id, KichCo e) {
        KichCo old = repo.findById(id).orElseThrow();

        boolean isDuplicate = repo.existsByTenIgnoreSpace(e.getTenKichCo());

        String oldCompare = old.getTenKichCo().replace(" ", "").toLowerCase();
        String newCompare = e.getTenKichCo().replace(" ", "").toLowerCase();

        if (isDuplicate && !oldCompare.equals(newCompare)) {
            throw new DuplicateException("Tên kích cỡ đã tồn tại");
        }
        e.setId(id);

        // giữ dữ liệu cũ
        e.setMaKichCo(old.getMaKichCo());
        e.setNgayTao(old.getNgayTao());
        e.setNguoiTao(old.getNguoiTao());

        // cập nhật
        e.setNgayCapNhat(LocalDate.now());
        e.setNguoiCapNhat(e.getNguoiCapNhat());

        return repo.save(e);
    }

    public KichCo doiTrangThai(Integer id, String nguoiCapNhat) {
        KichCo mauSac = repo.findById(id).orElseThrow();

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
        if (max == null) return p + "01";
        return p + String.format("%02d", Integer.parseInt(max.replace(p, "")) + 1);
    }
}
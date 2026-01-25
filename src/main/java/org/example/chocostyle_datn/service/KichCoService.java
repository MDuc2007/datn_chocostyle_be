package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.KichCo;
import org.example.chocostyle_datn.repository.KichCoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class KichCoService {

    private final KichCoRepository repo;

    public List<KichCo> getAll() {
        return repo.findAll();
    }

    public KichCo create(KichCo e) {
        e.setMaKichCo(genMa("KC", repo.findMaxMa()));

        // audit
        e.setNgayTao(LocalDate.now());
        e.setNguoiTao(e.getNguoiTao());
        e.setNgayCapNhat(null);
        e.setNguoiCapNhat(null);

        return repo.save(e);
    }

    public KichCo update(Integer id, KichCo e) {
        KichCo old = repo.findById(id).orElseThrow();

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

    public void delete(Integer id) {
        repo.deleteById(id);
    }

    private String genMa(String p, String max) {
        if (max == null) return p + "01";
        return p + String.format("%02d", Integer.parseInt(max.replace(p, "")) + 1);
    }
}
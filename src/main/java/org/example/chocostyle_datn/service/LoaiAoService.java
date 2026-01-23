package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.LoaiAo;
import org.example.chocostyle_datn.repository.LoaiAoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class LoaiAoService {

    private final LoaiAoRepository repo;

    public List<LoaiAo> getAll() {
        return repo.findAll();
    }

    public LoaiAo create(LoaiAo e) {
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

        return repo.save(e);
    }

    public LoaiAo update(Integer id, LoaiAo e) {
        LoaiAo old = repo.findById(id).orElseThrow();

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

    public void delete(Integer id) {
        repo.deleteById(id);
    }
}

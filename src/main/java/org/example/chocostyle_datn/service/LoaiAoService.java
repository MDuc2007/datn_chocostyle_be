package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.LoaiAo;
import org.example.chocostyle_datn.repository.LoaiAoRepository;
import org.springframework.stereotype.Service;

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
        String ma = max == null ? "LA01"
                : "LA" + String.format("%02d", Integer.parseInt(max.substring(2)) + 1);
        e.setMaLoai(ma);
        return repo.save(e);
    }

    public LoaiAo update(Integer id, LoaiAo e) {
        LoaiAo old = repo.findById(id).orElseThrow();
        e.setId(id);
        e.setMaLoai(old.getMaLoai());
        return repo.save(e);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }
}

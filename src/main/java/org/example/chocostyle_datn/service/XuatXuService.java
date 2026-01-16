package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.XuatXu;
import org.example.chocostyle_datn.repository.XuatXuRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class XuatXuService {

    private final XuatXuRepository repo;

    public List<XuatXu> getAll() { return repo.findAll(); }

    public XuatXu create(XuatXu e) {
        e.setMaXuatXu(genMa("XX", repo.findMaxMa()));
        return repo.save(e);
    }

    public XuatXu update(Integer id, XuatXu e) {
        XuatXu old = repo.findById(id).orElseThrow();
        e.setId(id);
        e.setMaXuatXu(old.getMaXuatXu());
        return repo.save(e);
    }

    public void delete(Integer id) { repo.deleteById(id); }

    private String genMa(String p, String max) {
        if (max == null) return p + "01";
        return p + String.format("%02d", Integer.parseInt(max.replace(p, "")) + 1);
    }
}


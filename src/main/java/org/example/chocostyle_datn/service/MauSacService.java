package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.MauSac;
import org.example.chocostyle_datn.repository.MauSacRepository;
import org.springframework.stereotype.Service;

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
        return repo.save(e);
    }

    public MauSac update(Integer id, MauSac e) {
        MauSac old = repo.findById(id).orElseThrow();
        e.setId(id);
        e.setMaMauSac(old.getMaMauSac());
        return repo.save(e);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }

    private String genMa(String p, String max) {
        if (max == null) return p + "01";
        return p + String.format("%03d", Integer.parseInt(max.replace(p, "")) + 1);
    }
}


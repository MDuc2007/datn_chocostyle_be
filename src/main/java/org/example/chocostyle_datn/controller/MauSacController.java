package org.example.chocostyle_datn.controller;

import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.MauSac;
import org.example.chocostyle_datn.service.MauSacService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mau-sac")
@RequiredArgsConstructor
public class MauSacController {

    private final MauSacService service;

    @GetMapping
    public List<MauSac> getAll() { return service.getAll(); }

    @PostMapping
    public MauSac create(@RequestBody MauSac e) { return service.create(e); }

    @PutMapping("/{id}")
    public MauSac update(@PathVariable Integer id, @RequestBody MauSac e) {
        return service.update(id, e);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) { service.delete(id); }

    @PutMapping("/{id}/doi-trang-thai")
    public MauSac doiTrangThai(
            @PathVariable Integer id,
            @RequestParam String nguoiCapNhat
    ) {
        return service.doiTrangThai(id, nguoiCapNhat);
    }
}


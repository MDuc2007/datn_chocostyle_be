package org.example.chocostyle_datn.controller;

import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.KieuDang;
import org.example.chocostyle_datn.service.KieuDangService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kieu-dang")
@RequiredArgsConstructor
public class KieuDangController {

    private final KieuDangService service;

    @GetMapping
    public List<KieuDang> getAll() {
        return service.getAll();
    }

    @PostMapping
    public KieuDang create(@RequestBody KieuDang e) {
        return service.create(e);
    }

    @PutMapping("/{id}")
    public KieuDang update(@PathVariable Integer id, @RequestBody KieuDang e) {
        return service.update(id, e);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}


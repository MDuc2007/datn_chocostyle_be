package org.example.chocostyle_datn.controller;

import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.service.ChiTietSanPhamService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chi-tiet-san-pham")
@RequiredArgsConstructor
public class ChiTietSanPhamController {

    private final ChiTietSanPhamService service;

    @GetMapping
    public List<ChiTietSanPham> getAll() {
        return service.getAll();
    }

    @GetMapping("/san-pham/{id}")
    public List<ChiTietSanPham> getBySanPham(@PathVariable Integer id) {
        return service.getBySanPham(id);
    }

    @PostMapping
    public ChiTietSanPham create(@RequestBody ChiTietSanPham e) {
        return service.create(e);
    }

    @PutMapping("/{id}")
    public ChiTietSanPham update(@PathVariable Integer id, @RequestBody ChiTietSanPham e) {
        return service.update(id, e);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}

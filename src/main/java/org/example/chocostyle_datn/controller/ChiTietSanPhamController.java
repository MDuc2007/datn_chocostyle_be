package org.example.chocostyle_datn.controller;

import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.model.Request.ChiTietSanPhamRequest;
import org.example.chocostyle_datn.model.Response.ChiTietSanPhamResponse;
import org.example.chocostyle_datn.service.ChiTietSanPhamService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chi-tiet-san-pham")
@RequiredArgsConstructor
public class ChiTietSanPhamController {

    private final ChiTietSanPhamService service;

    /* ================= GET ================= */

    @GetMapping
    public List<ChiTietSanPhamResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public ChiTietSanPhamResponse getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    /* ================= CREATE ================= */
    // 👉 Nếu sau này muốn chuẩn hơn thì đổi sang Request DTO
    @PostMapping
    public ChiTietSanPhamResponse create(@RequestBody ChiTietSanPhamRequest chiTietSanPham) {
        return service.create(chiTietSanPham);
    }

    /* ================= UPDATE ================= */

    @PutMapping("/{id}")
    public ChiTietSanPhamResponse update(
            @PathVariable Integer id,
            @RequestBody ChiTietSanPhamRequest request
    ) {
        return service.update(id, request);
    }

    /* ================= DELETE ================= */

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

}

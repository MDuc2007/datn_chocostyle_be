package org.example.chocostyle_datn.controller;

import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.model.Request.ChiTietSanPhamRequest;
import org.example.chocostyle_datn.model.Response.ChiTietSanPhamResponse;
import org.example.chocostyle_datn.service.ChiTietSanPhamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chi-tiet-san-pham")
@RequiredArgsConstructor
public class ChiTietSanPhamController {

    private final ChiTietSanPhamService service;
    private final ChiTietSanPhamService chiTietSanPhamService;

    /* ================= GET ================= */

    @GetMapping("/filter")
    public ResponseEntity<Page<ChiTietSanPhamResponse>> filterCTSP(
            @RequestParam Long productId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long mauSacId,
            @RequestParam(required = false) Long kichCoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChiTietSanPhamResponse> result =
                chiTietSanPhamService.getChiTietSanPham(
                        productId,
                        keyword,
                        mauSacId,
                        kichCoId,
                        pageable
                );
        return ResponseEntity.ok(result);
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

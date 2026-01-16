package org.example.chocostyle_datn.controller;

import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.model.Request.SanPhamRequest;
import org.example.chocostyle_datn.model.Response.SanPhamResponse;
import org.example.chocostyle_datn.service.SanPhamService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
@CrossOrigin
public class SanPhamController {

    private final SanPhamService sanPhamService;

    @GetMapping
    public List<SanPhamResponse> getAll() {
        return sanPhamService.getAll();
    }

    @GetMapping("/{id}")
    public SanPhamResponse getById(@PathVariable Integer id) {
        return sanPhamService.getById(id);
    }

    @PostMapping
    public SanPhamResponse create(@RequestBody SanPhamRequest request) {
        return sanPhamService.create(request);
    }

    @PutMapping("/{id}")
    public SanPhamResponse update(
            @PathVariable Integer id,
            @RequestBody SanPhamRequest request
    ) {
        return sanPhamService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        sanPhamService.delete(id);
    }
}


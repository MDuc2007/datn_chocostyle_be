package org.example.chocostyle_datn.controller;

import org.example.chocostyle_datn.model.request.NhanVienRequest;
import org.example.chocostyle_datn.model.response.NhanVienResponse;
import org.example.chocostyle_datn.service.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/nhan-vien")
@CrossOrigin //
public class NhanVienController {
    @Autowired
    private NhanVienService service;

    @GetMapping
    public List<NhanVienResponse> getAll() {
        return service.getAllNhanVien();
    }
    @PostMapping
    public ResponseEntity<NhanVienResponse> create(@RequestBody NhanVienRequest request) {
        NhanVienResponse createdNv = service.createNhanVien(request);
        return ResponseEntity.ok(createdNv);
    }
    @PutMapping("/{id}")
    public ResponseEntity<NhanVienResponse> update(@PathVariable Integer id, @RequestBody NhanVienRequest request) {
        NhanVienResponse updatedNv = service.updateNhanVien(id, request);
        return ResponseEntity.ok(updatedNv);
    }
}

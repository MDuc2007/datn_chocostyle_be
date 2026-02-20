package org.example.chocostyle_datn.controller;

import org.example.chocostyle_datn.entity.CaLamViec;
import org.example.chocostyle_datn.service.CaLamViecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/ca-lam-viec")
@CrossOrigin("*") // Cho phép gọi từ Vue.js
public class CaLamViecController {

    @Autowired
    private CaLamViecService service;

    // QUAN TRỌNG: Đặt API /search lên TRƯỚC API /{id} để tránh bị nhận diện nhầm
    @GetMapping("/search")
    public ResponseEntity<Page<CaLamViec>> search(
            @RequestParam(required = false) Integer trangThai,
            // Đổi iso thành pattern = "HH:mm:ss"
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm:ss") LocalTime gioBatDau,
            @RequestParam(required = false) @DateTimeFormat(pattern = "HH:mm:ss") LocalTime gioKetThuc,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {

        return ResponseEntity.ok(service.searchCaLamViec(trangThai, gioBatDau, gioKetThuc, page, size));
    }

    // Lấy tất cả danh sách ca
    @GetMapping
    public ResponseEntity<List<CaLamViec>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // Lấy chi tiết 1 ca
    @GetMapping("/{id}")
    public ResponseEntity<CaLamViec> getDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // Thêm mới ca
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CaLamViec ca) {
        try {
            return ResponseEntity.ok(service.create(ca));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Cập nhật ca
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody CaLamViec ca) {
        try {
            return ResponseEntity.ok(service.update(id, ca));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Xóa ca
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            service.delete(id);
            return ResponseEntity.ok("Xóa thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
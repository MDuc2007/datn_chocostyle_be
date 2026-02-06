package org.example.chocostyle_datn.controller;


import org.example.chocostyle_datn.entity.CaLamViec;
import org.example.chocostyle_datn.service.CaLamViecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequestMapping("/api/ca-lam-viec")
@CrossOrigin("*") // Cho phép gọi từ Vue.js
public class CaLamViecController {


    @Autowired
    private CaLamViecService service;


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


    // Xóa (Đổi trạng thái về 0)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}


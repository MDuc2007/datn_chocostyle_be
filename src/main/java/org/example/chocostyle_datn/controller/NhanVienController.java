package org.example.chocostyle_datn.controller;


import org.example.chocostyle_datn.model.Request.NhanVienRequest;
import org.example.chocostyle_datn.model.Response.NhanVienResponse;
import org.example.chocostyle_datn.service.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequestMapping("/api/nhan-vien")
public class NhanVienController {
    @Autowired
    private NhanVienService service;


    @GetMapping
    public List<NhanVienResponse> getAll() {
        return service.getAllNhanVien();
    }
    // 2. Lấy chi tiết (Dùng cho trang Edit)
    @GetMapping("/{id}")
    public ResponseEntity<NhanVienResponse> getDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(service.getNhanVienById(id));
    }
    @PostMapping
    public ResponseEntity<?> create(@RequestBody NhanVienRequest request) {
        try {
            NhanVienResponse createdNv = service.createNhanVien(request);
            return ResponseEntity.ok(createdNv);
        } catch (RuntimeException e) {
            // Trả về lỗi 400 Bad Request kèm message cho Frontend hiển thị Toast
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody NhanVienRequest request) {
        try {
            NhanVienResponse updatedNv = service.updateNhanVien(id, request);
            return ResponseEntity.ok(updatedNv);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    // Class wrapper để trả về JSON đẹp: { "message": "Email đã tồn tại" }
    static class ErrorResponse {
        public String message;
        public ErrorResponse(String message) { this.message = message; }
    }
}


package org.example.chocostyle_datn.controller;

import jakarta.validation.Valid;
import org.example.chocostyle_datn.model.Request.KhachHangRequest;
import org.example.chocostyle_datn.model.Response.KhachHangDetailResponse;
import org.example.chocostyle_datn.model.Response.KhachHangResponse;
import org.example.chocostyle_datn.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/khach-hang")
@CrossOrigin(origins = "*", allowedHeaders = "*") //
public class KhachHangController {

    @Autowired
    private KhachHangService khachHangService;

    // 1. LẤY DANH SÁCH + PHÂN TRANG
    @GetMapping
    public ResponseEntity<Page<KhachHangResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(khachHangService.getKhachHangs(keyword, status, pageRequest));
    }

    // 2. CHI TIẾT KHÁCH HÀNG (Trả về đầy đủ các trường ngày tạo, cập nhật,...)
    @GetMapping("/{id}")
    public ResponseEntity<KhachHangDetailResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(khachHangService.getDetailById(id));
    }

    // 3. THÊM MỚI (Xử lý Multipart cho ảnh đại diện)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> add(
            @Valid @RequestPart("data") KhachHangRequest req,
            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile,
            BindingResult result
    ) {
        if (result.hasErrors()) return handleValidationErrors(result);
        try {
            khachHangService.addKhachHang(req, avatarFile);
            return ResponseEntity.status(HttpStatus.CREATED).body("Thêm khách hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. CẬP NHẬT THÔNG TIN (Sử dụng hàm updateKhachHang mới)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestPart("data") KhachHangRequest req,
            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile,
            BindingResult result
    ) {
        if (result.hasErrors()) return handleValidationErrors(result);
        try {
            khachHangService.updateKhachHang(id, req, avatarFile);
            return ResponseEntity.ok("Cập nhật khách hàng thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. ĐỔI TRẠNG THÁI (Đã sửa lỗi 415 bằng cách tách riêng API)
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        try {
            khachHangService.toggleStatus(id);
            return ResponseEntity.ok("Đã cập nhật trạng thái thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // 6. THỐNG KÊ (Dùng cho stats box trên giao diện)
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> res = new HashMap<>();
        res.put("totalCustomers", khachHangService.getTotalKhachHang());
        return ResponseEntity.ok(res);
    }

    private ResponseEntity<Map<String, String>> handleValidationErrors(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError e : result.getFieldErrors()) {
            errors.put(e.getField(), e.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(errors);
    }
}
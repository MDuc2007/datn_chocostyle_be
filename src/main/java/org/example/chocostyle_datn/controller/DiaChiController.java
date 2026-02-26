package org.example.chocostyle_datn.controller;

import org.example.chocostyle_datn.entity.DiaChi;
import org.example.chocostyle_datn.model.Request.DiaChiRequest;
import org.example.chocostyle_datn.service.DiaChiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dia-chi")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class DiaChiController {

    @Autowired
    private DiaChiService diaChiService;

    // 1. LẤY DANH SÁCH ĐỊA CHỈ THEO ID KHÁCH HÀNG
    @GetMapping("/khach-hang/{khachHangId}")
    public ResponseEntity<List<DiaChi>> getDiaChiByKhachHang(@PathVariable Integer khachHangId) {
        try {
            List<DiaChi> list = diaChiService.findByKhachHangId(khachHangId);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 2. THÊM NHANH ĐỊA CHỈ MỚI
    @PostMapping
    public ResponseEntity<?> addDiaChiNhanh(@RequestBody DiaChiRequest req) {
        try {
            DiaChi saved = diaChiService.addDiaChi(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi thêm địa chỉ: " + e.getMessage());
        }
    }

    // 3. ĐẶT ĐỊA CHỈ LÀM MẶC ĐỊNH (MỚI THÊM)
    @PutMapping("/{id}/mac-dinh")
    public ResponseEntity<?> setDefaultAddress(@PathVariable Integer id, @RequestParam Integer khachHangId) {
        try {
            // Cần viết thêm hàm setDefaultAddress trong DiaChiService
            diaChiService.setDefaultAddress(id, khachHangId);
            return ResponseEntity.ok("Đặt địa chỉ mặc định thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi cập nhật địa chỉ mặc định: " + e.getMessage());
        }
    }
}
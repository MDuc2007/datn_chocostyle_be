package org.example.chocostyle_datn.controller;


import org.example.chocostyle_datn.model.Request.NhanVienRequest;
import org.example.chocostyle_datn.model.Response.NhanVienResponse;
import org.example.chocostyle_datn.service.NhanVienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    @GetMapping("/search")
    public ResponseEntity<Page<NhanVienResponse>> search(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {

        Page<NhanVienResponse> result = service.searchNhanVien(keyword, trangThai, page, size);
        return ResponseEntity.ok(result);
    }

    // ==========================================
    // API NHẬN FILE TỪ VUE ĐỂ CẬP NHẬT AVATAR
    // ==========================================
    @PostMapping("/{id}/avatar")
    public ResponseEntity<?> updateAvatar(
            @PathVariable Integer id,
            @RequestParam("avatarFile") org.springframework.web.multipart.MultipartFile file) {
        try {
            // Kiểm tra xem có gửi file lên không
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Vui lòng chọn ảnh."));
            }

            // Kiểm tra dung lượng (Ví dụ giới hạn 5MB = 5 * 1024 * 1024 bytes)
            if (file.getSize() > 5242880) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Dung lượng ảnh không được vượt quá 5MB."));
            }

            // Gọi Service để xử lý và lưu
            String newAvatarBase64 = service.updateAvatar(id, file);

            // Trả về JSON chứa ảnh mới cho Vue: { "message": "Thành công", "avatar": "data:image/..." }
            java.util.Map<String, String> response = new java.util.HashMap<>();
            response.put("message", "Cập nhật ảnh thành công");
            response.put("avatar", newAvatarBase64);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}


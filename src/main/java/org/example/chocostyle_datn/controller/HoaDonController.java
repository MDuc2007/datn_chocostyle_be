package org.example.chocostyle_datn.controller;


import org.example.chocostyle_datn.model.Request.SearchHoaDonRequest;
import org.example.chocostyle_datn.model.Request.UpdateTrangThaiRequest;
import org.example.chocostyle_datn.model.Response.HoaDonDetailResponse;
import org.example.chocostyle_datn.model.Response.HoaDonResponse;
import org.example.chocostyle_datn.service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hoa-don")
//@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    /**
     * API 1: Lấy danh sách hóa đơn (Có lọc + Phân trang)
     * URL: GET /api/hoa-don?page=0&size=10&keyword=HD01&trangThai=1
     */
    @GetMapping
    public ResponseEntity<Page<HoaDonResponse>> getAll(
            // Spring tự động map các tham số trên URL (?keyword=...&trangThai=...) vào object này
            SearchHoaDonRequest searchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        // Tạo đối tượng Pageable (Mặc định sắp xếp ngày tạo mới nhất nếu Repository chưa sort)
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(hoaDonService.getAll(searchRequest, pageable));
    }

    /**
     * API 2: Xem chi tiết hóa đơn
     * URL: GET /api/hoa-don/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<HoaDonDetailResponse> getDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(hoaDonService.getDetail(id));
    }

    /**
     * API 3: Cập nhật trạng thái hóa đơn (Xác nhận, Giao hàng, Hủy...)
     * URL: PUT /api/hoa-don/{id}/trang-thai
     * Body (JSON): { "trangThaiMoi": 2, "ghiChu": "Giao cho shipper" }
     */
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer id,
            @RequestBody UpdateTrangThaiRequest request
    ) {
        try {
            hoaDonService.updateStatus(id, request);
            return ResponseEntity.ok("Cập nhật trạng thái thành công!");
        } catch (RuntimeException e) {
            // Trả về lỗi 400 nếu có ngoại lệ (Ví dụ: Hóa đơn đã hủy mà cố tình update)
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * API 4: TẠO HÓA ĐƠN MỚI (Dùng cho cả Bán hàng tại quầy & Online)
     * URL: POST /api/hoa-don
     * Body: JSON chứa thông tin khách, nhân viên, list sản phẩm
     */
    @PostMapping // Không cần thêm path phụ, gọi thẳng vào /api/hoa-don
    public ResponseEntity<?> taoHoaDon(@RequestBody org.example.chocostyle_datn.model.Request.CreateOrderRequest request) {
        try {
            Integer idHoaDon = hoaDonService.taoHoaDonMoi(request);
            return ResponseEntity.status(201).body("Tạo đơn thành công. ID: " + idHoaDon);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}

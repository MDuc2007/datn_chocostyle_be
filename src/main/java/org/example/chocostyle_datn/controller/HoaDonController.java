package org.example.chocostyle_datn.controller;


import org.example.chocostyle_datn.entity.HoaDon;
import org.example.chocostyle_datn.model.Request.CreateOrderRequest;
import org.example.chocostyle_datn.model.Request.RefundRequest;
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
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class HoaDonController {


    @Autowired
    private HoaDonService hoaDonService;


    // API 1: Lấy danh sách
    @GetMapping
    public ResponseEntity<Page<HoaDonResponse>> getAll(
            SearchHoaDonRequest searchRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(hoaDonService.getAll(searchRequest, pageable));
    }


    // API 2: Xem chi tiết
    @GetMapping("/{id}")
    public ResponseEntity<HoaDonDetailResponse> getDetail(@PathVariable Integer id) {
        return ResponseEntity.ok(hoaDonService.getDetail(id));
    }


    // API 3: Cập nhật trạng thái
    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer id,
            @RequestBody UpdateTrangThaiRequest request
    ) {
        try {
            hoaDonService.updateStatus(id, request);
            return ResponseEntity.ok("Cập nhật trạng thái thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // API 4: Tạo hóa đơn mới (Dành cho Online)
    @PostMapping
    public ResponseEntity<?> taoHoaDon(@RequestBody CreateOrderRequest request) {
        try {
            Integer idHoaDon = hoaDonService.taoHoaDonMoi(request);
            return ResponseEntity.status(201).body("Tạo đơn thành công. ID: " + idHoaDon);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }


    // API 5: Xác nhận hoàn tiền
    @PostMapping("/hoan-tien")
    public ResponseEntity<?> xacNhanHoanTien(@RequestBody RefundRequest request) {
        try {
            hoaDonService.hoanTien(request);
            return ResponseEntity.ok("Hoàn tiền thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }


//    // API 6: Tạo tab hóa đơn rỗng (Bán hàng tại quầy)
//    @PostMapping("/tai-quay/tao-moi")
//    public ResponseEntity<?> taoDonChoTaiQuay(@RequestParam Integer idNhanVien) {
//        try {
//            HoaDon hdMoi = hoaDonService.taoHoaDonChoTaiQuay(idNhanVien);
//            // Trả về thẳng object để Frontend lấy id và maHoaDon gán lên UI
//            return ResponseEntity.status(201).body(hdMoi);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
//        }
//    }


    // API 7: Cập nhật hóa đơn nháp (Xác nhận đặt hàng tại quầy)
    @PutMapping("/tai-quay/xac-nhan/{id}")
    public ResponseEntity<?> xacNhanDatHangTaiQuay(
            @PathVariable Integer id,
            @RequestBody org.example.chocostyle_datn.model.Request.CreateOrderRequest request) {
        try {
            hoaDonService.xacNhanDatHangTaiQuay(id, request);
            return ResponseEntity.ok("Xác nhận đơn hàng tại quầy thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }


    // API 8: Xóa đơn nháp tại quầy (Khi nhân viên đóng Tab)
    @DeleteMapping("/xoa-don-quay/{id}")
    public ResponseEntity<?> xoaDonQuay(@PathVariable Integer id) {
        try {
            hoaDonService.xoaDonQuay(id);
            return ResponseEntity.ok("Đã xóa hóa đơn nháp thành công!");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }
    @PostMapping("/tai-quay/tao-moi")
    public ResponseEntity<?> taoDonChoTaiQuay() {
        HoaDon hdMoi = hoaDonService.taoHoaDonChoTaiQuay();
        return ResponseEntity.status(201).body(hdMoi);
    }
}




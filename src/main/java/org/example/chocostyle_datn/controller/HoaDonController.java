package org.example.chocostyle_datn.controller;

import org.example.chocostyle_datn.entity.HoaDon;
import org.example.chocostyle_datn.entity.HoaDonChiTiet;
import org.example.chocostyle_datn.entity.KhachHang;
import org.example.chocostyle_datn.entity.ThanhToan;
import org.example.chocostyle_datn.model.Request.CreateOrderRequest;
import org.example.chocostyle_datn.model.Request.RefundRequest;
import org.example.chocostyle_datn.model.Request.SearchHoaDonRequest;
import org.example.chocostyle_datn.model.Request.UpdateTrangThaiRequest;
import org.example.chocostyle_datn.model.Response.HoaDonDetailResponse;
import org.example.chocostyle_datn.model.Response.HoaDonResponse;
import org.example.chocostyle_datn.model.Response.SanPhamTraCuuDto;
import org.example.chocostyle_datn.repository.*;
import org.example.chocostyle_datn.service.HoaDonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hoa-don")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private HoaDonRepository hoaDonRepository;
    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private ThanhToanRepository thanhToanRepository;
    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;

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
            HoaDon hoaDon = hoaDonRepository.findById(idHoaDon).orElseThrow();
            Map<String, Object> noti = new HashMap<>();
            noti.put("title", "Đơn hàng mới");
            System.out.println("SEND NOTIFICATION: " + hoaDon.getMaHoaDon());
            noti.put("content", "Có đơn #" + hoaDon.getMaHoaDon());
            noti.put("orderId", idHoaDon);
            messagingTemplate.convertAndSend("/topic/notification", (Object) noti);
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

    @PutMapping("/tam-thoi-ton-kho")
    public ResponseEntity<?> capNhatTonKhoTam(@RequestParam Integer idSpct,
                                              @RequestParam Integer soLuongThayDoi) {
        hoaDonService.capNhatSoLuongTamThoi(idSpct, soLuongThayDoi);
        return ResponseEntity.ok("OK");
    }

    // ==========================================
    // TỪ NHÁNH HEAD: THÊM / XÓA SẢN PHẨM VÀO GIỎ
    // ==========================================

    // API 1: Khi nhân viên chọn thêm SP vào giỏ (Thêm SP A, SP B...)
    @PostMapping("/{idHoaDon}/them-sp-vao-gio")
    public ResponseEntity<?> themSanPhamVaoGio(
            @PathVariable Integer idHoaDon,
            @RequestParam Integer idSpct,
            @RequestParam Integer soLuong) {

        hoaDonService.themSanPhamVaoDonNhap(idHoaDon, idSpct, soLuong);
        return ResponseEntity.ok("Đã thêm sản phẩm vào giỏ và ghi lịch sử!");
    }

    // API 2: Khi nhân viên đổi ý, ấn nút xóa SP A khỏi giỏ
    @DeleteMapping("/{idHoaDon}/xoa-sp-khoi-gio/{idSpct}")
    public ResponseEntity<?> xoaSanPhamKhoiGio(
            @PathVariable Integer idHoaDon,
            @PathVariable Integer idSpct) {

        hoaDonService.xoaSanPhamKhoiDonNhap(idHoaDon, idSpct);
        return ResponseEntity.ok("Đã bỏ sản phẩm khỏi giỏ và ghi lịch sử!");
    }

    // ==========================================
    // TỪ NHÁNH HungDepZai: TRA CỨU ĐƠN HÀNG
    // ==========================================

    // API 9: Tra cứu đơn hàng cho khách (KHÔNG CẦN LOGIN)
    @GetMapping("/tra-cuu")
    public ResponseEntity<?> traCuuDonHang(
            @RequestParam String maDonHang,
            @RequestParam(required = false) String sdt) { // Thêm tham số sdt, để required = false để linh hoạt
        try {
            // Truyền cả maDonHang và sdt vào Service
            TraCuuDonHangResponse response = hoaDonService.traCuuDonHang(maDonHang, sdt);

            // Trả về object JSON cho Frontend
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Trả về lỗi 400 kèm thông báo cụ thể (ví dụ: "Số điện thoại không khớp")
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Lỗi hệ thống khác
            return ResponseEntity.internalServerError().body("Lỗi hệ thống: " + e.getMessage());
        }
    }


    @GetMapping("/my-orders")
    public ResponseEntity<?> getMyOrders() {
        try {
            return ResponseEntity.ok(hoaDonService.getMyOrders());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi tải lịch sử đơn hàng: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/thong-tin-nhan-hang")
    public ResponseEntity<?> capNhatThongTinNhanHang(
            @PathVariable Integer id,
            @RequestParam String ten,
            @RequestParam String sdt,
            @RequestParam String diaChi) {
        try {
            hoaDonService.capNhatThongTinGiaoHang(id, ten, sdt, diaChi);
            return ResponseEntity.ok("Cập nhật thông tin nhận hàng thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/so-luong-san-pham")
    public ResponseEntity<?> capNhatSoLuongSanPham(
            @PathVariable Integer id,
            @RequestParam Integer idSpct,
            @RequestParam int soLuongMoi,
            @RequestParam(required = false) BigDecimal donGia) { // Thêm donGia
        try {
            hoaDonService.capNhatSoLuongChiTiet(id, idSpct, soLuongMoi, donGia); // Truyền donGia vào service
            return ResponseEntity.ok("Cập nhật số lượng thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Trong HoaDonController.java
    @PutMapping("/{id}/thay-doi-gia")
    public ResponseEntity<?> thayDoiGiaSanPham(
            @PathVariable Integer id,
            @RequestParam Integer idSpct,
            @RequestParam BigDecimal giaMoi) {
        try {
            hoaDonService.thayDoiGiaChiTiet(id, idSpct, giaMoi);
            return ResponseEntity.ok("Cập nhật giá thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ==========================================
    // API TIẾP SÓNG REALTIME (VUE -> SPRING BOOT -> FLUTTER)
    // ==========================================
    @PostMapping("/sync-realtime/{id}")
    public ResponseEntity<?> syncCartToMobile(
            @PathVariable Integer id,
            @RequestBody Object cartData) { // Hứng nguyên cục JSON từ Web Vue
        try {
            // Cầm cục JSON đó, ném thẳng sang kênh WebSocket của Flutter
            messagingTemplate.convertAndSend("/topic/order/active", cartData);
            return ResponseEntity.ok("Đã đồng bộ realtime sang App thành công!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi đồng bộ: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/thanh-toan-thu-cong")
    public ResponseEntity<?> thanhToanThuCong(@PathVariable Integer id, @RequestBody java.util.Map<String, String> body) {
        try {
            // hoaDonService đã có sẵn trong HoaDonController
            hoaDonService.xacNhanThanhToanThuCong(id, body.get("ghiChu"));
            return ResponseEntity.ok("Xác nhận thanh toán thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
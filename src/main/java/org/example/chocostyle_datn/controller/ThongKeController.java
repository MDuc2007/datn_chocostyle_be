package org.example.chocostyle_datn.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.example.chocostyle_datn.entity.CauHinhHeThong;
import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.model.Request.ThongKeRequest;
import org.example.chocostyle_datn.model.Response.*;
import org.example.chocostyle_datn.repository.CauHinhHeThongRepository;
import org.example.chocostyle_datn.repository.ThongKeRepository;
import org.example.chocostyle_datn.service.EmailServiceThongKe;
import org.example.chocostyle_datn.service.ThongKeExcelService;
import org.example.chocostyle_datn.service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/thong-ke")
@CrossOrigin("*")
public class ThongKeController {

    @Autowired
    private ThongKeService thongKeService;

    @Autowired
    private ThongKeExcelService thongKeExcelService;

    @Autowired
    private CauHinhHeThongRepository cauHinhRepo;

    @Autowired
    private ThongKeRepository thongKeRepo;

    @Autowired
    private EmailServiceThongKe emailService;

    @GetMapping("/tong-quat")
    public ResponseEntity<?> getTongQuat() {
        return ResponseEntity.ok(thongKeService.getDuLieuTongQuan());
    }

    // 🔥 FIX TRUYỀN THAM SỐ: Nhận String để tránh tuyệt đối lỗi 400 Bad Request
    @GetMapping("/doanh-thu")
    public ResponseEntity<List<DoanhThuResponse>> getDoanhThu(
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr) {

        ThongKeRequest request = new ThongKeRequest();
        try {
            if (startDateStr != null && !startDateStr.trim().isEmpty() && !startDateStr.equals("null")) {
                request.setStartDate(LocalDate.parse(startDateStr));
            }
            if (endDateStr != null && !endDateStr.trim().isEmpty() && !endDateStr.equals("null")) {
                request.setEndDate(LocalDate.parse(endDateStr));
            }
        } catch (Exception e) {
            // Nếu Frontend gửi sai ngày, bỏ qua để lấy mặc định
        }
        return ResponseEntity.ok(thongKeService.getDoanhThuChart(request));
    }

    // 🔥 FIX TRUYỀN THAM SỐ: Tự parse ngày tháng
    @GetMapping("/ban-chay")
    public ResponseEntity<List<SanPhamBanChayResponse>> getBanChay(
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr) {

        LocalDate startDate = null;
        LocalDate endDate = null;

        try {
            if (startDateStr != null && !startDateStr.trim().isEmpty() && !startDateStr.equals("null")) {
                startDate = LocalDate.parse(startDateStr);
            }
            if (endDateStr != null && !endDateStr.trim().isEmpty() && !endDateStr.equals("null")) {
                endDate = LocalDate.parse(endDateStr);
            }
        } catch (Exception e) {}

        return ResponseEntity.ok(thongKeService.getTopBanChay(startDate, endDate));
    }

    // 🔥 FIX TRUYỀN THAM SỐ: Chặn lỗi gửi chuỗi "null" hoặc rỗng
    @GetMapping("/ban-chay-theo-dot")
    public ResponseEntity<?> getBanChayTheoDotGiamGia(
            @RequestParam(value = "idDotGiamGia", required = false) String idStr) {

        if (idStr == null || idStr.trim().isEmpty() || idStr.equals("null")) {
            return ResponseEntity.ok(List.of()); // Trả về mảng rỗng thay vì báo lỗi
        }
        try {
            Integer idDotGiamGia = Integer.parseInt(idStr);
            return ResponseEntity.ok(thongKeService.getTopBanChayTheoDotGiamGia(idDotGiamGia));
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }

    @GetMapping("/trang-thai")
    public ResponseEntity<List<TrangThaiDonResponse>> getTrangThai() {
        return ResponseEntity.ok(thongKeService.getPhanBoTrangThai());
    }

    @GetMapping("/sap-het")
    public ResponseEntity<List<ChiTietSanPham>> getSapHet() {
        return ResponseEntity.ok(thongKeService.getSanPhamSapHet());
    }

    @GetMapping("/loai-don")
    public ResponseEntity<?> getLoaiDon() {
        return ResponseEntity.ok(thongKeService.getPhanBoLoaiDon());
    }

    @GetMapping("/chi-tiet-thoi-gian")
    public ResponseEntity<List<ThongKeChiTietResponse>> getChiTietThoiGian() {
        return ResponseEntity.ok(thongKeService.getBangThongKeChiTiet());
    }

    // 🔥 FIX TRUYỀN THAM SỐ: Xuất báo cáo an toàn
    @GetMapping("/export")
    public void exportDoanhThu(
            HttpServletResponse response,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr) throws IOException {

        LocalDate finalStart = null;
        LocalDate finalEnd = null;

        try {
            if (startDateStr != null && !startDateStr.trim().isEmpty() && !startDateStr.equals("null")) {
                finalStart = LocalDate.parse(startDateStr);
            }
            if (endDateStr != null && !endDateStr.trim().isEmpty() && !endDateStr.equals("null")) {
                finalEnd = LocalDate.parse(endDateStr);
            }
        } catch (Exception e) {}

        if (finalStart == null || finalEnd == null) {
            LocalDate now = LocalDate.now();
            finalStart = now.with(TemporalAdjusters.firstDayOfMonth());
            finalEnd = now.with(TemporalAdjusters.lastDayOfMonth());
        }

        List<HoaDonExportResponse> dataList = thongKeRepo.getDanhSachHoaDonExport(finalStart, finalEnd);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=BaoCaoChiTietDoanhThu_" + finalStart + "_to_" + finalEnd + ".xlsx";
        response.setHeader(headerKey, headerValue);

        thongKeExcelService.exportDoanhThu(dataList, finalStart, finalEnd, response.getOutputStream());
    }

    @GetMapping("/cau-hinh-email")
    public ResponseEntity<?> getCauHinhEmail() {
        return ResponseEntity.ok(cauHinhRepo.findById(1).orElse(null));
    }

    @PostMapping("/cau-hinh-email")
    public ResponseEntity<?> saveCauHinhEmail(@RequestBody CauHinhHeThong request) {
        CauHinhHeThong config = cauHinhRepo.findById(1).orElse(null);
        if (config != null) {
            config.setEmailNhan(request.getEmailNhan());
            config.setGuiNgay(request.getGuiNgay());
            config.setGuiTuan(request.getGuiTuan());
            config.setGuiThang(request.getGuiThang());
            config.setGuiNam(request.getGuiNam());
            cauHinhRepo.save(config);
            return ResponseEntity.ok("Cập nhật cấu hình email thành công!");
        }
        return ResponseEntity.badRequest().body("Lỗi: Không tìm thấy dòng cấu hình trong DB!");
    }

    @PostMapping("/test-email")
    public ResponseEntity<?> testEmail() {
        CauHinhHeThong config = cauHinhRepo.findById(1).orElse(null);
        String emailHeThong = "hethong.chocostyle@gmail.com";

        if (config == null) {
            return ResponseEntity.badRequest().body("❌ Lỗi: Chưa có cấu hình hệ thống!");
        }

        Map<String, TongQuatResponse> duLieu = thongKeService.getDuLieuTongQuan();
        boolean daGui = false;

        if (Boolean.TRUE.equals(config.getGuiNgay())) {
            emailService.guiMailHtml(emailHeThong, "🧪 [TEST] Báo Cáo Doanh Thu Ngày - ChocoStyle", duLieu.get("homNay"), "hôm nay");
            daGui = true;
        }

        if (Boolean.TRUE.equals(config.getGuiTuan())) {
            emailService.guiMailHtml(emailHeThong, "🧪 [TEST] Báo Cáo Doanh Thu Tuần - ChocoStyle", duLieu.get("tuanNay"), "tuần này");
            daGui = true;
        }

        if (Boolean.TRUE.equals(config.getGuiThang())) {
            emailService.guiMailHtml(emailHeThong, "🧪 [TEST] Báo Cáo Doanh Thu Tháng - ChocoStyle", duLieu.get("thangNay"), "tháng này");
            daGui = true;
        }

        if (Boolean.TRUE.equals(config.getGuiNam())) {
            emailService.guiMailHtml(emailHeThong, "🧪 [TEST] Báo Cáo Doanh Thu Năm - ChocoStyle", duLieu.get("namNay"), "năm nay");
            daGui = true;
        }

        if (!daGui) {
            emailService.guiMailHtml(emailHeThong, "🧪 [TEST] Báo Cáo Doanh Thu - ChocoStyle", duLieu.get("homNay"), "gửi thử nghiệm");
        }

        return ResponseEntity.ok("✅ Đã kiểm tra cấu hình và gửi mail test tương ứng!");
    }
}
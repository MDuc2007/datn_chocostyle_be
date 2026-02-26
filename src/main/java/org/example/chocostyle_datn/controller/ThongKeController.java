package org.example.chocostyle_datn.controller;


import jakarta.servlet.http.HttpServletResponse;
import org.example.chocostyle_datn.entity.CauHinhHeThong;
import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.model.Request.ThongKeRequest;
import org.example.chocostyle_datn.model.Response.DoanhThuResponse;
import org.example.chocostyle_datn.model.Response.SanPhamBanChayResponse;
import org.example.chocostyle_datn.model.Response.TongQuatResponse;
import org.example.chocostyle_datn.model.Response.TrangThaiDonResponse;
import org.example.chocostyle_datn.repository.CauHinhHeThongRepository;
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
@CrossOrigin("*") // Cho phép Vue.js gọi API không bị chặn
public class ThongKeController {


    @Autowired
    private ThongKeService thongKeService;


    @Autowired
    private ThongKeExcelService thongKeExcelService;

    @Autowired
    private CauHinhHeThongRepository cauHinhRepo;

    @Autowired
    private EmailServiceThongKe emailService; // Khai báo biến này để hết lỗi đỏ


    // API 1: Lấy dữ liệu tổng quan cho 4 Card (Hôm nay, Tuần này, Tháng này, Năm nay)
    // URL: GET /api/thong-ke/tong-quat
    @GetMapping("/tong-quat")
    public ResponseEntity<?> getTongQuat() {
        return ResponseEntity.ok(thongKeService.getDuLieuTongQuan());
    }


    // API 2: Lấy dữ liệu biểu đồ doanh thu
    // URL: GET /api/thong-ke/doanh-thu?startDate=2025-01-01&endDate=2025-01-31
    @GetMapping("/doanh-thu")
    public ResponseEntity<List<DoanhThuResponse>> getDoanhThu(ThongKeRequest request) {
        return ResponseEntity.ok(thongKeService.getDoanhThuChart(request));
    }


    // API 3: Top 5 sản phẩm bán chạy nhất
    // URL: GET /api/thong-ke/ban-chay
    @GetMapping("/ban-chay")
    public ResponseEntity<List<SanPhamBanChayResponse>> getBanChay() {
        return ResponseEntity.ok(thongKeService.getTopBanChay());
    }


    // API 4: Biểu đồ tròn trạng thái đơn hàng
    // URL: GET /api/thong-ke/trang-thai
    @GetMapping("/trang-thai")
    public ResponseEntity<List<TrangThaiDonResponse>> getTrangThai() {
        return ResponseEntity.ok(thongKeService.getPhanBoTrangThai());
    }


    // API 5: Danh sách sản phẩm sắp hết hàng (Cảnh báo tồn kho)
    // URL: GET /api/thong-ke/sap-het
    @GetMapping("/sap-het")
    public ResponseEntity<List<ChiTietSanPham>> getSapHet() {
        return ResponseEntity.ok(thongKeService.getSanPhamSapHet());
    }


    // API 6: Phân phối đa kênh (Đã cập nhật logic thật)
    // URL: GET /api/thong-ke/loai-don
    @GetMapping("/loai-don")
    public ResponseEntity<?> getLoaiDon() {
        return ResponseEntity.ok(thongKeService.getPhanBoLoaiDon());
    }
    @GetMapping("/export")
    public void exportDoanhThu(
            HttpServletResponse response,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) throws IOException {


        // 1️⃣ Logic ưu tiên lấy ngày (Giống logic getDataExport của bạn)
        LocalDate finalStart;
        LocalDate finalEnd;


        if (startDate != null && endDate != null) {
            // Case 1: Có chọn ngày
            finalStart = startDate;
            finalEnd = endDate;
        } else {
            // Case 2: Mặc định lấy tháng hiện tại (Hoặc logic khác tùy bạn)
            LocalDate now = LocalDate.now();
            finalStart = now.with(TemporalAdjusters.firstDayOfMonth());
            finalEnd = now.with(TemporalAdjusters.lastDayOfMonth());
        }


        // Gọi qua Service (biến thongKeService đã được @Autowired ở trên)
        List<DoanhThuResponse> dataList = thongKeService.getDataExport(finalStart, finalEnd);


        // Thiết lập Header để trình duyệt hiểu là file tải xuống
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=BaoCaoDoanhThu_" + finalStart + "_to_" + finalEnd + ".xlsx";
        response.setHeader(headerKey, headerValue);


        // Gọi Service viết vào luồng xuất của response
        thongKeExcelService.exportDoanhThu(dataList, finalStart, finalEnd, response.getOutputStream());
    }

    // API lấy cấu hình hiện tại đưa lên giao diện
    @GetMapping("/cau-hinh-email")
    public ResponseEntity<?> getCauHinhEmail() {
        return ResponseEntity.ok(cauHinhRepo.findById(1).orElse(null));
    }

    // API lưu cấu hình từ giao diện gửi xuống
    @PostMapping("/cau-hinh-email")
    public ResponseEntity<?> saveCauHinhEmail(@RequestBody org.example.chocostyle_datn.entity.CauHinhHeThong request) {
        org.example.chocostyle_datn.entity.CauHinhHeThong config = cauHinhRepo.findById(1).orElse(null);
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

    // API GỬI THỬ EMAIL ĐỂ TEST
    @PostMapping("/test-email")
    public ResponseEntity<?> testEmail() {
        // Lấy cấu hình từ Database
        org.example.chocostyle_datn.entity.CauHinhHeThong config = cauHinhRepo.findById(1).orElse(null);
        String emailHeThong = "hethong.chocostyle@gmail.com";

        if (config == null) {
            return ResponseEntity.badRequest().body("❌ Lỗi: Chưa có cấu hình hệ thống!");
        }

        // Lấy toàn bộ bộ dữ liệu thống kê (Ngày, Tuần, Tháng, Năm)
        Map<String, org.example.chocostyle_datn.model.Response.TongQuatResponse> duLieu = thongKeService.getDuLieuTongQuan();
        boolean daGui = false;

        // Kiểm tra xem công tắc nào đang bật thì gửi mail loại đó
        if (config.getGuiNgay() != null && config.getGuiNgay()) {
            emailService.guiMailHtml(emailHeThong, "🧪 [TEST] Báo Cáo Doanh Thu Ngày - ChocoStyle", duLieu.get("homNay"), "hôm nay");
            daGui = true;
        }

        if (config.getGuiTuan() != null && config.getGuiTuan()) {
            emailService.guiMailHtml(emailHeThong, "🧪 [TEST] Báo Cáo Doanh Thu Tuần - ChocoStyle", duLieu.get("tuanNay"), "tuần này");
            daGui = true;
        }

        if (config.getGuiThang() != null && config.getGuiThang()) {
            emailService.guiMailHtml(emailHeThong, "🧪 [TEST] Báo Cáo Doanh Thu Tháng - ChocoStyle", duLieu.get("thangNay"), "tháng này");
            daGui = true;
        }

        if (config.getGuiNam() != null && config.getGuiNam()) {
            emailService.guiMailHtml(emailHeThong, "🧪 [TEST] Báo Cáo Doanh Thu Năm - ChocoStyle", duLieu.get("namNay"), "năm nay");
            daGui = true;
        }

        // Nếu người dùng không bật công tắc nào mà vẫn cố tình bấm Test
        if (!daGui) {
            emailService.guiMailHtml(emailHeThong, "🧪 [TEST] Báo Cáo Doanh Thu - ChocoStyle", duLieu.get("homNay"), "gửi thử nghiệm");
        }

        return ResponseEntity.ok("✅ Đã kiểm tra cấu hình và gửi mail test tương ứng!");
    }
}




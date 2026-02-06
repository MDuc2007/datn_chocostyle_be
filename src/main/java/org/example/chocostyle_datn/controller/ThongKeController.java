package org.example.chocostyle_datn.controller;


import jakarta.servlet.http.HttpServletResponse;
import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.model.Request.ThongKeRequest;
import org.example.chocostyle_datn.model.Response.DoanhThuResponse;
import org.example.chocostyle_datn.model.Response.SanPhamBanChayResponse;
import org.example.chocostyle_datn.model.Response.TrangThaiDonResponse;
import org.example.chocostyle_datn.service.ThongKeExcelService;
import org.example.chocostyle_datn.service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;




import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;


@RestController
@RequestMapping("/api/thong-ke")
@CrossOrigin("*") // Cho phép Vue.js gọi API không bị chặn
public class ThongKeController {


    @Autowired
    private ThongKeService thongKeService;


    @Autowired
    private ThongKeExcelService thongKeExcelService;


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
}




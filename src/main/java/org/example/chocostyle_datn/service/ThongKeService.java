package org.example.chocostyle_datn.service;


import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.model.Request.ThongKeRequest;
import org.example.chocostyle_datn.model.Response.*;
import org.example.chocostyle_datn.repository.ThongKeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class ThongKeService {


    @Autowired
    private ThongKeRepository thongKeRepo;


    /**
     * 1. Lấy dữ liệu biểu đồ doanh thu (Line Chart)
     * Logic: Nếu không chọn ngày, mặc định lấy tháng hiện tại.
     */
    public List<DoanhThuResponse> getDoanhThuChart(ThongKeRequest req) {
        LocalDate startDate = req.getStartDate();
        LocalDate endDate = req.getEndDate();


        // Mặc định: Lấy ngày đầu tháng đến ngày hiện tại nếu null
        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now.with(TemporalAdjusters.firstDayOfMonth()); // Ngày 1
            endDate = now.with(TemporalAdjusters.lastDayOfMonth());    // Ngày cuối tháng
        }


        return thongKeRepo.getDoanhThuChart(startDate, endDate);
    }

    public List<DoanhThuResponse> getDataExport(LocalDate startDate, LocalDate endDate) {
        return thongKeRepo.getDoanhThuChart(startDate, endDate);
    }


    /**
     * 2. Lấy Top 5 sản phẩm bán chạy (Table)
     * Dựa trên số lượng bán của các đơn HOÀN THÀNH
     */
    public List<SanPhamBanChayResponse> getTopBanChay() {
        return thongKeRepo.getTopBanChay();
    }


    /**
     * 3. Lấy tỷ lệ trạng thái đơn hàng (Pie Chart)
     * Trả về danh sách: Trạng thái - Số lượng
     */
    public List<TrangThaiDonResponse> getPhanBoTrangThai() {
        return thongKeRepo.getTrangThaiDon();
    }


    /**
     * 4. Lấy danh sách sản phẩm sắp hết hàng
     * Logic: Lấy các SP có số lượng tồn <= limit (ví dụ 10)
     */
    public List<ChiTietSanPham> getSanPhamSapHet() {
        // Giới hạn cảnh báo là 10 sản phẩm
        return thongKeRepo.getSanPhamSapHetHang(10);
    }


    /**
     * 5. Lấy dữ liệu Tổng quan cho 4 Card (Hôm nay, Tuần này, Tháng này, Năm nay)
     * Trả về Map để Frontend dễ lấy theo key.
     */
    public Map<String, TongQuatResponse> getDuLieuTongQuan() {
        Map<String, TongQuatResponse> result = new HashMap<>();
        LocalDate now = LocalDate.now();


        // --- A. Hôm nay ---
        result.put("homNay", calculateSummary(now, now));


        // --- B. Tuần này (Thứ 2 -> Chủ nhật) ---
        LocalDate startWeek = now.with(DayOfWeek.MONDAY);
        LocalDate endWeek = now.with(DayOfWeek.SUNDAY);
        result.put("tuanNay", calculateSummary(startWeek, endWeek));


        // --- C. Tháng này ---
        LocalDate startMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endMonth = now.with(TemporalAdjusters.lastDayOfMonth());
        result.put("thangNay", calculateSummary(startMonth, endMonth));


        // --- D. Năm nay ---
        LocalDate startYear = now.with(TemporalAdjusters.firstDayOfYear());
        LocalDate endYear = now.with(TemporalAdjusters.lastDayOfYear());
        result.put("namNay", calculateSummary(startYear, endYear));


        return result;
    }


    // ================= PRIVATE HELPER METHODS =================


    /**
     * Hàm phụ: Tính tổng doanh thu và số lượng đơn trong khoảng thời gian
     * Tận dụng lại query getDoanhThuChart để đỡ phải viết thêm Query mới.
     * (Lưu ý: Với dữ liệu lớn hàng triệu dòng, nên viết Query riêng dùng SUM/COUNT trực tiếp trong DB)
     */
    private TongQuatResponse calculateSummary(LocalDate start, LocalDate end) {
        List<DoanhThuResponse> dataList = thongKeRepo.getDoanhThuChart(start, end);


        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalOrders = 0;


        if (dataList != null && !dataList.isEmpty()) {
            // Cộng dồn doanh thu
            totalRevenue = dataList.stream()
                    .map(DoanhThuResponse::getDoanhThu)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);


            // Cộng dồn số đơn
            totalOrders = dataList.stream()
                    .mapToInt(DoanhThuResponse::getSoLuongDon)
                    .sum();
        }


        return TongQuatResponse.builder()
                .doanhThu(totalRevenue)
                .soDonHang(totalOrders)
                .build();
    }
    public List<Map<String, Object>> getPhanBoLoaiDon() {
        List<Object[]> rawData = thongKeRepo.getPhanBoLoaiDonRaw();
        List<Map<String, Object>> result = new ArrayList<>();


        for (Object[] row : rawData) {
            // row[0] là loai_don (Integer), row[1] là count (Number)
            if (row[0] == null) continue;


            int loaiDon = (Integer) row[0];
            long soLuong = ((Number) row[1]).longValue();


            Map<String, Object> item = new HashMap<>();


            // GIẢ ĐỊNH LOGIC: 0 = Tại quầy, 1 = Online (Bạn sửa lại theo quy ước của nhóm bạn)
            String label = "Khác";
            if (loaiDon == 0) label = "Tại quầy";
            else if (loaiDon == 1) label = "Online";


            item.put("loai", label);
            item.put("soLuong", soLuong);


            result.add(item);
        }


        // Nếu DB chưa có dữ liệu, trả về dữ liệu giả để Test Frontend
        if (result.isEmpty()) {
            result.add(Map.of("loai", "Online", "soLuong", 0));
            result.add(Map.of("loai", "Tại quầy", "soLuong", 0));
        }


        return result;
    }
}


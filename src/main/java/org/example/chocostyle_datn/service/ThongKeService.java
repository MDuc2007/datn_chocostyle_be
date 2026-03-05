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


        // Mặc định: Lấy ngày hiện tại (Hôm nay) nếu null
        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now; // Gán là hôm nay
            endDate = now;   // Gán là hôm nay
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
    /**
     * Lấy dữ liệu cho Bảng Thống Kê Chi Tiết Theo Thời Gian
     * Trả về danh sách (List) để Frontend dễ dàng dùng v-for render ra bảng
     */
    public List<ThongKeChiTietResponse> getBangThongKeChiTiet() {
        List<ThongKeChiTietResponse> result = new ArrayList<>();
        LocalDate now = LocalDate.now();

        // 1. Dòng Hôm nay (So với Hôm qua)
        LocalDate yesterday = now.minusDays(1);
        result.add(calculateRow("Hôm nay", now, now, yesterday, yesterday));

        // 2. Dòng Tuần này (So với Tuần trước)
        LocalDate startWeek = now.with(DayOfWeek.MONDAY);
        LocalDate endWeek = now.with(DayOfWeek.SUNDAY);
        LocalDate startPrevWeek = startWeek.minusWeeks(1);
        LocalDate endPrevWeek = endWeek.minusWeeks(1);
        result.add(calculateRow("Tuần này", startWeek, endWeek, startPrevWeek, endPrevWeek));

        // 3. Dòng Tháng này (So với Tháng trước)
        LocalDate startMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endMonth = now.with(TemporalAdjusters.lastDayOfMonth());
        LocalDate startPrevMonth = startMonth.minusMonths(1);
        LocalDate endPrevMonth = startPrevMonth.with(TemporalAdjusters.lastDayOfMonth());
        result.add(calculateRow("Tháng này", startMonth, endMonth, startPrevMonth, endPrevMonth));

        // 4. Dòng Năm nay (So với Năm trước)
        LocalDate startYear = now.with(TemporalAdjusters.firstDayOfYear());
        LocalDate endYear = now.with(TemporalAdjusters.lastDayOfYear());
        LocalDate startPrevYear = startYear.minusYears(1);
        LocalDate endPrevYear = startPrevYear.with(TemporalAdjusters.lastDayOfYear());
        result.add(calculateRow("Năm nay", startYear, endYear, startPrevYear, endPrevYear));

        return result;
    }

    /**
     * Hàm phụ: Tính toán chi tiết cho 1 dòng trong bảng
     */
    private ThongKeChiTietResponse calculateRow(String thoiGianStr, LocalDate currStart, LocalDate currEnd,
                                                LocalDate prevStart, LocalDate prevEnd) {
        // 1. Lấy dữ liệu kỳ hiện tại
        List<DoanhThuResponse> currentData = thongKeRepo.getDoanhThuChart(currStart, currEnd);
        BigDecimal currentRevenue = BigDecimal.ZERO;
        int currentOrders = 0;

        if (currentData != null && !currentData.isEmpty()) {
            // Chỗ này bạn có thể dùng getDoanhThu() nếu muốn lấy tổng,
            // hoặc getDoanhThuThucTe() nếu đã cập nhật logic như bước trước
            currentRevenue = currentData.stream()
                    .map(DoanhThuResponse::getDoanhThu)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            currentOrders = currentData.stream()
                    .mapToInt(DoanhThuResponse::getSoLuongDon)
                    .sum();
        }

        // 2. Lấy dữ liệu kỳ trước (để so sánh tăng trưởng)
        List<DoanhThuResponse> previousData = thongKeRepo.getDoanhThuChart(prevStart, prevEnd);
        BigDecimal previousRevenue = BigDecimal.ZERO;

        if (previousData != null && !previousData.isEmpty()) {
            previousRevenue = previousData.stream()
                    .map(DoanhThuResponse::getDoanhThu)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // 3. Tính Giá trị trung bình/đơn
        BigDecimal avgValue = BigDecimal.ZERO;
        if (currentOrders > 0) {
            avgValue = currentRevenue.divide(new BigDecimal(currentOrders), 0, java.math.RoundingMode.HALF_UP);
        }

        // 4. Tính % Tăng trưởng
        double growth = 0.0;
        if (previousRevenue.compareTo(BigDecimal.ZERO) == 0) {
            growth = currentRevenue.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
        } else {
            BigDecimal diff = currentRevenue.subtract(previousRevenue);
            BigDecimal percentage = diff.divide(previousRevenue, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            growth = percentage.doubleValue();
        }

        return ThongKeChiTietResponse.builder()
                .thoiGian(thoiGianStr)
                .doanhThu(currentRevenue)
                .soDonHang(currentOrders)
                .giaTriTrungBinh(avgValue)
                .tangTruong(growth)
                .build();
    }
}


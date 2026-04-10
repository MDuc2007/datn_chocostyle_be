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

@Service
public class ThongKeService {

    @Autowired
    private ThongKeRepository thongKeRepo;

    public List<DoanhThuResponse> getDoanhThuChart(ThongKeRequest req) {
        LocalDate startDate = req.getStartDate();
        LocalDate endDate = req.getEndDate();

        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now;
            endDate = now;
        }
        return thongKeRepo.getDoanhThuChart(startDate, endDate);
    }

    public List<SanPhamBanChayResponse> getTopBanChay(LocalDate startDate, LocalDate endDate) {
        // Mẹo fix lỗi SQL Server: Nếu Frontend không truyền ngày (chọn Tất cả),
        // ta tự động lấy khoảng thời gian siêu rộng (Từ năm 2000 đến 2100) thay vì ném NULL xuống DB
        if (startDate == null) {
            startDate = LocalDate.of(2000, 1, 1);
        }
        if (endDate == null) {
            endDate = LocalDate.of(2100, 12, 31);
        }
        return thongKeRepo.getTopBanChayTheoThoiGian(startDate, endDate);
    }

    public List<SanPhamBanChayResponse> getTopBanChayTheoDotGiamGia(Integer idDotGiamGia) {
        if (idDotGiamGia == null) {
            return new ArrayList<>();
        }
        return thongKeRepo.getTopBanChayTheoDotGiamGia(idDotGiamGia);
    }

    public List<TrangThaiDonResponse> getPhanBoTrangThai() {
        return thongKeRepo.getTrangThaiDon();
    }

    public List<ChiTietSanPham> getSanPhamSapHet() {
        return thongKeRepo.getSanPhamSapHetHang(10);
    }

    public Map<String, TongQuatResponse> getDuLieuTongQuan() {
        Map<String, TongQuatResponse> result = new HashMap<>();
        LocalDate now = LocalDate.now();

        result.put("homNay", calculateSummary(now, now));

        LocalDate startWeek = now.with(DayOfWeek.MONDAY);
        LocalDate endWeek = now.with(DayOfWeek.SUNDAY);
        result.put("tuanNay", calculateSummary(startWeek, endWeek));

        LocalDate startMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endMonth = now.with(TemporalAdjusters.lastDayOfMonth());
        result.put("thangNay", calculateSummary(startMonth, endMonth));

        LocalDate startYear = now.with(TemporalAdjusters.firstDayOfYear());
        LocalDate endYear = now.with(TemporalAdjusters.lastDayOfYear());
        result.put("namNay", calculateSummary(startYear, endYear));

        return result;
    }

    private TongQuatResponse calculateSummary(LocalDate start, LocalDate end) {
        List<DoanhThuResponse> dataList = thongKeRepo.getDoanhThuChart(start, end);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalOrders = 0;

        if (dataList != null && !dataList.isEmpty()) {
            totalRevenue = dataList.stream()
                    .map(DoanhThuResponse::getDoanhThu)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

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
            if (row[0] == null) continue;
            int loaiDon = (Integer) row[0];
            long soLuong = ((Number) row[1]).longValue();

            Map<String, Object> item = new HashMap<>();
            String label = "Khác";
            if (loaiDon == 0) label = "Tại quầy";
            else if (loaiDon == 1) label = "Online";

            item.put("loai", label);
            item.put("soLuong", soLuong);
            result.add(item);
        }

        if (result.isEmpty()) {
            result.add(Map.of("loai", "Online", "soLuong", 0));
            result.add(Map.of("loai", "Tại quầy", "soLuong", 0));
        }
        return result;
    }

    public List<ThongKeChiTietResponse> getBangThongKeChiTiet() {
        List<ThongKeChiTietResponse> result = new ArrayList<>();
        LocalDate now = LocalDate.now();

        LocalDate yesterday = now.minusDays(1);
        result.add(calculateRow("Hôm nay", now, now, yesterday, yesterday));

        LocalDate startWeek = now.with(DayOfWeek.MONDAY);
        LocalDate endWeek = now.with(DayOfWeek.SUNDAY);
        LocalDate startPrevWeek = startWeek.minusWeeks(1);
        LocalDate endPrevWeek = endWeek.minusWeeks(1);
        result.add(calculateRow("Tuần này", startWeek, endWeek, startPrevWeek, endPrevWeek));

        LocalDate startMonth = now.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endMonth = now.with(TemporalAdjusters.lastDayOfMonth());
        LocalDate startPrevMonth = startMonth.minusMonths(1);
        LocalDate endPrevMonth = startPrevMonth.with(TemporalAdjusters.lastDayOfMonth());
        result.add(calculateRow("Tháng này", startMonth, endMonth, startPrevMonth, endPrevMonth));

        LocalDate startYear = now.with(TemporalAdjusters.firstDayOfYear());
        LocalDate endYear = now.with(TemporalAdjusters.lastDayOfYear());
        LocalDate startPrevYear = startYear.minusYears(1);
        LocalDate endPrevYear = startPrevYear.with(TemporalAdjusters.lastDayOfYear());
        result.add(calculateRow("Năm nay", startYear, endYear, startPrevYear, endPrevYear));

        return result;
    }

    private ThongKeChiTietResponse calculateRow(String thoiGianStr, LocalDate currStart, LocalDate currEnd,
                                                LocalDate prevStart, LocalDate prevEnd) {
        List<DoanhThuResponse> currentData = thongKeRepo.getDoanhThuChart(currStart, currEnd);
        BigDecimal currentRevenue = BigDecimal.ZERO;
        int currentOrders = 0;

        if (currentData != null && !currentData.isEmpty()) {
            currentRevenue = currentData.stream()
                    .map(DoanhThuResponse::getDoanhThu)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            currentOrders = currentData.stream()
                    .mapToInt(DoanhThuResponse::getSoLuongDon)
                    .sum();
        }

        List<DoanhThuResponse> previousData = thongKeRepo.getDoanhThuChart(prevStart, prevEnd);
        BigDecimal previousRevenue = BigDecimal.ZERO;

        if (previousData != null && !previousData.isEmpty()) {
            previousRevenue = previousData.stream()
                    .map(DoanhThuResponse::getDoanhThu)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal avgValue = BigDecimal.ZERO;
        if (currentOrders > 0) {
            avgValue = currentRevenue.divide(new BigDecimal(currentOrders), 0, java.math.RoundingMode.HALF_UP);
        }

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
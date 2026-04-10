package org.example.chocostyle_datn.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.chocostyle_datn.model.Response.HoaDonExportResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class ThongKeExcelService {
    public void exportDoanhThu(List<HoaDonExportResponse> data, LocalDate startDate, LocalDate endDate, OutputStream os)
            throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("ChiTietDoanhThu");

        // ================== COLUMN WIDTH ==================
        sheet.setColumnWidth(0, 8 * 256);  // STT
        sheet.setColumnWidth(1, 15 * 256); // Mã Hóa Đơn
        sheet.setColumnWidth(2, 25 * 256); // Khách hàng
        sheet.setColumnWidth(3, 15 * 256); // Loại đơn
        sheet.setColumnWidth(4, 20 * 256); // Trạng thái
        sheet.setColumnWidth(5, 20 * 256); // Ngày tạo
        sheet.setColumnWidth(6, 20 * 256); // Tổng Tiền

        // ================== STYLES ==================
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(headerStyle);

        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(dataStyle);

        CellStyle moneyStyle = workbook.createCellStyle();
        moneyStyle.cloneStyleFrom(dataStyle);
        DataFormat format = workbook.createDataFormat();
        moneyStyle.setDataFormat(format.getFormat("#,##0 ₫"));

        // ================== TITLE & SUB-TITLE ==================
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO CHI TIẾT DOANH THU");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

        Row dateRow = sheet.createRow(1);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("Từ ngày: " + startDate + " - Đến ngày: " + endDate);
        CellStyle subTitleStyle = workbook.createCellStyle();
        subTitleStyle.setAlignment(HorizontalAlignment.CENTER);
        dateCell.setCellStyle(subTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

        // ================== HEADER ==================
        String[] headers = { "STT", "Mã Hóa Đơn", "Khách Hàng", "Loại Đơn", "Trạng Thái", "Ngày Tạo", "Doanh Thu" };
        Row headerRow = sheet.createRow(3);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // ================== DATA ==================
        int rowIndex = 4;
        int stt = 1;
        double tongDoanhThu = 0;

        for (HoaDonExportResponse item : data) {
            Row row = sheet.createRow(rowIndex++);

            createCell(row, 0, stt++, dataStyle);
            createCell(row, 1, item.getMaHoaDon(), dataStyle);
            createCell(row, 2, item.getTenKhachHang(), dataStyle);
            createCell(row, 3, item.getLoaiDon() == 1 ? "Online" : "Tại quầy", dataStyle);
            createCell(row, 4, mapTrangThai(item.getTrangThai()), dataStyle);
            createCell(row, 5, item.getNgayTao(), dataStyle);

            Cell revenueCell = row.createCell(6);
            double tien = item.getTongTien() != null ? item.getTongTien().doubleValue() : 0;
            revenueCell.setCellValue(tien);
            revenueCell.setCellStyle(moneyStyle);

            tongDoanhThu += tien;
        }

        // ================== FOOTER (TỔNG CỘNG) ==================
        Row totalRow = sheet.createRow(rowIndex);
        Cell labelCell = totalRow.createCell(0);
        labelCell.setCellValue("TỔNG CỘNG (" + data.size() + " đơn)");
        labelCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 5)); // Merge cột 0 đến 5

        Cell totalMoneyCell = totalRow.createCell(6);
        totalMoneyCell.setCellValue(tongDoanhThu);
        CellStyle totalMoneyStyle = workbook.createCellStyle();
        totalMoneyStyle.cloneStyleFrom(headerStyle);
        totalMoneyStyle.setDataFormat(format.getFormat("#,##0 ₫"));
        totalMoneyCell.setCellStyle(totalMoneyStyle);

        workbook.write(os);
        workbook.close();
    }

    private String mapTrangThai(Integer status) {
        if (status == null) return "Không xác định";
        return switch (status) {
            case 0 -> "Chờ xác nhận";
            case 1 -> "Đã xác nhận";
            case 2 -> "Chờ vận chuyển";
            case 3 -> "Đang vận chuyển";
            case 4 -> "Hoàn thành";
            default -> "Khác";
        };
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private void createCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value != null ? value.toString() : "");
        }
        cell.setCellStyle(style);
    }
}
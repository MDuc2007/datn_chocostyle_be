package org.example.chocostyle_datn.service;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.chocostyle_datn.model.Response.DoanhThuResponse; // Import DTO doanh thu
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;


@Service
public class ThongKeExcelService {
    public void exportDoanhThu(List<DoanhThuResponse> data, LocalDate startDate, LocalDate endDate, OutputStream os)
            throws IOException {


        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("BaoCaoDoanhThu");


        // ================== COLUMN WIDTH ==================
        sheet.setColumnWidth(0, 10 * 256); // STT
        sheet.setColumnWidth(1, 20 * 256); // Thời gian
        sheet.setColumnWidth(2, 20 * 256); // Số lượng đơn
        sheet.setColumnWidth(3, 25 * 256); // Doanh thu


        // ================== STYLES (Giữ nguyên style của bạn) ==================
        // Title style
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);


        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorder(headerStyle);


        // Data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(dataStyle);


        // Money style (Format tiền tệ)
        CellStyle moneyStyle = workbook.createCellStyle();
        moneyStyle.cloneStyleFrom(dataStyle);
        DataFormat format = workbook.createDataFormat();
        moneyStyle.setDataFormat(format.getFormat("#,##0 ₫"));


        // ================== TITLE ==================
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("BÁO CÁO DOANH THU");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));


        // ================== SUB-TITLE (Thời gian thống kê) ==================
        Row dateRow = sheet.createRow(1);
        Cell dateCell = dateRow.createCell(0);
        String subTitle = "Từ ngày: " + startDate + " - Đến ngày: " + endDate;
        dateCell.setCellValue(subTitle);
        CellStyle subTitleStyle = workbook.createCellStyle();
        subTitleStyle.setAlignment(HorizontalAlignment.CENTER);
        dateCell.setCellStyle(subTitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 3));


        // ================== HEADER ==================
        String[] headers = { "STT", "Thời Gian", "Số Lượng Đơn", "Doanh Thu" };
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
        int tongDonHang = 0;


        for (DoanhThuResponse item : data) {
            Row row = sheet.createRow(rowIndex++);


            createCell(row, 0, stt++, dataStyle);
            createCell(row, 1, item.getThoiGian(), dataStyle); // Thời gian (String)
            createCell(row, 2, item.getSoLuongDon(), dataStyle); // Số đơn


            // Doanh thu (Double/BigDecimal)
            Cell revenueCell = row.createCell(3);
            revenueCell.setCellValue(item.getDoanhThu().doubleValue());
            revenueCell.setCellStyle(moneyStyle);


            // Tính tổng
            tongDoanhThu += item.getDoanhThu().doubleValue();
            tongDonHang += item.getSoLuongDon();
        }


        // ================== FOOTER (TỔNG CỘNG) ==================
        Row totalRow = sheet.createRow(rowIndex);


        Cell labelCell = totalRow.createCell(0);
        labelCell.setCellValue("TỔNG CỘNG");
        labelCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, 0, 1)); // Merge STT và Thời gian


        // Tổng đơn
        createCell(totalRow, 2, tongDonHang, headerStyle);


        // Tổng tiền
        Cell totalMoneyCell = totalRow.createCell(3);
        totalMoneyCell.setCellValue(tongDoanhThu);
        CellStyle totalMoneyStyle = workbook.createCellStyle();
        totalMoneyStyle.cloneStyleFrom(headerStyle);
        totalMoneyStyle.setDataFormat(format.getFormat("#,##0 ₫"));
        totalMoneyCell.setCellStyle(totalMoneyStyle);


        workbook.write(os);
        workbook.close();
    }


    // ================== UTILS ==================
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


package org.example.chocostyle_datn.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.chocostyle_datn.model.Response.BienTheResponse;
import org.example.chocostyle_datn.model.Response.SanPhamResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class SanPhamExcelService {

    public void writeExcel(List<SanPhamResponse> data, OutputStream os)
            throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("SanPham");

        // ================== COLUMN WIDTH (GIỐNG ẢNH 1) ==================
        sheet.setColumnWidth(0, 8 * 256);    // STT
        sheet.setColumnWidth(1, 15 * 256);   // Mã SP
        sheet.setColumnWidth(2, 30 * 256);   // Tên sản phẩm
        sheet.setColumnWidth(3, 18 * 256);   // Xuất xứ
        sheet.setColumnWidth(4, 18 * 256);   // Chất liệu
        sheet.setColumnWidth(5, 14 * 256);   // Số lượng
        sheet.setColumnWidth(6, 25 * 256);   // Giá
        sheet.setColumnWidth(7, 16 * 256);   // Trạng thái

        // ================== STYLES ==================

        // Title style
        CellStyle titleStyle = workbook.createCellStyle();
        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);

        // Subtitle style
        CellStyle subTitleStyle = workbook.createCellStyle();
        Font subFont = workbook.createFont();
        subFont.setItalic(true);
        subTitleStyle.setFont(subFont);
        subTitleStyle.setAlignment(HorizontalAlignment.CENTER);

        // Header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(headerStyle);

        // Data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(dataStyle);

        // ================== TITLE ==================
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("DANH SÁCH SẢN PHẨM");
        titleCell.setCellStyle(titleStyle);

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 7));

        // ================== DATE ==================
        Row dateRow = sheet.createRow(1);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("Xuất file excel vào: " + LocalDate.now());
        dateCell.setCellStyle(subTitleStyle);

        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 7));

        // ================== HEADER ==================
        String[] headers = {
                "STT",
                "Mã SP",
                "Tên sản phẩm",
                "Xuất xứ",
                "Chất liệu",
                "Số lượng",
                "Giá",
                "Trạng thái"
        };

        Row headerRow = sheet.createRow(3);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // ================== DATA ==================
        int rowIndex = 4;
        int stt = 1;

        for (SanPhamResponse sp : data) {
            Row row = sheet.createRow(rowIndex++);

            int col = 0;
            createCell(row, col++, stt++, dataStyle);
            createCell(row, col++, sp.getMaSp(), dataStyle);
            createCell(row, col++, sp.getTenSp(), dataStyle);
            createCell(row, col++, sp.getTenXuatXu(), dataStyle);
            createCell(row, col++, sp.getTenChatLieu(), dataStyle);

            // SỐ LƯỢNG = tổng biến thể
            int soLuong = sp.getBienTheList()
                    .stream()
                    .mapToInt(BienTheResponse::getSoLuongTon)
                    .sum();
            createCell(row, col++, soLuong, dataStyle);

            // GIÁ (min ~ max)
            List<BigDecimal> prices = sp.getBienTheList()
                    .stream()
                    .map(BienTheResponse::getGiaBan)
                    .filter(Objects::nonNull)
                    .toList();

            if (prices.isEmpty()) {
                createCell(row, col++, 0, dataStyle);
            } else {
                BigDecimal min = prices.stream().min(BigDecimal::compareTo).get();
                BigDecimal max = prices.stream().max(BigDecimal::compareTo).get();

                if (min.compareTo(max) == 0) {
                    createCell(row, col++, min.doubleValue(), dataStyle);
                } else {
                    createCell(row, col++, min + " ~ " + max, dataStyle);
                }
            }

            // TRẠNG THÁI
            createCell(
                    row,
                    col,
                    sp.getTrangThai() == 1 ? "Đang bán" : "Ngừng bán",
                    dataStyle
            );
        }

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

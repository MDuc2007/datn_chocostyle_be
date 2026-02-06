package org.example.chocostyle_datn.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class ChiTietSanPhamExcelService {

    public void writeExcel(List<ChiTietSanPham> data, OutputStream os)
            throws IOException {

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("ChiTietSanPham");

        // ================== COLUMN WIDTH ==================
        sheet.setColumnWidth(0, 20 * 256); // Mã SP
        sheet.setColumnWidth(1, 18 * 256); // Mã CTSP
        sheet.setColumnWidth(2, 12 * 256); // Kích cỡ
        sheet.setColumnWidth(3, 15 * 256); // Màu sắc
        sheet.setColumnWidth(4, 18 * 256); // Số lượng tồn
        sheet.setColumnWidth(5, 18 * 256); // Giá nhập
        sheet.setColumnWidth(6, 18 * 256); // Giá bán
        sheet.setColumnWidth(7, 15 * 256); // Trạng thái

        // ================== STYLES ==================

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
        setBorder(headerStyle);

        // Data style
        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setAlignment(HorizontalAlignment.CENTER);
        dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(dataStyle);

        // ================== TITLE ==================
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("DANH SÁCH CHI TIẾT SẢN PHẨM");
        titleCell.setCellStyle(titleStyle);

        sheet.addMergedRegion(new CellRangeAddress(
                0, 0, 0, 7
        ));

        // ================== DATE ==================
        Row dateRow = sheet.createRow(1);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("Xuất file excel vào: " + LocalDate.now());
        dateCell.setCellStyle(titleStyle);

        sheet.addMergedRegion(new CellRangeAddress(
                1, 1, 0, 7
        ));

        // ================== HEADER ==================
        String[] headers = {
                "Mã SP",
                "Mã CTSP",
                "Kích cỡ",
                "Màu sắc",
                "Số lượng tồn",
                "Giá nhập",
                "Giá bán",
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
        for (ChiTietSanPham ctsp : data) {
            Row row = sheet.createRow(rowIndex++);

            createCell(row, 0, ctsp.getIdSanPham().getTenSp(), dataStyle);
            createCell(row, 1, ctsp.getMaChiTietSanPham(), dataStyle);
            createCell(row, 2, ctsp.getIdKichCo().getTenKichCo(), dataStyle);
            createCell(row, 3, ctsp.getIdMauSac().getTenMauSac(), dataStyle);
            createCell(row, 4, ctsp.getSoLuongTon(), dataStyle);
            createCell(row, 5,
                    ctsp.getGiaNhap() != null ? ctsp.getGiaNhap().doubleValue() : 0,
                    dataStyle);
            createCell(row, 6,
                    ctsp.getGiaBan() != null ? ctsp.getGiaBan().doubleValue() : 0,
                    dataStyle);
            createCell(row, 7,
                    ctsp.getTrangThai() == 1 ? "Đang bán" : "Ngừng bán",
                    dataStyle);
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

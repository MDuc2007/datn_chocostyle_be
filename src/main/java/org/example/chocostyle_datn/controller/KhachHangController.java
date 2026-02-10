package org.example.chocostyle_datn.controller;


import jakarta.validation.Valid;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.chocostyle_datn.entity.KhachHang;
import org.example.chocostyle_datn.model.Request.KhachHangRequest;
import org.example.chocostyle_datn.model.Response.KhachHangDetailResponse;
import org.example.chocostyle_datn.model.Response.KhachHangResponse;
import org.example.chocostyle_datn.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/khach-hang")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class KhachHangController {


    @Autowired
    private KhachHangService khachHangService;


    @GetMapping("/export-excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) throws IOException {


        List<KhachHang> list = khachHangService.getKhachHangForExport(keyword, status);


        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh sách khách hàng");


        // ================= STYLE =================


        DataFormat dataFormat = workbook.createDataFormat();


        CellStyle vndStyle = workbook.createCellStyle();
        vndStyle.setDataFormat(
                dataFormat.getFormat("#,##0 [$₫-vi-VN]")
        );
        vndStyle.setBorderTop(BorderStyle.THIN);
        vndStyle.setBorderBottom(BorderStyle.THIN);
        vndStyle.setBorderLeft(BorderStyle.THIN);
        vndStyle.setBorderRight(BorderStyle.THIN);
        vndStyle.setAlignment(HorizontalAlignment.RIGHT);


        Font titleFont = workbook.createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);


        CellStyle titleStyle = workbook.createCellStyle();
        titleStyle.setFont(titleFont);
        titleStyle.setAlignment(HorizontalAlignment.CENTER);


        Font italicFont = workbook.createFont();
        italicFont.setItalic(true);


        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setFont(italicFont);
        dateStyle.setAlignment(HorizontalAlignment.CENTER);


        Font headerFont = workbook.createFont();
        headerFont.setBold(true);


        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);


        CellStyle dataStyle = workbook.createCellStyle();
        dataStyle.setBorderTop(BorderStyle.THIN);
        dataStyle.setBorderBottom(BorderStyle.THIN);
        dataStyle.setBorderLeft(BorderStyle.THIN);
        dataStyle.setBorderRight(BorderStyle.THIN);


        // ================= TITLE =================
        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("DANH SÁCH KHÁCH HÀNG");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 10));


        // ================= DATE =================
        Row dateRow = sheet.createRow(1);
        Cell dateCell = dateRow.createCell(0);
        dateCell.setCellValue("Xuất file excel vào: " + LocalDate.now());
        dateCell.setCellStyle(dateStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 10));


        // ================= HEADER =================
        String[] headers = {
                "STT", "Mã KH", "Tên khách hàng", "SĐT", "Email",
                "Giới tính", "Ngày sinh", "Trạng thái",
                "Số đơn", "Tổng chi tiêu", "Ngày tạo"
        };


        Row headerRow = sheet.createRow(3);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }


        // ================= DATA =================
        int rowIndex = 4;
        int stt = 1;


        for (KhachHang kh : list) {
            Row row = sheet.createRow(rowIndex++);


            row.createCell(0).setCellValue(stt++);
            row.createCell(1).setCellValue(kh.getMaKh());
            row.createCell(2).setCellValue(kh.getTenKhachHang());
            row.createCell(3).setCellValue(kh.getSoDienThoai());
            row.createCell(4).setCellValue(kh.getEmail());
            row.createCell(5).setCellValue(
                    kh.getGioiTinh() == null ? "" : (kh.getGioiTinh() ? "Nam" : "Nữ")
            );
            row.createCell(6).setCellValue(
                    kh.getNgaySinh() == null ? "" : kh.getNgaySinh().toString()
            );
            row.createCell(7).setCellValue(
                    kh.getTrangThai() == 1 ? "Hoạt động" : "Ngưng"
            );
            row.createCell(8).setCellValue(kh.getSoLuongDonHang());
            Cell moneyCell = row.createCell(9);
            moneyCell.setCellValue(
                    kh.getTongChiTieu() == null ? 0 : kh.getTongChiTieu().doubleValue()
            );
            moneyCell.setCellStyle(vndStyle);


            row.createCell(10).setCellValue(kh.getNgayTao().toString());


            for (int i = 0; i < headers.length; i++) {
                if (i == 9) continue; // bỏ qua cột tiền
                row.getCell(i).setCellStyle(dataStyle);
            }


        }


        // ================= AUTO SIZE =================
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }


        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();


        HttpHeaders headersHttp = new HttpHeaders();
        headersHttp.setContentDisposition(
                ContentDisposition.builder("attachment")
                        .filename("khach_hang.xlsx")
                        .build()
        );
        headersHttp.setContentType(
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
        );


        return new ResponseEntity<>(out.toByteArray(), headersHttp, HttpStatus.OK);
    }






    // 1. LẤY DANH SÁCH + PHÂN TRANG
    @GetMapping
    public ResponseEntity<Page<KhachHangResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status
    ) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
        return ResponseEntity.ok(khachHangService.getKhachHangs(keyword, status, pageRequest));
    }


    // 2. CHI TIẾT KHÁCH HÀNG
    @GetMapping("/{id}")
    public ResponseEntity<KhachHangDetailResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(khachHangService.getDetailById(id));
    }


    // 3. THÊM MỚI
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> add(
            @Valid @RequestPart("data") KhachHangRequest req,
            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return handleValidationErrors(result);
        }


        try {
            khachHangService.addKhachHang(req, avatarFile);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Thêm khách hàng thành công");
        } catch (ResponseStatusException e) {
            // BẮT LỖI CHECK TRÙNG (409)
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }


    // 4. CẬP NHẬT
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            @PathVariable Integer id,
            @Valid @RequestPart("data") KhachHangRequest req,
            @RequestPart(value = "avatarFile", required = false) MultipartFile avatarFile,
            BindingResult result
    ) {
        if (result.hasErrors()) {
            return handleValidationErrors(result);
        }


        try {
            khachHangService.updateKhachHang(id, req, avatarFile);
            return ResponseEntity.ok("Cập nhật khách hàng thành công");
        } catch (ResponseStatusException e) {
            // BẮT LỖI CHECK TRÙNG (409)
            return ResponseEntity
                    .status(e.getStatusCode())
                    .body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }


    // 5. ĐỔI TRẠNG THÁI
    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        try {
            khachHangService.toggleStatus(id);
            return ResponseEntity.ok("Đã cập nhật trạng thái thành công");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }


    // 6. THỐNG KÊ
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> res = new HashMap<>();
        res.put("totalCustomers", khachHangService.getTotalKhachHang());
        return ResponseEntity.ok(res);
    }


    // ================== VALIDATION HELPER ==================


    private ResponseEntity<Map<String, String>> handleValidationErrors(BindingResult result) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError e : result.getFieldErrors()) {
            errors.put(e.getField(), e.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(errors);
    }
}


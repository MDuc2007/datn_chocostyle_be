package org.example.chocostyle_datn.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.model.Request.ChiTietSanPhamRequest;
import org.example.chocostyle_datn.model.Response.ChiTietSanPhamResponse;
import org.example.chocostyle_datn.service.ChiTietSanPhamExcelService;
import org.example.chocostyle_datn.service.ChiTietSanPhamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/chi-tiet-san-pham")
@RequiredArgsConstructor
public class ChiTietSanPhamController {

    private final ChiTietSanPhamService service;
    private final ChiTietSanPhamService chiTietSanPhamService;
    private final ChiTietSanPhamExcelService excelService;

    /* ================= GET ================= */

    @GetMapping
    public ResponseEntity<Page<ChiTietSanPhamResponse>> getAll(
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer mauSacId,
            @RequestParam(required = false) Integer kichCoId,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(
                service.getAll(
                        productId,
                        keyword,
                        mauSacId,
                        kichCoId,
                        trangThai,
                        minPrice,
                        maxPrice,
                        pageable
                )
        );
    }

    @GetMapping("/{id}")
    public ChiTietSanPhamResponse getById(@PathVariable Integer id) {
        return service.getById(id);
    }

    /* ================= CREATE ================= */
    // 👉 Nếu sau này muốn chuẩn hơn thì đổi sang Request DTO
    @PostMapping
    public ChiTietSanPhamResponse create(@RequestBody ChiTietSanPhamRequest chiTietSanPham) {
        return service.create(chiTietSanPham);
    }

    /* ================= UPDATE ================= */

    @PutMapping("/{id}")
    public ChiTietSanPhamResponse update(
            @PathVariable Integer id,
            @RequestBody ChiTietSanPhamRequest request
    ) {
        return service.update(id, request);
    }

    /* ================= DELETE ================= */

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Integer id,
            @RequestParam Integer trangThai,
            @RequestParam String nguoiCapNhat
    ) {
        chiTietSanPhamService.changeStatusChiTietSanPham(id, trangThai, nguoiCapNhat);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export-excel")
    public void exportExcel(
            @RequestParam(required = false) List<Integer> ids,
            @RequestParam(required = false) Integer productId,
            HttpServletResponse response
    ) {
        try {
            List<ChiTietSanPham> data = service.getDataExport(ids,productId);

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=chi_tiet_san_pham.xlsx"
            );

            excelService.writeExcel(data, response.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace(); // 👈 BẮT BUỘC
            throw new RuntimeException(e.getMessage());
        }

    }

}

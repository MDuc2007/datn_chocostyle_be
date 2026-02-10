package org.example.chocostyle_datn.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.model.Request.SanPhamRequest;
import org.example.chocostyle_datn.model.Response.SanPhamHomeListResponse;
import org.example.chocostyle_datn.model.Response.SanPhamResponse;
import org.example.chocostyle_datn.service.SanPhamExcelService;
import org.example.chocostyle_datn.service.SanPhamService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
public class SanPhamController {

    private final SanPhamService sanPhamService;
    private final SanPhamExcelService sanPhamExcelService;

//    @GetMapping
//    public List<SanPhamResponse> getAll() {
//        return sanPhamService.getAll();
//    }

    @GetMapping("/{id}")
    public SanPhamResponse getById(@PathVariable Integer id) {
        return sanPhamService.getById(id);
    }

    @PostMapping
    public SanPhamResponse create(@RequestBody SanPhamRequest request) {
        return sanPhamService.create(request);
    }

    @PutMapping("/{id}")
    public SanPhamResponse update(
            @PathVariable Integer id,
            @RequestBody SanPhamRequest request
    ) {
        return sanPhamService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        sanPhamService.delete(id);
    }

    @GetMapping
    public ResponseEntity<Page<SanPhamResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer idChatLieu,
            @RequestParam(required = false) Integer idXuatXu
    ) {
        Page<SanPhamResponse> result = sanPhamService.getSanPham(
                keyword,
                status,
                idChatLieu,
                idXuatXu,
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Order.desc("ngayTao"),
                                Sort.Order.desc("id")
                        )
                )
        );

        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/change-status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Integer id,
            @RequestParam Integer trangThai,
            @RequestParam String nguoiCapNhat
    ) {
        sanPhamService.changeStatusSanPham(id, trangThai, nguoiCapNhat);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/export-excel")
    public void exportSanPhamExcel(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer idChatLieu,
            @RequestParam(required = false) Integer idXuatXu,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=san_pham.xlsx"
        );

        List<SanPhamResponse> data =
                sanPhamService.getAllForExport(
                        keyword, status, idChatLieu, idXuatXu
                );

        sanPhamExcelService.writeExcel(data, response.getOutputStream());
    }
//    @GetMapping("/qr/{qrCode}")
//    public Object scan(@PathVariable String qrCode) {
//        return sanPhamService.scanByQrCode(qrCode);
//    }

    @GetMapping("/home")
    public ResponseEntity<List<SanPhamHomeListResponse>> getDanhSachSanPhamHome() {
        return ResponseEntity.ok(sanPhamService.getDanhSachSanPham());
    }

    // 🔥 Sản phẩm bán chạy
    @GetMapping("/best-seller")
    public ResponseEntity<List<SanPhamHomeListResponse>> getSanPhamBanChay() {
        return ResponseEntity.ok(sanPhamService.getSanPhamBanChay());
    }

}


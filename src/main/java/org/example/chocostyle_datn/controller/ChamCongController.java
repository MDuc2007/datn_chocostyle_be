package org.example.chocostyle_datn.controller;

import org.example.chocostyle_datn.entity.ChamCong;
import org.example.chocostyle_datn.repository.ChamCongRepository;
import org.example.chocostyle_datn.service.ChamCongService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/cham-cong")
@CrossOrigin("*")
public class ChamCongController {

    private final ChamCongService service;
    private final ChamCongRepository chamCongRepository;

    public ChamCongController(ChamCongService service, ChamCongRepository chamCongRepository) {
        this.service = service;
        this.chamCongRepository = chamCongRepository;
    }

    @PostMapping("/check-in/{idNv}")
    public ResponseEntity<?> checkIn(@PathVariable Integer idNv){

        try {
            return ResponseEntity.ok(service.checkIn(idNv));
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Đề xuất sửa Backend (Bạn có thể thêm 1 class DTO hoặc dùng Map)

    @PostMapping("/check-out/{idNv}")
    public ResponseEntity<?> checkOut(
            @PathVariable Integer idNv,
            @RequestBody Map<String, Double> payload // 👉 Hứng cục data JSON từ Frontend
    ) {
        try {
            // Rút tiền từ payload ra
            Double tienMat = payload.getOrDefault("tienMatCuoiCa", 0.0);
            Double tienChuyenKhoan = payload.getOrDefault("tienChuyenKhoanCuoiCa", 0.0);

            // Truyền xuống Service
            return ResponseEntity.ok(service.checkOut(idNv, tienMat, tienChuyenKhoan));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/hom-nay/{idNv}")
    public ResponseEntity<?> getChamCongHomNay(@PathVariable Integer idNv) {
        LocalDate today = LocalDate.now();
        Optional<ChamCong> cc = chamCongRepository
                .findByNhanVien_IdAndNgay(idNv, today);

        return cc.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
    // API LẤY DANH SÁCH GIAO CA KẾT TOÁN
    @GetMapping("/giao-ca")
    public ResponseEntity<?> getDanhSachGiaoCa(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate) {
        try {
            return ResponseEntity.ok(service.getDanhSachGiaoCa(keyword, fromDate, toDate));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
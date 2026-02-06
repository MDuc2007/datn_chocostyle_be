package org.example.chocostyle_datn.controller;


import org.example.chocostyle_datn.entity.LichLamViec;
import org.example.chocostyle_datn.model.Request.LichLamViecRequest;
import org.example.chocostyle_datn.service.LichLamViecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/lich-lam-viec")
@CrossOrigin("*")
public class LichLamViecController {


    @Autowired
    private LichLamViecService service;


    // GET: Cho phép null param để load mặc định
    @GetMapping
    public ResponseEntity<List<LichLamViec>> getSchedules(
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(service.getSchedules(from, to));
    }


    // POST: Tạo mới
    @PostMapping
    public ResponseEntity<?> create(@RequestBody LichLamViecRequest request) {
        try {
            return ResponseEntity.ok(service.createSchedule(request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }


    // PUT: Cập nhật
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody LichLamViecRequest request) {
        try {
            return ResponseEntity.ok(service.updateSchedule(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }


    // DELETE: Xóa
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            service.deleteSchedule(id);
            return ResponseEntity.ok("Đã xóa lịch thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }


    // Wrapper class để trả về JSON lỗi đẹp hơn (Optional)
    static class ErrorResponse {
        public String message;
        public ErrorResponse(String message) { this.message = message; }
    }
}


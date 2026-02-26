package org.example.chocostyle_datn.controller;


import org.example.chocostyle_datn.entity.LichLamViec;
import org.example.chocostyle_datn.model.Request.LichLamViecRequest;
import org.example.chocostyle_datn.repository.LichLamViecRepository;
import org.example.chocostyle_datn.service.LichLamViecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.time.LocalTime;
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

    // POST: Tạo hàng loạt (Cho tính năng lặp lại)
    @PostMapping("/batch")
    public ResponseEntity<?> createBatch(@RequestBody List<LichLamViecRequest> requests) {
        try {
            return ResponseEntity.ok(service.createBatchSchedule(requests));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // PUT: Cập nhật theo chuỗi
    @PutMapping("/series/{maLapLai}")
    public ResponseEntity<?> updateSeries(@PathVariable String maLapLai, @RequestBody LichLamViecRequest request) {
        try {
            service.updateScheduleSeries(maLapLai, request);
            return ResponseEntity.ok("Đã cập nhật chuỗi lịch thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/series/{maLapLai}")
    public ResponseEntity<?> deleteSeries(@PathVariable String maLapLai) {
        try {
            service.deleteScheduleSeries(maLapLai);
            return ResponseEntity.ok("Đã xóa chuỗi lịch thành công (chỉ áp dụng lịch chưa diễn ra)");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // Wrapper class để trả về JSON lỗi đẹp hơn (Optional)
    static class ErrorResponse {
        public String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }


    private final LichLamViecRepository lichLamViecRepository;

    public LichLamViecController(LichLamViecRepository lichLamViecRepository) {
        this.lichLamViecRepository = lichLamViecRepository;
    }

    //    @GetMapping("/check-ca-hom-nay/{idNv}")
//    public ResponseEntity<?> checkCa(@PathVariable Integer idNv){
//
//        LocalDate today = LocalDate.now();
//
//        List<LichLamViec> lich =
//                lichLamViecRepository.checkCaHomNay(idNv, today);
//
//        if(!lich.isEmpty()){
//            return ResponseEntity.ok(lich.get(0));
//        }
//
//        return ResponseEntity.noContent().build();
//    }
    // THÊM API TÌM KIẾM
    @GetMapping("/search")
    public ResponseEntity<Page<LichLamViec>> search(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {

        return ResponseEntity.ok(service.searchLichLamViec(keyword, fromDate, toDate, trangThai, page, size));
    }

    // API: Lấy tất cả lịch của 1 nhân viên
    @GetMapping("/my-schedule/{idNv}")
    public ResponseEntity<List<LichLamViec>> getMyAllSchedules(@PathVariable Integer idNv) {
        return ResponseEntity.ok(service.getMySchedules(idNv));
    }

    // API: Lấy lịch phân trang của 1 nhân viên
    @GetMapping("/my-schedule/{idNv}/search")
    public ResponseEntity<Page<LichLamViec>> searchMySchedules(
            @PathVariable Integer idNv,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size) {
        return ResponseEntity.ok(service.searchMySchedules(idNv, fromDate, toDate, trangThai, page, size));
    }

    @GetMapping("/check-ca-hom-nay/{idNv}")
    public ResponseEntity<?> checkCaHomNay(@PathVariable Integer idNv) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Lấy TẤT CẢ các lịch của nhân viên trong ngày hôm nay
        // (Sử dụng hàm findByNhanVien_IdAndNgayLamViec mà chúng ta đã thêm ở bước trước)
        List<LichLamViec> lichs = lichLamViecRepository.findByNhanVien_IdAndNgayLamViec(idNv, today);

        for (LichLamViec l : lichs) {
            // TRƯỜNG HỢP 1: Ca đang làm (Trạng thái = 3)
            // Ưu tiên trả về ngay lập tức để nhân viên có thể Kết toán (Dù có quá giờ kết thúc ca)
            if (l.getTrangThai() == 3) {
                return ResponseEntity.ok(l);
            }

            // TRƯỜNG HỢP 2: Ca chưa làm (Trạng thái = 2)
            // Chỉ hiển thị Modal nếu hiện tại nằm trong khung giờ (Cho phép mở sớm 30 phút)
            if (l.getTrangThai() == 2) {
                LocalTime start = l.getCaLamViec().getGioBatDau();
                LocalTime end = l.getCaLamViec().getGioKetThuc();

                if (now.isAfter(start.minusMinutes(30)) && now.isBefore(end)) {
                    return ResponseEntity.ok(l);
                }
            }
        }

        // Nếu không có lịch nào khớp (hoặc đã đóng ca xong hết rồi)
        return ResponseEntity.badRequest().body("Hôm nay bạn không có lịch phân công, hoặc hiện tại không nằm trong thời gian ca làm.");
    }



        private final LichLamViecRepository lichLamViecRepository;

        public LichLamViecController(LichLamViecRepository lichLamViecRepository) {
            this.lichLamViecRepository = lichLamViecRepository;
        }

    @GetMapping("/check-ca-hom-nay/{idNv}")
    public ResponseEntity<?> checkCa(@PathVariable Integer idNv){

        LocalDate today = LocalDate.now();

        List<LichLamViec> lich =
                lichLamViecRepository.checkCaHomNay(idNv, today);

        if(!lich.isEmpty()){
            return ResponseEntity.ok(lich.get(0));
        }

        return ResponseEntity.noContent().build();
    }
    }





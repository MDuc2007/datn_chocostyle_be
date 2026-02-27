package org.example.chocostyle_datn.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.HoaDon;
import org.example.chocostyle_datn.entity.PhuongThucThanhToan;
import org.example.chocostyle_datn.entity.ThanhToan;
import org.example.chocostyle_datn.repository.HoaDonRepository;
import org.example.chocostyle_datn.repository.PhuongThucThanhToanRepository;
import org.example.chocostyle_datn.repository.ThanhToanRepository;
import org.example.chocostyle_datn.service.HoaDonService;
import org.example.chocostyle_datn.service.VnpayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/vnpay")
@CrossOrigin("*") // Cho phép Vue gọi API
public class VnpayController {

    @Autowired
    private VnpayService vnpayService;

    @Autowired
    private HoaDonRepository hoaDonRepo;

    @Autowired
    private ThanhToanRepository thanhToanRepo;

    @Autowired
    private PhuongThucThanhToanRepository ptttRepo;

    @PostMapping("/create-payment")
    public ResponseEntity<String> createPayment(@RequestParam Integer hoaDonId) {
        HoaDon hd = hoaDonRepo.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        String paymentUrl = vnpayService.createPaymentUrl(
                hoaDonId,
                hd.getTongTienThanhToan()
        );
        // Trả về chuỗi URL để Vue nhận được
        return ResponseEntity.ok(paymentUrl);
    }

    @GetMapping("/payment-return")
    public void paymentReturn(
            @RequestParam Map<String, String> params,
            HttpServletResponse response) throws IOException {

        // 1. Kiểm tra chữ ký (Checksum) để đảm bảo dữ liệu không bị giả mạo
        boolean valid = vnpayService.validateSignature(params);
        if (!valid) {
            response.sendRedirect("http://localhost:5173/payment-result?status=invalid");
            return;
        }

        // 2. Lấy thông tin từ VNPAY trả về
        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef"); // Đây là hoaDonId
        long vnpAmount = Long.parseLong(params.get("vnp_Amount")) / 100; // VNPAY nhân 100 nên phải chia lại

        Integer hoaDonId = Integer.parseInt(txnRef);
        HoaDon hd = hoaDonRepo.findById(hoaDonId).orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // 3. Kiểm tra logic: Thành công (00) VÀ Số tiền phải khớp
        if ("00".equals(responseCode)) {
            // Kiểm tra số tiền (quan trọng)
            if (vnpAmount != hd.getTongTienThanhToan().longValue()) {
                response.sendRedirect("http://localhost:5173/payment-result?status=fraud"); // Sai tiền
                return;
            }

            // Cập nhật trạng thái thành công
            hd.setTrangThai(0); // 1: Đã thanh toán / Chờ xác nhận
            hd.setNgayThanhToan(LocalDateTime.now());
            hoaDonRepo.save(hd);

            ThanhToan tt = new ThanhToan();
            tt.setIdHoaDon(hd);
            tt.setSoTien(hd.getTongTienThanhToan());
            tt.setTrangThai(1);
            tt.setLoaiGiaoDich(1);
            tt.setMaGiaoDich(params.get("vnp_TransactionNo"));
            tt.setThoiGianThanhToan(LocalDateTime.now());
            PhuongThucThanhToan pttt = ptttRepo.findById(5).orElse(null);
            tt.setIdPttt(pttt);

            thanhToanRepo.save(tt);

            response.sendRedirect("http://localhost:5173/payment-result?status=success");

        } else {
            // Thanh toán thất bại hoặc hủy
            hd.setTrangThai(0); // 0: Hủy hoặc chưa thanh toán
            hoaDonRepo.save(hd);
            response.sendRedirect("http://localhost:5173/payment-result?status=fail");
        }
    }
}
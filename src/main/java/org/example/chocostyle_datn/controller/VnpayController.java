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

    // API: Nhận kết quả trả về từ VNPAY
    @GetMapping("/payment-return")
    public void paymentReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String responseCode = request.getParameter("vnp_ResponseCode");
        String transactionNo = request.getParameter("vnp_TransactionNo"); // Mã giao dịch VNPAY
        String txnRef = request.getParameter("vnp_TxnRef"); // ID Hóa đơn của mình

        // --- SỬA LỖI Ở ĐÂY: Chia 100 để đưa về đúng đơn vị tiền tệ ---
        long vnpAmount = Long.parseLong(request.getParameter("vnp_Amount")) / 100;

        if (txnRef == null) {
            response.sendRedirect("http://localhost:5173/payment-result?status=error");
            return;
        }

        Integer hoaDonId = Integer.parseInt(txnRef);
        HoaDon hd = hoaDonRepo.findById(hoaDonId).orElse(null);

        if (hd == null) {
            response.sendRedirect("http://localhost:5173/payment-result?status=not_found");
            return;
        }

        // 1. Kiểm tra mã phản hồi: 00 là thành công
        if ("00".equals(responseCode)) {

            // 2. Kiểm tra số tiền: Nếu không khớp thì báo lỗi (Tránh gian lận)
            if (vnpAmount != hd.getTongTienThanhToan().longValue()) {
                // Sai tiền -> Chuyển hướng báo lỗi
                response.sendRedirect("http://localhost:5173/payment-result?status=fraud&vnp_ResponseCode=99");
                return;
            }

            // 3. Cập nhật thành công vào Database
            // Chỉ cập nhật nếu đơn chưa hoàn thành để tránh xử lý lặp lại
            if (hd.getTrangThai() == 0) {
                hd.setTrangThai(0); // 1: Đã thanh toán / Chờ xác nhận (Tùy quy ước của bạn)
                hd.setNgayThanhToan(LocalDateTime.now());
                hoaDonRepo.save(hd);

                // Lưu lịch sử thanh toán
                ThanhToan tt = new ThanhToan();
                tt.setIdHoaDon(hd);
                tt.setSoTien(hd.getTongTienThanhToan());
                tt.setTrangThai(1); // Thành công
                tt.setLoaiGiaoDich(1); // 1: Thanh toán
                tt.setMaGiaoDich(transactionNo);
                tt.setThoiGianThanhToan(LocalDateTime.now());

                // Lấy phương thức thanh toán VNPAY (Giả sử ID 2 là VNPAY/Chuyển khoản)
                // Bạn cần thay đổi ID này cho đúng với DB của bạn
                 PhuongThucThanhToan pttt = ptttRepo.findById(5).orElse(null);
                 tt.setIdPttt(pttt);

                thanhToanRepo.save(tt);
            }

            // 4. CHUYỂN HƯỚNG VỀ TRANG KẾT QUẢ (QUAN TRỌNG)
            // Phải kèm hoaDonId để Frontend hiện nút "Xem chi tiết"
            // Phải kèm vnp_ResponseCode=00 để hiện màn hình Xanh (Thành công)
            response.sendRedirect("http://localhost:5173/payment-result?vnp_ResponseCode=00&hoaDonId=" + hd.getId());

        } else {
            // Trường hợp thất bại / Hủy giao dịch
            response.sendRedirect("http://localhost:5173/payment-result?vnp_ResponseCode=" + responseCode);
        }
    }
}
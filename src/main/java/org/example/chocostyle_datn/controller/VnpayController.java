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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                PhuongThucThanhToan pttt = ptttRepo.findById(3).orElse(null);
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

    // ==========================================
    // KHU VỰC MOMO
    // ==========================================
    @PostMapping("/momo/create-payment")
    public ResponseEntity<String> createMomoPayment(@RequestParam Integer hoaDonId) {
        HoaDon hd = hoaDonRepo.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        try {
            String paymentUrl = vnpayService.createMomoUrl(hoaDonId, hd.getTongTienThanhToan());
            return ResponseEntity.ok(paymentUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi tạo mã thanh toán MoMo");
        }
    }

    @GetMapping("/momo/payment-return")
    public void momoReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String resultCode = request.getParameter("resultCode");
        String orderId = request.getParameter("orderId");
        String amountStr = request.getParameter("amount");
        String transId = request.getParameter("transId");

        if (orderId == null || !orderId.contains("_")) {
            response.sendRedirect("http://localhost:5173/payment-result?status=error");
            return;
        }

        Integer hoaDonId = Integer.parseInt(orderId.split("_")[1]);
        HoaDon hd = hoaDonRepo.findById(hoaDonId).orElse(null);

        if (hd != null && "0".equals(resultCode)) {
            // ... (Giữ nguyên logic cập nhật thành công MoMo như cũ) ...
            long amount = Long.parseLong(amountStr);
            if (amount == hd.getTongTienThanhToan().longValue() && hd.getTrangThai() == 0) {
                hd.setTrangThai(0);
                hd.setNgayThanhToan(LocalDateTime.now());
                hoaDonRepo.save(hd);

                ThanhToan tt = new ThanhToan();
                tt.setIdHoaDon(hd);
                tt.setSoTien(hd.getTongTienThanhToan());
                tt.setTrangThai(1);
                tt.setLoaiGiaoDich(1);
                tt.setMaGiaoDich(transId);
                tt.setThoiGianThanhToan(LocalDateTime.now());

                PhuongThucThanhToan pttt = ptttRepo.findById(4).orElse(null);
                tt.setIdPttt(pttt);
                thanhToanRepo.save(tt);
            }
            response.sendRedirect("http://localhost:5173/payment-result?method=MOMO&status=success&hoaDonId=" + hd.getId());
        } else {
            // === THÊM LOGIC XÓA ĐƠN HÀNG KHI HỦY MOMO ===
            if (hd != null && hd.getTrangThai() == 0) {
                try {
                    hoaDonRepo.delete(hd);
                } catch (Exception e) {
                    System.out.println("Lỗi khi xóa đơn nháp (MoMo): " + e.getMessage());
                }
            }
            response.sendRedirect("http://localhost:5173/payment-result?method=MOMO&status=error");
        }
    }

    // ==========================================
    // KHU VỰC VIETQR (WEBHOOK NHẬN TIỀN TỰ ĐỘNG TỪ SEPAY)
    // ==========================================
    @PostMapping("/vietqr-webhook")
    public ResponseEntity<String> sepayWebhook(@RequestBody Map<String, Object> payload) {
        try {
            String content = (String) payload.get("content");
            String transferAmountStr = String.valueOf(payload.get("transferAmount"));
            long amount = Double.valueOf(transferAmountStr).longValue();

            if (content != null && content.toUpperCase().contains("HD")) {
                // Dùng Regex để tìm cụm "HD" đi kèm với các chữ số liền sau nó
                Pattern pattern = Pattern.compile("HD(\\d+)");
                Matcher matcher = pattern.matcher(content.toUpperCase());

                if (matcher.find()) {
                    // matcher.group(1) sẽ lấy chính xác các số nằm ngay sau HD.
                    // Ví dụ: Với HD133-CHUYEN..., group(1) sẽ trả về đúng "133"
                    String idStr = matcher.group(1);
                    Integer hoaDonId = Integer.parseInt(idStr);

                    HoaDon hd = hoaDonRepo.findById(hoaDonId).orElse(null);
                    if (hd != null && hd.getTrangThai() == 0 && amount >= hd.getTongTienThanhToan().longValue()) {
                        hd.setTrangThai(0);
                        hd.setNgayThanhToan(LocalDateTime.now());
                        hoaDonRepo.save(hd);

                        ThanhToan tt = new ThanhToan();
                        tt.setIdHoaDon(hd);
                        tt.setSoTien(BigDecimal.valueOf(amount));
                        tt.setTrangThai(1);
                        tt.setLoaiGiaoDich(1);
                        tt.setMaGiaoDich("VIETQR_" + payload.get("referenceCode"));
                        tt.setThoiGianThanhToan(LocalDateTime.now());

                        PhuongThucThanhToan pttt = ptttRepo.findById(5).orElse(null);
                        tt.setIdPttt(pttt);
                        thanhToanRepo.save(tt);
                    }
                }
            }
            return ResponseEntity.ok("Received");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing webhook");
        }
    }
    @PostMapping("/zalopay/create-payment")
    public ResponseEntity<String> createZaloPayPayment(@RequestParam Integer hoaDonId) {
        HoaDon hd = hoaDonRepo.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        try {
            String paymentUrl = vnpayService.createZaloPayUrl(hoaDonId, hd.getTongTienThanhToan());
            return ResponseEntity.ok(paymentUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi tạo mã thanh toán ZaloPay");
        }
    }

    @GetMapping("/zalopay/payment-return")
    public void zaloPayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String status = request.getParameter("status"); // 1 = Thành công, các mã khác là thất bại/hủy
        String appTransId = request.getParameter("apptransid");
        String hoaDonIdStr = request.getParameter("hoaDonId");

        if (hoaDonIdStr == null) {
            response.sendRedirect("http://localhost:5173/payment-result?status=error");
            return;
        }

        Integer hoaDonId = Integer.parseInt(hoaDonIdStr);
        HoaDon hd = hoaDonRepo.findById(hoaDonId).orElse(null);

        // ZaloPay trả về status = 1 là thanh toán thành công
        if (hd != null && "1".equals(status)) {
            if (hd.getTrangThai() == 0) {
                hd.setTrangThai(0); // Đã thanh toán / Chờ xác nhận
                hd.setNgayThanhToan(LocalDateTime.now());
                hoaDonRepo.save(hd);

                ThanhToan tt = new ThanhToan();
                tt.setIdHoaDon(hd);
                tt.setSoTien(hd.getTongTienThanhToan());
                tt.setTrangThai(1);
                tt.setLoaiGiaoDich(1);
                tt.setMaGiaoDich(appTransId);
                tt.setThoiGianThanhToan(LocalDateTime.now());

                // LƯU Ý: Đảm bảo trong bảng phuong_thuc_thanh_toan bạn đã tạo 1 dòng cho ZaloPay (ví dụ ID = 6)
                PhuongThucThanhToan pttt = ptttRepo.findById(6).orElse(null);
                tt.setIdPttt(pttt);
                thanhToanRepo.save(tt);
            }
            response.sendRedirect("http://localhost:5173/payment-result?method=ZALOPAY&status=success&hoaDonId=" + hd.getId());
        } else {
            // Khách hàng hủy hoặc thanh toán lỗi -> Xóa đơn nháp
            if (hd != null && hd.getTrangThai() == 0) {
                try {
                    hoaDonRepo.delete(hd);
                } catch (Exception e) {
                    System.out.println("Lỗi khi xóa đơn nháp (ZaloPay): " + e.getMessage());
                }
            }
            response.sendRedirect("http://localhost:5173/payment-result?method=ZALOPAY&status=error");
        }
    }
}
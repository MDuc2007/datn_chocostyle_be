package org.example.chocostyle_datn.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.example.chocostyle_datn.entity.KhachHang;
import org.example.chocostyle_datn.entity.PhieuGiamGia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Async
    public void sendAccountInfo(String toEmail, String hoTen, String username, String password) {
        // Chủ đề email
        String subject = "Chào mừng bạn đến với ChocoStyle — Thông tin đăng nhập";

        // Nội dung HTML (Giống mẫu bạn gửi)
        String htmlContent = String.format("""
            <div style="font-family: Arial, sans-serif; color: #333; max-width: 600px;">
                <h2 style="color: #444;">Xin chào %s,</h2>
                <p>Chào mừng bạn đã gia nhập đội ngũ <b>ChocoStyle</b>. Dưới đây là thông tin đăng nhập của bạn:</p>
                <ul style="background-color: #f9f9f9; padding: 15px; border-radius: 5px; list-style-type: none;">
                    <li style="margin-bottom: 10px;">
                        <strong>📛 Tên đăng nhập:</strong> <span style="color: #63391F;">%s</span>
                    </li>
                    <li>
                        <strong>🔑 Mật khẩu:</strong> <span style="color: #63391F;">%s</span>
                    </li>
                </ul>
                <p>Vui lòng đổi mật khẩu sau khi đăng nhập lần đầu để bảo mật tài khoản.</p>
                <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
                <p>Trân trọng,<br><b>ChocoStyle Team</b></p>
            </div>
            """, hoTen, username, password);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = gửi dưới dạng HTML

            mailSender.send(message);
            System.out.println("Gửi mail thành công cho: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Lỗi gửi mail: " + e.getMessage());
        }
    }
    public void sendVoucherEmail(KhachHang kh, PhieuGiamGia pgg) {

        if (kh.getEmail() == null || kh.getEmail().isBlank()) return;

        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(kh.getEmail());
            helper.setSubject("🎁 Ưu đãi dành riêng cho bạn - Mã giảm giá " + pgg.getMaPgg());

            String html = buildHtmlContent(kh, pgg);
            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
    private String buildHtmlContent(KhachHang kh, PhieuGiamGia pgg) {

        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        return """
            <div style="font-family: Arial, sans-serif; background:#f5f5f5; padding:30px">
              <div style="max-width:600px; margin:auto; background:#ffffff; border-radius:8px; overflow:hidden">
                
                <div style="background:#5a2d0c; color:white; padding:16px; text-align:center; font-size:20px; font-weight:bold">
                  🎁 Ưu Đãi Đặc Biệt Dành Cho Bạn
                </div>

                <div style="padding:20px; color:#333">
                  <p>Xin chào <b>%s</b>,</p>

                  <p>
                    Chúng tôi gửi tặng bạn một mã <b>phiếu giảm giá đặc biệt</b>.
                    Hãy sử dụng ngay để nhận ưu đãi hấp dẫn!
                  </p>

                  <table style="width:100%%; border-collapse:collapse; margin-top:15px">
                                <tr style="background:#f5f5f5">
                                  <td style="padding:10px; width:40%%">Mã Voucher</td>
                                  <td style="padding:10px; font-weight:bold; color:#ff7a00">%s</td>
                                </tr>
                                <tr style="background:#ffffff">
                                  <td style="padding:10px">Giá trị giảm</td>
                                  <td style="padding:10px">%s</td>
                                </tr>
                                <tr style="background:#f5f5f5">
                                  <td style="padding:10px">Giảm tối đa</td>
                                  <td style="padding:10px">%s</td>
                                </tr>
                                <tr style="background:#ffffff">
                                  <td style="padding:10px">Thời gian áp dụng</td>
                                  <td style="padding:10px">Từ %s Đến %s</td>
                                </tr>
                                <tr style="background:#f5f5f5">
                                  <td style="padding:10px">Điều kiện</td>
                                  <td style="padding:10px">Đơn hàng từ %s</td>
                                </tr>
                              </table>

                  <p style="margin-top:20px">
                    Chúc bạn mua sắm vui vẻ! <br/>
                    <b>ChocoStyle</b>
                  </p>
                </div>
              </div>
            </div>
            """.formatted(
                kh.getTenKhachHang(),
                pgg.getMaPgg(),
                formatGiaTri(pgg),
                pgg.getGiaTriToiDa() != null ? formatMoney(pgg.getGiaTriToiDa()) : "Không giới hạn",
                pgg.getNgayBatDau().format(df),
                pgg.getNgayKetThuc().format(df),
                formatMoney(pgg.getDieuKienDonHang())
        );
    }

    private String formatGiaTri(PhieuGiamGia pgg) {
        if ("PERCENT".equals(pgg.getLoaiGiam())) {
            return pgg.getGiaTri() + "%";
        }
        return formatMoney(pgg.getGiaTri());
    }

    private String formatMoney(Object value) {
        return value + " VND";
    }


}
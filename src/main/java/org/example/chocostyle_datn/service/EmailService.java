package org.example.chocostyle_datn.service;




import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.example.chocostyle_datn.entity.HoaDon;
import org.example.chocostyle_datn.entity.KhachHang;
import org.example.chocostyle_datn.entity.PhieuGiamGia;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;




import java.time.format.DateTimeFormatter;




@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private JavaMailSender javaMailSender;

    @Async // Chạy ngầm để không làm khách phải chờ lâu khi đặt hàng
    public void sendOrderConfirmation(String toEmail, HoaDon hoaDon) {
        if (toEmail == null || toEmail.isEmpty()) return;

        String subject = "ChocoStyle - Xác nhận đơn hàng #" + hoaDon.getMaHoaDon();
        // Link tra cứu (Thay đổi port Frontend của bạn, ví dụ localhost:5173)
        String trackingLink = "http://localhost:5173/tra-cuu?code=" + hoaDon.getMaHoaDon();

        String htmlContent = "<html><body>"
                + "<h2>Cảm ơn bạn đã đặt hàng tại ChocoStyle!</h2>"
                + "<p>Xin chào <b>" + hoaDon.getTenKhachHang() + "</b>,</p>"
                + "<p>Đơn hàng của bạn đã được ghi nhận thành công.</p>"
                + "<h3>Thông tin đơn hàng:</h3>"
                + "<ul>"
                + "<li><b>Mã đơn hàng:</b> " + hoaDon.getMaHoaDon() + "</li>"
                + "<li><b>Sản phẩm mua:</b> (Xem chi tiết trong link tra cứu)</li>"
                + "<li><b>Tổng thanh toán:</b> " + String.format("%,.0f", hoaDon.getTongTienThanhToan()) + " đ</li>"
                + "<li><b>Địa chỉ giao:</b> " + hoaDon.getDiaChiKhachHang() + "</li>"
                + "</ul>"
                + "<p>Bạn có thể theo dõi trạng thái đơn hàng tại đường dẫn sau:</p>"
                + "<a href='" + trackingLink + "' style='background-color: #6b3f1e; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>TRA CỨU ĐƠN HÀNG</a>"
                + "<p>Hoặc truy cập link: " + trackingLink + "</p>"
                + "<br><p>Trân trọng,<br>Đội ngũ ChocoStyle</p>"
                + "</body></html>";

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            javaMailSender.send(message);
            System.out.println("Đã gửi mail xác nhận đơn hàng tới: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Lỗi gửi mail: " + e.getMessage());
        }
    }

    @Async
    public void sendAccountInfo(String toEmail, String hoTen, String username, String password) {
        // Chủ đề email
        String subject = "Chào mừng bạn đến với ChocoStyle — Thông tin đăng nhập";

        // Nội dung HTML đã thay thế username bằng toEmail và thêm ghi chú rõ ràng
        String htmlContent = String.format("""
          <div style="font-family: Arial, sans-serif; color: #333; max-width: 600px;">
              <h2 style="color: #444;">Xin chào %s,</h2>
              <p>Chào mừng bạn đã gia nhập đội ngũ <b>ChocoStyle</b>. Dưới đây là thông tin đăng nhập của bạn:</p>
              <ul style="background-color: #f9f9f9; padding: 15px; border-radius: 5px; list-style-type: none;">
                  <li style="margin-bottom: 10px;">
                      <strong>📧 Tài khoản đăng nhập:</strong> <span style="color: #63391F;">%s</span> <br/>
                      <i style="font-size: 13px; color: #666;">(Vui lòng sử dụng chính email này để đăng nhập vào hệ thống)</i>
                  </li>
                  <li style="margin-top: 10px;">
                      <strong>🔑 Mật khẩu:</strong> <span style="color: #63391F;">%s</span>
                  </li>
              </ul>
              <p>Vui lòng đổi mật khẩu sau khi đăng nhập lần đầu để bảo mật tài khoản.</p>
              <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
              <p>Trân trọng,<br><b>ChocoStyle Team</b></p>
          </div>
          """, hoTen, toEmail, password);

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




    @Async
    public void sendVoucherCreatedEmail(KhachHang kh, PhieuGiamGia pgg) {
        sendVoucherMail(
                kh,
                pgg,
                "🎁 Bạn nhận được phiếu giảm giá mới - " + pgg.getMaPgg(),
                "Chúng tôi gửi tặng bạn một mã <b>phiếu giảm giá đặc biệt</b>."
        );
    }




    @Async
    public void sendVoucherUpdatedEmail(KhachHang kh, PhieuGiamGia pgg) {
        sendVoucherMail(
                kh,
                pgg,
                "🔔 Phiếu giảm giá của bạn đã được cập nhật - " + pgg.getMaPgg(),
                "Phiếu giảm giá bạn đang sở hữu <b>đã được cập nhật thông tin mới</b>."
        );
    }




    private void sendVoucherMail(
            KhachHang kh,
            PhieuGiamGia pgg,
            String subject,
            String introText
    ) {




        if (kh.getEmail() == null || kh.getEmail().isBlank()) return;




        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");




            helper.setTo(kh.getEmail());
            helper.setSubject(subject);




            String html = buildHtmlContent(kh, pgg, introText);
            helper.setText(html, true);




            mailSender.send(message);




        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }




    private String buildHtmlContent(
            KhachHang kh,
            PhieuGiamGia pgg,
            String introText
    ) {




        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");




        return """
          <div style="font-family: Arial, sans-serif; background:#f5f5f5; padding:30px">
            <div style="max-width:600px; margin:auto; background:#ffffff; border-radius:8px; overflow:hidden">
            
              <div style="background:#5a2d0c; color:white; padding:16px; text-align:center; font-size:20px; font-weight:bold">
                🎁 Ưu Đãi Đặc Biệt Dành Cho Bạn
              </div>




              <div style="padding:20px; color:#333">
                <p>Xin chào <b>%s</b>,</p>




                <p>%s</p>




                <table style="width:100%%; border-collapse:collapse; margin-top:15px">
                  <tr style="background:#f5f5f5">
                    <td style="padding:10px; width:40%%">Mã Voucher</td>
                    <td style="padding:10px; font-weight:bold; color:#ff7a00">%s</td>
                  </tr>
                  <tr>
                    <td style="padding:10px">Giá trị giảm</td>
                    <td style="padding:10px">%s</td>
                  </tr>
                  <tr style="background:#f5f5f5">
                    <td style="padding:10px">Giảm tối đa</td>
                    <td style="padding:10px">%s</td>
                  </tr>
                  <tr>
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
                introText,
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




    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }


    public void sendAccountInfo(String toEmail, String username, String password) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("[ChocoStyle] Thông báo khởi tạo tài khoản thành công");


            // Sử dụng String.format để dễ nhìn và dễ sửa nội dung sau này
            String content = String.format(
                    "Xin chào,\n\n" +
                            "Chào mừng bạn đến với ChocoStyle! Tài khoản của bạn đã được khởi tạo thành công trên hệ thống.\n\n" +
                            "--------------------------------------------------\n" +
                            "THÔNG TIN ĐĂNG NHẬP CỦA BẠN:\n" +
                            "📧 Email/Tài khoản: %s\n" +
                            "🔑 Mật khẩu:       %s\n" +
                            "--------------------------------------------------\n\n" +
                            "⚠️ LƯU Ý BẢO MẬT:\n" +
                            "Vui lòng đăng nhập và thay đổi mật khẩu ngay trong lần truy cập đầu tiên để bảo vệ tài khoản.\n\n" +
                            "Nếu cần hỗ trợ, vui lòng liên hệ với chúng tôi.\n\n" +
                            "Trân trọng,\n" +
                            "Đội ngũ ChocoStyle",
                    toEmail,  // Tham số thứ 1 (%s đầu tiên)
                    password   // Tham số thứ 2 (%s thứ hai)
            );


            message.setText(content);


            mailSender.send(message);
            System.out.println("Gửi mail thành công đến: " + toEmail);


        } catch (Exception e) {
            // Log lỗi để biết nếu mail không gửi được (quan trọng)
            System.err.println("Lỗi khi gửi email: " + e.getMessage());
            e.printStackTrace();
        }
    }

}

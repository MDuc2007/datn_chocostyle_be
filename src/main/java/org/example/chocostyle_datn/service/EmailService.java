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
import org.springframework.scheduling.annotation.Async;


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

        // Sử dụng String.format kết hợp Text Block để làm giao diện HTML đẹp mắt hơn
        String htmlContent = String.format("""
                <div style="font-family: 'Segoe UI', Arial, sans-serif; background-color: #f5f5f5; padding: 40px 10px;">
                    <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.05);">
                        
                        <div style="background-color: #5a2d0c; color: #ffffff; padding: 25px 20px; text-align: center;">
                            <h2 style="margin: 0; font-size: 24px; font-weight: bold;">XÁC NHẬN ĐƠN HÀNG</h2>
                            <p style="margin: 8px 0 0; font-size: 15px; color: #f0e6d2;">Cảm ơn bạn đã tin tưởng và lựa chọn ChocoStyle!</p>
                        </div>
                        
                        <div style="padding: 30px; color: #333333; line-height: 1.6;">
                            <p style="font-size: 16px;">Xin chào <b style="color: #5a2d0c;">%s</b>,</p>
                            <p style="font-size: 15px;">Tuyệt vời! Đơn hàng của bạn đã được hệ thống ghi nhận thành công và đang trong quá trình chuẩn bị.</p>
                            
                            <h3 style="color: #5a2d0c; border-bottom: 2px solid #f0f0f0; padding-bottom: 10px; margin-top: 35px; font-size: 18px;">🛒 Chi tiết đơn hàng</h3>
                            
                            <table style="width: 100%%; border-collapse: collapse; margin-top: 15px;">
                                <tr>
                                    <td style="padding: 12px 0; border-bottom: 1px dashed #e0e0e0; color: #666; width: 40%%;">Mã đơn hàng:</td>
                                    <td style="padding: 12px 0; border-bottom: 1px dashed #e0e0e0; font-weight: bold; text-align: right; color: #333;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 12px 0; border-bottom: 1px dashed #e0e0e0; color: #666;">Sản phẩm mua:</td>
                                    <td style="padding: 12px 0; border-bottom: 1px dashed #e0e0e0; text-align: right; color: #888; font-style: italic;">(Xem chi tiết trong link tra cứu)</td>
                                </tr>
                                <tr>
                                    <td style="padding: 12px 0; border-bottom: 1px dashed #e0e0e0; color: #666;">Tổng thanh toán:</td>
                                    <td style="padding: 12px 0; border-bottom: 1px dashed #e0e0e0; font-weight: bold; color: #e74c3c; text-align: right; font-size: 16px;">%s đ</td>
                                </tr>
                                <tr>
                                    <td style="padding: 12px 0; border-bottom: 1px dashed #e0e0e0; color: #666; vertical-align: top;">Địa chỉ giao hàng:</td>
                                    <td style="padding: 12px 0; border-bottom: 1px dashed #e0e0e0; text-align: right; color: #333; line-height: 1.5;">%s</td>
                                </tr>
                            </table>
                            
                            <div style="text-align: center; margin-top: 40px;">
                                <p style="margin-bottom: 20px; font-size: 15px;">Bạn có thể theo dõi tiến độ đơn hàng bất cứ lúc nào tại đây:</p>
                                <a href="%s" style="background-color: #6b3f1e; color: #ffffff; padding: 14px 35px; text-decoration: none; border-radius: 6px; font-weight: bold; font-size: 15px; display: inline-block; text-transform: uppercase; box-shadow: 0 3px 6px rgba(107, 63, 30, 0.3);">TRA CỨU ĐƠN HÀNG</a>
                                <p style="margin-top: 25px; font-size: 13px; color: #888;">Nếu nút không hoạt động, vui lòng truy cập link:<br><a href="%s" style="color: #6b3f1e; word-break: break-all;">%s</a></p>
                            </div>
                        </div>
                        
                        <div style="background-color: #f9f9f9; padding: 25px 20px; text-align: center; border-top: 1px solid #eeeeee;">
                            <p style="margin: 0; color: #777; font-size: 14px;">Mọi thắc mắc xin vui lòng liên hệ với chúng tôi.</p>
                            <p style="margin: 10px 0 0; color: #777; font-size: 14px;">Trân trọng,</p>
                            <p style="margin: 5px 0 0; font-weight: bold; color: #5a2d0c; font-size: 16px;">Đội ngũ ChocoStyle</p>
                        </div>
                    </div>
                </div>
                """,
                hoaDon.getTenKhachHang(),
                hoaDon.getMaHoaDon(),
                String.format("%,.0f", hoaDon.getTongTienThanhToan()),
                hoaDon.getDiaChiKhachHang(),
                trackingLink,
                trackingLink,
                trackingLink
        );

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

    @Async
    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    @Async
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

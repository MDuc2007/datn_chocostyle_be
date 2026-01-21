package org.example.chocostyle_datn.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
}
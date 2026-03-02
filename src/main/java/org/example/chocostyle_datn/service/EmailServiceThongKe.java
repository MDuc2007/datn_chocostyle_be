package org.example.chocostyle_datn.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailServiceThongKe {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    // Thêm tham số kieuBaoCao (Ví dụ: "Hôm nay", "Tuần này", "Tháng này"...)
    public void guiMailHtml(String toEmail, String subject, Object dataTongQuan, String kieuBaoCao) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("hethong.chocostyle@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject(subject);

            Context context = new Context();
            context.setVariable("dataBaoCao", dataTongQuan);
            context.setVariable("kieuBaoCao", kieuBaoCao); // Truyền sang giao diện Email

            String html = templateEngine.process("bao-cao-template", context);
            helper.setText(html, true);

            mailSender.send(message);
            System.out.println("✅ Đã gửi email báo cáo [" + kieuBaoCao + "] tới: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Lỗi gửi mail: " + e.getMessage());
        }
    }
}
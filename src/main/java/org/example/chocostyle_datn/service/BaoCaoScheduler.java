package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.CauHinhHeThong;
import org.example.chocostyle_datn.model.Response.TongQuatResponse;
import org.example.chocostyle_datn.repository.CauHinhHeThongRepository;
import org.example.chocostyle_datn.service.EmailService;
import org.example.chocostyle_datn.service.EmailServiceThongKe;
import org.example.chocostyle_datn.service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class BaoCaoScheduler {

    @Autowired
    private ThongKeService thongKeService;

    @Autowired
    private EmailServiceThongKe emailService;

    @Autowired
    private CauHinhHeThongRepository cauHinhRepo;

    // --- HÀM XỬ LÝ CHUNG TRÁNH LẶP CODE ---
    private void xuLyGuiMailTudong(String loaiBaoCaoStr, TongQuatResponse data) {
        CauHinhHeThong config = cauHinhRepo.findById(1).orElse(null);
        if (config == null || config.getEmailNhan() == null || config.getEmailNhan().isEmpty()) return;

        boolean canGui = false;
        String subject = "";
        String kieuBaoCao = "";

        // Kiểm tra công tắc cấu hình
        if (loaiBaoCaoStr.equals("NGAY") && config.getGuiNgay()) {
            canGui = true; subject = "📊 Báo Cáo Doanh Thu Ngày - ChocoStyle"; kieuBaoCao = "hôm nay";
        } else if (loaiBaoCaoStr.equals("TUAN") && config.getGuiTuan()) {
            canGui = true; subject = "📊 Báo Cáo Doanh Thu Tuần - ChocoStyle"; kieuBaoCao = "tuần này";
        } else if (loaiBaoCaoStr.equals("THANG") && config.getGuiThang()) {
            canGui = true; subject = "📊 Báo Cáo Doanh Thu Tháng - ChocoStyle"; kieuBaoCao = "tháng này";
        } else if (loaiBaoCaoStr.equals("NAM") && config.getGuiNam()) {
            canGui = true; subject = "📊 Báo Cáo Doanh Thu Năm - ChocoStyle"; kieuBaoCao = "năm nay";
        }

        if (canGui) {
            System.out.println("⏳ Bắt đầu gửi báo cáo tự động (" + loaiBaoCaoStr + ")...");
            emailService.guiMailHtml(config.getEmailNhan(), subject, data, kieuBaoCao);
        }
    }

    // 1. CHẠY VÀO 20:00 HÀNG NGÀY
    @Scheduled(cron = "0 0 20 * * ?", zone = "Asia/Ho_Chi_Minh")
    public void guiBaoCaoNgay() {
        xuLyGuiMailTudong("NGAY", thongKeService.getDuLieuTongQuan().get("homNay"));
    }

    // 2. CHẠY VÀO 20:00 CHỦ NHẬT HÀNG TUẦN
    @Scheduled(cron = "0 0 20 * * ?", zone = "Asia/Ho_Chi_Minh")
    public void guiBaoCaoTuan() {
        xuLyGuiMailTudong("TUAN", thongKeService.getDuLieuTongQuan().get("tuanNay"));
    }

    // 3. CHẠY VÀO 20:00 NGÀY CUỐI CÙNG CỦA THÁNG
    @Scheduled(cron = "0 0 20 * * ?", zone = "Asia/Ho_Chi_Minh")
    public void guiBaoCaoThang() {
        xuLyGuiMailTudong("THANG", thongKeService.getDuLieuTongQuan().get("thangNay"));
    }

    // 4. CHẠY VÀO 20:00 NGÀY 31/12 (CUỐI NĂM)
    @Scheduled(cron = "0 0 20 * * ?", zone = "Asia/Ho_Chi_Minh")
    public void guiBaoCaoNam() {
        xuLyGuiMailTudong("NAM", thongKeService.getDuLieuTongQuan().get("namNay"));
    }

    /* * ===============================================
     * LƯU Ý KHI TEST TẠI NHÀ HOẶC LÚC BẢO VỆ ĐỒ ÁN:
     * Bạn có thể mở comment hàm dưới đây ra để nó chạy MỖI 1 PHÚT 1 LẦN
     * Nó sẽ đóng vai trò như báo cáo Ngày để bạn test cho nhanh.
     * ===============================================
     */
    // @Scheduled(cron = "0 * * * * ?")
    // public void testBaoCaoNhanh() {
    //     xuLyGuiMailTudong("NGAY", thongKeService.getDuLieuTongQuan().get("homNay"));
    // }
}
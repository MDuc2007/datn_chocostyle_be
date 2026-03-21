
package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.ThongBao;
import org.example.chocostyle_datn.repository.ThongBaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ThongBaoService {

    @Autowired
    private ThongBaoRepository thongBaoRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void thongBaoDonHangMoi(Integer orderId){

        ThongBao tb = new ThongBao();

        tb.setTieuDe("Đơn hàng mới");
        tb.setNoiDung("Có đơn #" + orderId);
        tb.setLoaiThongBao("ORDER_NEW");
        tb.setOrderId(orderId);
        tb.setDaDoc(false);
        tb.setNgayTao(LocalDateTime.now());

        ThongBao saved = thongBaoRepository.save(tb);
        messagingTemplate.convertAndSend("/topic/notification", saved);
    }

    public void thongBaoHuyDon(Integer orderId){

        ThongBao tb = new ThongBao();

        tb.setTieuDe("Đơn hàng bị hủy");
        tb.setNoiDung("Đơn #" + orderId + " đã bị hủy");
        tb.setLoaiThongBao("ORDER_CANCEL");
        tb.setOrderId(orderId);
        tb.setDaDoc(false);
        tb.setNgayTao(LocalDateTime.now());

        ThongBao saved = thongBaoRepository.save(tb);
        messagingTemplate.convertAndSend("/topic/notification", saved);
    }

    public void thongBaoThanhToan(Integer orderId){

        ThongBao tb = new ThongBao();

        tb.setTieuDe("Thanh toán thành công");
        tb.setNoiDung("Đơn #" + orderId + " đã thanh toán");
        tb.setLoaiThongBao("PAYMENT_SUCCESS");
        tb.setOrderId(orderId);
        tb.setDaDoc(false);
        tb.setNgayTao(LocalDateTime.now());

        ThongBao saved = thongBaoRepository.save(tb);
        messagingTemplate.convertAndSend("/topic/notification", saved);
    }

    public void thongBaoSupportRequest(Integer khachHangId, Integer conversationId){
        ThongBao tb = new ThongBao();

        tb.setTieuDe("Khách cần hỗ trợ");
        tb.setNoiDung("Khách #" + khachHangId + " cần hỗ trợ (Chat #" + conversationId + ")");
        tb.setLoaiThongBao("SUPPORT_REQUEST");
        tb.setDaDoc(false);
        tb.setNgayTao(LocalDateTime.now());

        ThongBao saved = thongBaoRepository.save(tb);

        messagingTemplate.convertAndSend("/topic/notification", saved);
    }

}

package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.*;
import org.example.chocostyle_datn.model.Request.ChatMessageRequest;
import org.example.chocostyle_datn.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {
    private final MessageRepository msgRepo;
    private final ConversationRepository convRepo;
    private final NhanVienRepository nhanVienRepo;
    private final KhachHangRepository khachHangRepo;

    public ChatService(MessageRepository msgRepo,
                       ConversationRepository convRepo,
                       NhanVienRepository nhanVienRepo,
                       KhachHangRepository khachHangRepo) {
        this.msgRepo = msgRepo;
        this.convRepo = convRepo;
        this.nhanVienRepo = nhanVienRepo;
        this.khachHangRepo = khachHangRepo;
    }

    @Transactional
    public Message saveIncomingMessage(ChatMessageRequest req) {
        // Tìm cuộc hội thoại
        Conversation conv = convRepo.findById(req.getConversationId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hội thoại"));

        // Tạo thực thể Message từ Request
        Message msg = Message.builder()
                .conversation(conv)
                .senderId(req.getSenderId())
                .senderType(req.getSenderType()) // "KHACH_HANG" hoặc "NHAN_VIEN"
                .content(req.getContent())
                .build();

        return msgRepo.save(msg);
    }

    // Hàm bổ trợ để lấy tên hiển thị của người gửi
    public String getSenderName(Integer senderId, String senderType) {
        if ("KHACH_HANG".equals(senderType)) {
            return khachHangRepo.findById(senderId)
                    .map(KhachHang::getTenKhachHang)
                    .orElse("Khách hàng ẩn danh");
        } else {
            return nhanVienRepo.findById(senderId)
                    .map(NhanVien::getHoTen)
                    .orElse("Nhân viên hỗ trợ");
        }
    }
    @Transactional
    public Conversation getOrCreateConversationForCustomer(Integer khachHangId) {
        KhachHang kh = khachHangRepo.findById(khachHangId)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        // 1. Tìm nhân viên đang trực ca
        NhanVien nvToAssign = nhanVienRepo.findNhanVienDangTrongCa()
                .orElseGet(() -> nhanVienRepo.findNhanVienDuPhong());

        // 2. Tìm cuộc hội thoại hiện có giữa khách này và nhân viên được gán
        return convRepo.findByKhachHangAndNhanVien(kh, nvToAssign)
                .orElseGet(() -> {
                    Conversation newConv = Conversation.builder()
                            .khachHang(kh)
                            .nhanVien(nvToAssign)
                            .build();
                    return convRepo.save(newConv);
                });
    }
}
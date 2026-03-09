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
    private final GeminiService geminiService; // thêm AI

    public ChatService(MessageRepository msgRepo,
                       ConversationRepository convRepo,
                       NhanVienRepository nhanVienRepo,
                       KhachHangRepository khachHangRepo,
                       GeminiService geminiService) {

        this.msgRepo = msgRepo;
        this.convRepo = convRepo;
        this.nhanVienRepo = nhanVienRepo;
        this.khachHangRepo = khachHangRepo;
        this.geminiService = geminiService; // inject
    }

    @Transactional
    public Message saveIncomingMessage(ChatMessageRequest req) {

        Conversation conv = convRepo.findById(req.getConversationId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hội thoại"));

        Message msg = Message.builder()
                .conversation(conv)
                .senderId(req.getSenderId())
                .senderType(req.getSenderType())
                .content(req.getContent())
                .build();

        Message savedMsg = msgRepo.save(msg);

        // Nếu khách hàng nhắn → AI trả lời
        if ("KHACH_HANG".equals(req.getSenderType())) {
            Integer khachHangId = conv.getKhachHang().getId();
            String aiReply = geminiService.hoiGemini(req.getContent(), khachHangId);

            Message botMsg = Message.builder()
                    .conversation(conv)
                    .senderId(0)          // BOT
                    .senderType("BOT")
                    .content(aiReply)
                    .build();

            msgRepo.save(botMsg);
        }

        return savedMsg;
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

        NhanVien nvToAssign = nhanVienRepo.findNhanVienDangTrongCa()
                .orElseGet(() -> nhanVienRepo.findNhanVienDuPhong());

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
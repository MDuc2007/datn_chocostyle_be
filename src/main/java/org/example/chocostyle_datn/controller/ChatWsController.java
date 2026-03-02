package org.example.chocostyle_datn.controller;

import org.example.chocostyle_datn.entity.Conversation;
import org.example.chocostyle_datn.entity.KhachHang;
import org.example.chocostyle_datn.entity.Message;
import org.example.chocostyle_datn.entity.NhanVien;
import org.example.chocostyle_datn.model.Request.ChatMessageRequest;
import org.example.chocostyle_datn.model.Response.ChatMessageResponse;
import org.example.chocostyle_datn.repository.ConversationRepository;
import org.example.chocostyle_datn.repository.KhachHangRepository;
import org.example.chocostyle_datn.repository.MessageRepository;
import org.example.chocostyle_datn.repository.NhanVienRepository;
import org.example.chocostyle_datn.service.ChatService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/conversations")
@CrossOrigin("*") // Cho phép Vue gọi API
public class ChatWsController {
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final KhachHangRepository khachHangRepository;
    private final NhanVienRepository nhanVienRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ChatWsController(ChatService chatService, SimpMessagingTemplate messagingTemplate, KhachHangRepository khachHangRepository, NhanVienRepository nhanVienRepository, ConversationRepository conversationRepository, MessageRepository messageRepository) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
        this.khachHangRepository = khachHangRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @MessageMapping("/chat.send")
    public void handleChat(ChatMessageRequest request) {
        // 1. Lưu tin nhắn vào DB thông qua Service
        Message saved = chatService.saveIncomingMessage(request);

        // 2. Chuyển đổi từ Entity sang Response DTO
        ChatMessageResponse response = ChatMessageResponse.builder()
                .id(saved.getId())
                .conversationId(saved.getConversation().getId())
                .senderId(saved.getSenderId())
                .senderType(saved.getSenderType())
                .content(saved.getContent())
                .sentAt(saved.getSentAt())
                .senderName(getSenderName(saved.getSenderId(), saved.getSenderType())) // Hàm lấy tên
                .build();

        // 3. Đẩy tới topic của hội thoại
        String destination = "/topic/chat/" + saved.getConversation().getId();
        messagingTemplate.convertAndSend(destination, response);
    }

    // Hàm hỗ trợ lấy tên người gửi dựa trên loại người dùng
    private String getSenderName(Integer id, String type) {
        if ("KHACH_HANG".equals(type)) {
            return khachHangRepository.findById(id).map(KhachHang::getTenKhachHang).orElse("Khách hàng");
        } else {
            return nhanVienRepository.findById(id).map(NhanVien::getHoTen).orElse("Nhân viên");
        }
    }

    @GetMapping("/waiting-list")
    public List<Conversation> getWaitingList() {
        return conversationRepository.findByNhanVienIsNull();
    }

    // 3. Thêm API "Tiếp nhận hội thoại" cho Nhân viên
    @PutMapping("/{id}/assign")
    public Conversation assignStaff(@PathVariable Integer id, @RequestBody Map<String, Integer> req) {
        Integer staffId = req.get("staffId");
        Conversation conv = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hội thoại không tồn tại"));

        if (conv.getNhanVien() != null) {
            throw new RuntimeException("Khách hàng này đã có người khác hỗ trợ!");
        }

        NhanVien nv = nhanVienRepository.findById(staffId).orElseThrow();
        conv.setNhanVien(nv);
        Conversation saved = conversationRepository.save(conv);

        // Thông báo cho các nhân viên khác cập nhật lại danh sách chờ qua WebSocket
        messagingTemplate.convertAndSend("/topic/chat/reload-waiting", "RELOAD");

        return saved;
    }

    // Trong ChatWsController.java

    // 1. API lấy tin nhắn kèm tên người gửi để hiển thị lịch sử không bị mất tên
    @GetMapping("/{id}/messages")
    public List<ChatMessageResponse> getMessages(@PathVariable Integer id,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "50") int size) {
        Conversation conv = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hội thoại không tồn tại"));

        List<Message> messages = messageRepository.findByConversationOrderBySentAtAsc(conv, PageRequest.of(page, size)).getContent();

        // Chuyển đổi từ Message Entity sang ChatMessageResponse DTO để có field senderName
        return messages.stream().map(msg -> ChatMessageResponse.builder()
                .id(msg.getId())
                .conversationId(msg.getConversation().getId())
                .senderId(msg.getSenderId())
                .senderType(msg.getSenderType())
                .senderName(getSenderName(msg.getSenderId(), msg.getSenderType())) // Lấy tên từ DB
                .content(msg.getContent())
                .sentAt(msg.getSentAt())
                .build()).toList();
    }

    // 2. Sửa lại API getOrCreate để hỗ trợ "Chỉ tìm kiếm" (Dành cho lúc mới mở Chat)
    @PostMapping("/get-or-create")
    public ResponseEntity<?> getOrCreate(@RequestBody Map<String, Object> req) {
        Integer khachHangId = (Integer) req.get("khachHangId");
        // Thêm flag check: Nếu true thì chỉ tìm cái cũ, nếu không thấy thì không tạo mới
        Boolean onlyFind = (Boolean) req.getOrDefault("onlyFind", false);

        KhachHang kh = khachHangRepository.findById(khachHangId).orElseThrow();

        Optional<Conversation> existing = conversationRepository.findTopByKhachHangOrderByIdDesc(kh);

        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get());
        }

        if (onlyFind) {
            return ResponseEntity.noContent().build(); // Trả về 204 nếu không có hội thoại cũ
        }

        // Nếu không có và onlyFind = false -> Tạo mới
        Conversation newConv = Conversation.builder()
                .khachHang(kh)
                .nhanVien(null)
                .build();

        Conversation saved = conversationRepository.save(newConv);
        messagingTemplate.convertAndSend("/topic/chat/new-waiting", "NEW");

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/staff/{staffId}")
    public List<Conversation> getStaffConversations(@PathVariable Integer staffId) {
        NhanVien nv = nhanVienRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));
        return conversationRepository.findByNhanVien(nv);
    }
}
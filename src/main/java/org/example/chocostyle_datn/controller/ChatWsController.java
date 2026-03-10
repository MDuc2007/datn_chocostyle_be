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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/conversations")
@CrossOrigin("*")
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

        Conversation conv = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new RuntimeException("Hội thoại không tồn tại"));

        Message saved;

        // 🔴 SỬA LỖI 2: Nếu khách đã chuyển sang WAITING hoặc ACTIVE (gặp nhân viên)
        // -> Chặn gọi ChatService (vì ChatService gọi Gemini), mà tự lưu tin nhắn thủ công.
        if ("WAITING".equals(conv.getTrangThai()) || "ACTIVE".equals(conv.getTrangThai())) {
            Message msg = Message.builder()
                    .conversation(conv)
                    .senderId(request.getSenderId())
                    .senderType(request.getSenderType())
                    .content(request.getContent())
                    .sentAt(java.time.LocalDateTime.now())
                    .build();
            saved = messageRepository.save(msg);
        } else {
            // Đang nói chuyện với BOT -> Giao cho ChatService để AI trả lời
            saved = chatService.saveIncomingMessage(request);
        }

        String destination = "/topic/chat/" + saved.getConversation().getId();

        // gửi message của user/nhân viên
        ChatMessageResponse response = ChatMessageResponse.builder()
                .id(saved.getId())
                .conversationId(saved.getConversation().getId())
                .senderId(saved.getSenderId())
                .senderType(saved.getSenderType())
                .content(saved.getContent())
                .sentAt(saved.getSentAt())
                .senderName(getSenderName(saved.getSenderId(), saved.getSenderType()))
                .build();

        messagingTemplate.convertAndSend(destination, response);

        // 🔴 SỬA LỖI 2 (tt): Chỉ kiểm tra và gửi tin nhắn BOT nếu trạng thái đang là BOT
        if ("BOT".equals(conv.getTrangThai())) {
            Optional<Message> lastMsg =
                    messageRepository.findTopByConversationOrderBySentAtDesc(saved.getConversation());

            if (lastMsg.isPresent()) {
                Message botMsg = lastMsg.get();

                if ("BOT".equals(botMsg.getSenderType())) {

                    ChatMessageResponse botResponse = ChatMessageResponse.builder()
                            .id(botMsg.getId())
                            .conversationId(botMsg.getConversation().getId())
                            .senderId(botMsg.getSenderId())
                            .senderType(botMsg.getSenderType())
                            .content(botMsg.getContent())
                            .sentAt(botMsg.getSentAt())
                            .senderName("ChocoBot")
                            .build();

                    messagingTemplate.convertAndSend(destination, botResponse);
                }
            }
        }
    }

    private String getSenderName(Integer id, String type) {
        if ("KHACH_HANG".equals(type)) {
            return khachHangRepository.findById(id).map(KhachHang::getTenKhachHang).orElse("Khách hàng");
        } else if ("AI".equals(type)) {
            return "ChocoBot";
        } else {
            return nhanVienRepository.findById(id).map(NhanVien::getHoTen).orElse("Nhân viên");
        }
    }

    @GetMapping("/waiting-list")
    public List<Conversation> getWaitingList() {
        return conversationRepository.findByNhanVienIsNullAndTrangThai("WAITING");
    }

    @PutMapping("/{id}/request-staff")
    public ResponseEntity<?> requestStaff(@PathVariable Integer id) {
        Conversation conv = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hội thoại không tồn tại"));

        conv.setTrangThai("WAITING");
        conversationRepository.save(conv);

        // Bắn thông báo cho StaffChat cập nhật danh sách
        messagingTemplate.convertAndSend("/topic/chat/new-waiting", "NEW");
        return ResponseEntity.ok().build();
    }

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
        conv.setTrangThai("ACTIVE");
        Conversation saved = conversationRepository.save(conv);

        Message sysMsg = Message.builder()
                .conversation(saved)
                .senderId(nv.getId())
                .senderType("SYSTEM")
                .content("Nhân viên " + nv.getHoTen() + " đã tham gia cuộc trò chuyện. Bạn có thể bắt đầu trao đổi.")
                .build();
        Message savedSysMsg = messageRepository.save(sysMsg);

        ChatMessageResponse response = ChatMessageResponse.builder()
                .id(savedSysMsg.getId())
                .conversationId(saved.getId())
                .senderId(nv.getId())
                .senderType("SYSTEM")
                .senderName("Hệ thống")
                .content(savedSysMsg.getContent())
                .sentAt(savedSysMsg.getSentAt())
                .build();
        messagingTemplate.convertAndSend("/topic/chat/" + saved.getId(), response);

        messagingTemplate.convertAndSend("/topic/chat/reload-waiting", "RELOAD");

        return saved;
    }

    @GetMapping("/{id}/messages")
    public List<ChatMessageResponse> getMessages(@PathVariable Integer id,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "50") int size) {
        Conversation conv = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hội thoại không tồn tại"));

        List<Message> messages = messageRepository.findByConversationOrderBySentAtAsc(conv, PageRequest.of(page, size)).getContent();

        return messages.stream().map(msg -> ChatMessageResponse.builder()
                .id(msg.getId())
                .conversationId(msg.getConversation().getId())
                .senderId(msg.getSenderId())
                .senderType(msg.getSenderType())
                .senderName(getSenderName(msg.getSenderId(), msg.getSenderType()))
                .content(msg.getContent())
                .sentAt(msg.getSentAt())
                .build()).toList();
    }

    @PostMapping("/get-or-create")
    public ResponseEntity<?> getOrCreate(@RequestBody Map<String, Object> req) {
        Integer khachHangId = (Integer) req.get("khachHangId");
        KhachHang kh = khachHangRepository.findById(khachHangId).orElseThrow();

        List<Conversation> existingConvs = conversationRepository.findAll().stream()
                .filter(c -> c.getKhachHang().getId().equals(khachHangId) &&
                        ("BOT".equals(c.getTrangThai()) || "WAITING".equals(c.getTrangThai()) || c.getNhanVien() != null))
                .toList();

        if (!existingConvs.isEmpty()) {
            return ResponseEntity.ok(existingConvs.get(existingConvs.size() - 1));
        }

        Conversation newConv = Conversation.builder()
                .khachHang(kh)
                .nhanVien(null)
                .trangThai("BOT")
                .build();

        Conversation saved = conversationRepository.save(newConv);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/staff/{staffId}")
    public List<Conversation> getStaffConversations(@PathVariable Integer staffId) {
        NhanVien nv = nhanVienRepository.findById(staffId).orElseThrow();
        return conversationRepository.findByNhanVien(nv);
    }
}
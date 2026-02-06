package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.Exception.DuplicateException;
import org.example.chocostyle_datn.entity.ChatLieu;
import org.example.chocostyle_datn.repository.ChatLieuRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static org.example.chocostyle_datn.util.TextNormalizeUtil.normalize;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatLieuService {

    private final ChatLieuRepository repo;

    public List<ChatLieu> getAll() {
        return repo.findAll();
    }

    public ChatLieu create(ChatLieu e) {
        if (repo.existsByTenIgnoreSpace(e.getTenChatLieu())) {
            throw new DuplicateException("Tên chất liệu đã tồn tại");
        }

        e.setMaChatLieu(genMa("CL", repo.findMaxMa()));

        // audit
        e.setNgayTao(LocalDate.now());
        e.setNguoiTao(e.getNguoiTao());
        e.setNgayCapNhat(null);
        e.setNguoiCapNhat(null);
        e.setTrangThai(1);

        return repo.save(e);
    }

    public ChatLieu update(Integer id, ChatLieu e) {
        ChatLieu old = repo.findById(id).orElseThrow();
        boolean isDuplicate = repo.existsByTenIgnoreSpace(e.getTenChatLieu());

        String oldCompare = old.getTenChatLieu().replace(" ", "").toLowerCase();
        String newCompare = e.getTenChatLieu().replace(" ", "").toLowerCase();

        if (isDuplicate && !oldCompare.equals(newCompare)) {
            throw new DuplicateException("Tên chất liệu đã tồn tại");
        }

        e.setId(id);

        // giữ dữ liệu cũ
        e.setMaChatLieu(old.getMaChatLieu());
        e.setNgayTao(old.getNgayTao());
        e.setNguoiTao(old.getNguoiTao());

        // cập nhật
        e.setNgayCapNhat(LocalDate.now());
        e.setNguoiCapNhat(e.getNguoiCapNhat());

        return repo.save(e);
    }

    public ChatLieu doiTrangThai(Integer id, String nguoiCapNhat) {
        ChatLieu mauSac = repo.findById(id).orElseThrow();

        // toggle trạng thái
        mauSac.setTrangThai(mauSac.getTrangThai() == 1 ? 0 : 1);

        mauSac.setNgayCapNhat(LocalDate.now());
        mauSac.setNguoiCapNhat(nguoiCapNhat);

        return repo.save(mauSac);
    }

    public void delete(Integer id) {
        repo.deleteById(id);
    }

    private String genMa(String p, String max) {
        if (max == null) return p + "01";
        return p + String.format("%02d", Integer.parseInt(max.replace(p, "")) + 1);
    }
}

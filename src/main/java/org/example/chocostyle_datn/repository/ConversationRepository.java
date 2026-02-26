package org.example.chocostyle_datn.repository;

import org.example.chocostyle_datn.entity.Conversation;
import org.example.chocostyle_datn.entity.KhachHang;
import org.example.chocostyle_datn.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    Optional<Conversation> findByKhachHangAndNhanVien(KhachHang khachHang, NhanVien nhanVien);

    List<Conversation> findByNhanVienIsNull();

    List<Conversation> findByNhanVien(NhanVien nv);

    Optional<Conversation> findTopByKhachHangOrderByIdDesc(KhachHang khachHang);
}
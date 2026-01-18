package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.NhanVien;
import org.example.chocostyle_datn.model.Request.NhanVienRequest;
import org.example.chocostyle_datn.model.Response.NhanVienResponse;
import org.example.chocostyle_datn.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class NhanVienService {

    @Autowired
    private NhanVienRepository repo;

    // 1. LẤY TẤT CẢ NHÂN VIÊN
    public List<NhanVienResponse> getAllNhanVien() {
        List<NhanVien> listNv = repo.findAll();
        // Convert Entity -> DTO Response
        return listNv.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 2. THÊM MỚI NHÂN VIÊN
    public NhanVienResponse createNhanVien(NhanVienRequest request) {
        NhanVien nv = new NhanVien();

        // --- Map thông tin cơ bản ---
        nv.setHoTen(request.getHoTen());
        nv.setEmail(request.getEmail());
        nv.setSoDienThoai(request.getSdt());
        nv.setCccd(request.getCccd());
        nv.setGioiTinh(request.getGioiTinh());
        nv.setNgaySinh(request.getNgaySinh());
        nv.setVaiTro(request.getVaiTro());
        nv.setNgayVaoLam(request.getNgayVaoLam());
        nv.setAvatar(request.getAvatar());

        // --- Map thông tin địa chỉ (Quan trọng) ---
        // Lưu địa chỉ cụ thể (Số nhà, đường) vào cột dia_chi
        nv.setDiaChi(request.getDiaChi());

        // Lưu các ID và Tên hành chính để phục vụ việc sửa và hiển thị
        nv.setTinhThanhId(request.getTinhThanhId());
        nv.setTinhThanh(request.getTinhThanh()); // Lưu tên Tỉnh (VD: Hà Nội)

        nv.setQuanHuyenId(request.getQuanHuyenId());
        nv.setQuanHuyen(request.getQuanHuyen()); // Lưu tên Huyện

        nv.setXaPhuongId(request.getXaPhuongId());
        nv.setXaPhuong(request.getXaPhuong());   // Lưu tên Xã

        // --- Các trường tự động ---
        nv.setMaNv(generateNextMaNv());           // Sinh mã NV (NV001...)
        nv.setMatKhau(generateRandomPassword(8)); // Sinh mật khẩu ngẫu nhiên
        nv.setTrangThai(1);                       // Mặc định: Đang làm việc

        // Lưu vào DB
        NhanVien savedNv = repo.save(nv);
        return mapToResponse(savedNv);
    }

    // 3. CẬP NHẬT NHÂN VIÊN
    public NhanVienResponse updateNhanVien(Integer id, NhanVienRequest request) {
        NhanVien nv = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên có ID: " + id));

        // Cập nhật các trường nếu có dữ liệu gửi lên
        if (request.getHoTen() != null) nv.setHoTen(request.getHoTen());
        if (request.getEmail() != null) nv.setEmail(request.getEmail());
        if (request.getSdt() != null) nv.setSoDienThoai(request.getSdt());
        if (request.getGioiTinh() != null) nv.setGioiTinh(request.getGioiTinh());
        if (request.getCccd() != null) nv.setCccd(request.getCccd());
        if (request.getNgaySinh() != null) nv.setNgaySinh(request.getNgaySinh());
        if (request.getVaiTro() != null) nv.setVaiTro(request.getVaiTro());
        if (request.getTrangThai() != null) nv.setTrangThai(request.getTrangThai());
        if (request.getNgayVaoLam() != null) nv.setNgayVaoLam(request.getNgayVaoLam());
        if (request.getAvatar() != null) nv.setAvatar(request.getAvatar());

        // Cập nhật Địa chỉ
        if (request.getDiaChi() != null) nv.setDiaChi(request.getDiaChi());

        // Cập nhật Tỉnh/Huyện/Xã (Cập nhật cả ID và Tên)
        if (request.getTinhThanhId() != null) {
            nv.setTinhThanhId(request.getTinhThanhId());
            nv.setTinhThanh(request.getTinhThanh());
        }
        if (request.getQuanHuyenId() != null) {
            nv.setQuanHuyenId(request.getQuanHuyenId());
            nv.setQuanHuyen(request.getQuanHuyen());
        }
        if (request.getXaPhuongId() != null) {
            nv.setXaPhuongId(request.getXaPhuongId());
            nv.setXaPhuong(request.getXaPhuong());
        }

        NhanVien updatedNv = repo.save(nv);
        return mapToResponse(updatedNv);
    }

    // --- HELPER: CHUYỂN ĐỔI ENTITY -> RESPONSE DTO ---
    private NhanVienResponse mapToResponse(NhanVien nv) {
        // Logic ghép chuỗi Full Address để hiển thị đẹp ở bảng danh sách
        // VD: "Số 10, Xã A, Huyện B, Tỉnh C"
        StringBuilder fullAddress = new StringBuilder();
        if (nv.getDiaChi() != null) fullAddress.append(nv.getDiaChi());
        if (nv.getXaPhuong() != null) fullAddress.append(", ").append(nv.getXaPhuong());
        if (nv.getQuanHuyen() != null) fullAddress.append(", ").append(nv.getQuanHuyen());
        if (nv.getTinhThanh() != null) fullAddress.append(", ").append(nv.getTinhThanh());

        return new NhanVienResponse(
                nv.getId(),
                nv.getMaNv(),
                nv.getHoTen(),
                nv.getEmail(),
                nv.getSoDienThoai(),

                fullAddress.toString(), // Trả về chuỗi full cho bảng hiển thị

                nv.getVaiTro(),
                nv.getTrangThai(),
                nv.getAvatar(),
                nv.getNgaySinh(),
                nv.getGioiTinh(),

                // Trả về các trường chi tiết để Form Sửa bind dữ liệu
                nv.getDiaChi(), // Địa chỉ cụ thể
                nv.getTinhThanhId(),
                nv.getQuanHuyenId(),
                nv.getXaPhuongId()
        );
    }

    // --- HELPER: SINH MÃ NV TỰ ĐỘNG (NV001, NV002...) ---
    private String generateNextMaNv() {
        NhanVien lastNv = repo.findNhanVienMoiNhat(); // Bạn cần viết Query này trong Repo
        if (lastNv == null) {
            return "NV001";
        }
        try {
            String lastMa = lastNv.getMaNv();
            int number = Integer.parseInt(lastMa.substring(2));
            number++;
            return String.format("NV%03d", number);
        } catch (Exception e) {
            return "NV" + System.currentTimeMillis();
        }
    }

    // --- HELPER: SINH MẬT KHẨU NGẪU NHIÊN ---
    private String generateRandomPassword(int len) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#";
        StringBuilder sb = new StringBuilder(len);
        Random random = new Random();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
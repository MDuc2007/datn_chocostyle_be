package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.DiaChi;
import org.example.chocostyle_datn.entity.KhachHang;
import org.example.chocostyle_datn.model.Request.DiaChiRequest;
import org.example.chocostyle_datn.model.Request.KhachHangRequest;
import org.example.chocostyle_datn.model.Response.KhachHangDetailResponse;
import org.example.chocostyle_datn.model.Response.KhachHangResponse;
import org.example.chocostyle_datn.repository.DiaChiRepository;
import org.example.chocostyle_datn.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KhachHangService {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private DiaChiRepository diaChiRepository;

    // 1. LẤY DANH SÁCH (Cập nhật avatar sang Base64 để hiển thị ở FE)
    public Page<KhachHangResponse> getKhachHangs(String keyword, Integer status, Pageable pageable) {
        Page<KhachHang> page = khachHangRepository.searchKhachHang(keyword, status, pageable);

        return page.map(kh -> {
            List<DiaChi> listDiaChi = diaChiRepository.findByKhachHangId(kh.getId());

            String diaChiChinh = listDiaChi.stream()
                    .filter(DiaChi::getMacDinh)
                    .map(dc -> dc.getDiaChiCuThe() + ", " + dc.getPhuong() + ", " + dc.getQuan() + ", " + dc.getThanhPho())
                    .findFirst()
                    .orElse("Chưa có địa chỉ mặc định");

            return KhachHangResponse.builder()
                    .id(kh.getId())
                    .maKhachHang(kh.getMaKh())
                    .tenKhachHang(kh.getTenKhachHang())
                    .email(kh.getEmail())
                    .soDienThoai(kh.getSoDienThoai())
                    .diaChiChinh(diaChiChinh)
                    .trangThai(kh.getTrangThai())
                    .avatar(convertImageToBase64(kh.getAvatar())) // Chuyển sang Base64
                    .build();
        });
    }

    // 2. LẤY CHI TIẾT (Đảm bảo đồng bộ JSON: ngayTao, ngayCapNhat, Base64 avatar)
    public KhachHangDetailResponse getDetailById(Integer id) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng ID: " + id));

        List<DiaChi> listDiaChiEntities = diaChiRepository.findByKhachHangId(id);

        return KhachHangDetailResponse.builder()
                .id(kh.getId())
                .avatar(convertImageToBase64(kh.getAvatar())) // Chuyển sang Base64 để FE hiển thị ngay
                .maKhachHang(kh.getMaKh())
                .tenKhachHang(kh.getTenKhachHang())
                .tenTaiKhoan(kh.getTenTaiKhoan())
                .soDienThoai(kh.getSoDienThoai())
                .email(kh.getEmail())
                .diaChiTongQuat(null) // Giữ theo mẫu JSON bạn yêu cầu
                .gioiTinh(kh.getGioiTinh())
                .ngaySinh(kh.getNgaySinh())
                .matKhau(kh.getMatKhau())
                .trangThai(kh.getTrangThai())
                .ngayTao(kh.getNgayTao()) // Map ngày tạo hệ thống
                .ngayCapNhat(kh.getNgayCapNhat()) // Map ngày cập nhật hệ thống
                .soLuongDonHang(kh.getSoLuongDonHang())
                .tongChiTieu(kh.getTongChiTieu())
                .listDiaChi(listDiaChiEntities.stream()
                        .map(dc -> KhachHangDetailResponse.DiaChiDetailResponse.builder()
                                .id(dc.getId())
                                .thanhPho(dc.getThanhPho())
                                .quan(dc.getQuan())
                                .phuong(dc.getPhuong())
                                .diaChiCuThe(dc.getDiaChiCuThe())
                                .macDinh(dc.getMacDinh())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    // 3. THÊM MỚI (Tự động thiết lập Ngày tạo)
    @Transactional
    public KhachHang addKhachHang(KhachHangRequest request, MultipartFile file) {
        validateUniqueFields(request.getSoDienThoai(), request.getEmail(), null);

        KhachHang kh = new KhachHang();
        kh.setTenKhachHang(request.getTenKhachHang());
        kh.setSoDienThoai(request.getSoDienThoai());
        kh.setEmail(request.getEmail());
        kh.setGioiTinh(request.getGioiTinh());
        kh.setNgaySinh(request.getNgaySinh());
        kh.setTenTaiKhoan(request.getTenTaiKhoan());
        kh.setMatKhau(request.getMatKhau());
        kh.setTrangThai(1);
        kh.setNgayTao(LocalDate.now()); // Thiết lập ngày tạo hệ thống
        kh.setMaKh("KH" + String.format("%05d", khachHangRepository.count() + 1));

        if (file != null && !file.isEmpty()) kh.setAvatar(saveAvatar(file));

        KhachHang savedKh = khachHangRepository.save(kh);
        saveAddresses(request.getListDiaChi(), savedKh);

        return savedKh;
    }

    // 4. CẬP NHẬT (Tự động thiết lập Ngày cập nhật)
    @Transactional
    public KhachHang updateKhachHang(Integer id, KhachHangRequest request, MultipartFile file) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        kh.setTenKhachHang(request.getTenKhachHang());
        kh.setSoDienThoai(request.getSoDienThoai());
        kh.setEmail(request.getEmail());
        kh.setGioiTinh(request.getGioiTinh());
        kh.setNgaySinh(request.getNgaySinh());
        kh.setTrangThai(request.getTrangThai());
        kh.setNgayCapNhat(LocalDate.now()); // Cập nhật ngày sửa đổi hệ thống

        if (file != null && !file.isEmpty()) kh.setAvatar(saveAvatar(file));

        if (request.getListDiaChi() != null) {
            diaChiRepository.deleteByKhachHangId(id);
            saveAddresses(request.getListDiaChi(), kh);
        }

        return khachHangRepository.save(kh);
    }

    // 5. ĐỔI TRẠNG THÁI
    @Transactional
    public void toggleStatus(Integer id) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
        kh.setTrangThai(kh.getTrangThai() == 1 ? 0 : 1);
        khachHangRepository.save(kh);
    }

    // 6. ĐẶT ĐỊA CHỈ MẶC ĐỊNH
    @Transactional
    public void setDefaultAddress(Integer khachHangId, Integer diaChiId) {
        List<DiaChi> list = diaChiRepository.findByKhachHangId(khachHangId);
        for (DiaChi dc : list) {
            dc.setMacDinh(dc.getId().equals(diaChiId));
        }
        diaChiRepository.saveAll(list);
    }

    // 7. THỐNG KÊ
    public long getTotalKhachHang() {
        return khachHangRepository.count();
    }

    // --- CÁC PHƯƠNG THỨC HỖ TRỢ (HELPER METHODS) ---

    // Chuyển đổi tệp ảnh vật lý sang chuỗi Base64 để hiển thị trực tiếp ở FE
    private String convertImageToBase64(String fileName) {
        if (fileName == null || fileName.isEmpty()) return null;
        try {
            Path path = Paths.get("uploads").resolve(fileName);
            if (Files.exists(path)) {
                byte[] bytes = Files.readAllBytes(path);
                String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                return "data:image/" + extension + ";base64," + Base64.getEncoder().encodeToString(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveAddresses(List<DiaChiRequest> listReq, KhachHang kh) {
        if (listReq == null || listReq.isEmpty()) return;
        List<DiaChi> listEntities = listReq.stream().map(req -> {
            DiaChi dc = new DiaChi();
            dc.setThanhPho(req.getThanhPho());
            dc.setQuan(req.getQuan());
            dc.setPhuong(req.getPhuong());
            dc.setDiaChiCuThe(req.getDiaChiCuThe());
            dc.setMacDinh(req.getMacDinh() != null ? req.getMacDinh() : false);
            dc.setMaDiaChi("DC" + UUID.randomUUID().toString().substring(0, 8));
            dc.setKhachHang(kh);
            dc.setTenDiaChi("Địa chỉ khách hàng"); // Gán mặc định để tránh lỗi DB
            return dc;
        }).collect(Collectors.toList());

        ensureSingleDefaultAddress(listEntities);
        diaChiRepository.saveAll(listEntities);
    }

    private void ensureSingleDefaultAddress(List<DiaChi> list) {
        if (list.isEmpty()) return;
        long countDefault = list.stream().filter(DiaChi::getMacDinh).count();
        if (countDefault != 1) {
            list.forEach(dc -> dc.setMacDinh(false));
            list.get(0).setMacDinh(true);
        }
    }

    private void validateUniqueFields(String sdt, String email, Integer currentId) {
        // Logic kiểm tra trùng lặp email/sdt (nếu cần)
    }

    private String saveAvatar(MultipartFile file) {
        try {
            // Xử lý loại bỏ khoảng trắng trong tên file để tránh lỗi URL
            String originalName = file.getOriginalFilename() != null ?
                    file.getOriginalFilename().replaceAll("\\s+", "_") : "avatar.png";
            String fileName = UUID.randomUUID() + "_" + originalName;
            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload ảnh", e);
        }
    }
}
package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.DiaChi;
import org.example.chocostyle_datn.entity.KhachHang;
import org.example.chocostyle_datn.model.Request.KhachHangRequest;
import org.example.chocostyle_datn.model.Request.KhachHangRequest.DiaChiRequest;
import org.example.chocostyle_datn.model.Response.KhachHangDetailResponse;
import org.example.chocostyle_datn.model.Response.KhachHangResponse;
import org.example.chocostyle_datn.repository.DiaChiRepository;
import org.example.chocostyle_datn.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KhachHangService {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private DiaChiRepository diaChiRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<KhachHang> getKhachHangForExport(String keyword, Integer status) {
        return khachHangRepository.searchKhachHangForExport(keyword, status);
    }

    public void toggleStatus(Integer id) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng ID: " + id));
        kh.setTrangThai(kh.getTrangThai() == 1 ? 0 : 1);
        khachHangRepository.save(kh);
    }

    public long getTotalKhachHang() {
        return khachHangRepository.count();
    }

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
                    .ngaySinh(kh.getNgaySinh())
                    .diaChiChinh(diaChiChinh)
                    .trangThai(kh.getTrangThai())
                    .avatar(processAvatarUrl(kh.getAvatar()))
                    .build();
        });
    }

    public KhachHangDetailResponse getDetailById(Integer id) {
        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng ID: " + id));

        List<DiaChi> listDiaChiEntities = diaChiRepository.findByKhachHangId(id);

        return KhachHangDetailResponse.builder()
                .id(kh.getId())
                .avatar(processAvatarUrl(kh.getAvatar()))
                .maKhachHang(kh.getMaKh())
                .tenKhachHang(kh.getTenKhachHang())
                .soDienThoai(kh.getSoDienThoai())
                .email(kh.getEmail())
                .gioiTinh(kh.getGioiTinh())
                .ngaySinh(kh.getNgaySinh())
                .trangThai(kh.getTrangThai())
                .ngayTao(kh.getNgayTao())
                .ngayCapNhat(kh.getNgayCapNhat())
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

    @Transactional
    public KhachHang addKhachHang(KhachHangRequest request, MultipartFile file) {
        validateUniqueFields(request.getSoDienThoai(), request.getEmail(), null);

        KhachHang kh = new KhachHang();
        kh.setTenKhachHang(request.getTenKhachHang());
        kh.setSoDienThoai(request.getSoDienThoai());
        kh.setEmail(request.getEmail());
        kh.setGioiTinh(request.getGioiTinh());
        kh.setNgaySinh(request.getNgaySinh());

        String rawPassword = generateRandomPassword(6);
        kh.setMatKhau(passwordEncoder.encode(rawPassword));

        kh.setTrangThai(1);
        kh.setVaiTro("USER");
        kh.setNgayTao(LocalDate.now());
        kh.setAuthProvider(org.example.chocostyle_datn.entity.AuthenticationProvider.LOCAL);

        // 👉 LƯU BASE64
        if (file != null && !file.isEmpty()) {
            kh.setAvatar(saveAvatar(file));
        }

        long total = khachHangRepository.count() + 1;
        String ma = String.format("KH%02d", total);
        kh.setMaKh(ma);

        KhachHang savedKh = khachHangRepository.save(kh);
        saveAddresses(request.getListDiaChi(), savedKh);

        try {
            emailService.sendAccountInfo(savedKh.getEmail(), savedKh.getEmail(), rawPassword);
        } catch (Exception e) {
            System.err.println("Lỗi gửi mail: " + e.getMessage());
        }

        return savedKh;
    }

    @Transactional
    public KhachHang updateKhachHang(Integer id, KhachHangRequest request, MultipartFile file) {
        validateUniqueFields(request.getSoDienThoai(), request.getEmail(), id);

        KhachHang kh = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));

        kh.setTenKhachHang(request.getTenKhachHang());
        kh.setSoDienThoai(request.getSoDienThoai());
        kh.setEmail(request.getEmail());
        kh.setGioiTinh(request.getGioiTinh());
        kh.setNgaySinh(request.getNgaySinh());
        kh.setTrangThai(request.getTrangThai());
        kh.setNgayCapNhat(LocalDate.now());

        // 👉 GHI ĐÈ CHUỖI BASE64 MỚI NẾU CÓ ẢNH
        if (file != null && !file.isEmpty()) {
            kh.setAvatar(saveAvatar(file));
        }

        if (request.getListDiaChi() != null) {
            diaChiRepository.deleteByKhachHangId(id);
            saveAddresses(request.getListDiaChi(), kh);
        }

        return khachHangRepository.save(kh);
    }

    @Transactional
    public void setDefaultAddress(Integer khachHangId, Integer diaChiId) {
        List<DiaChi> list = diaChiRepository.findByKhachHangId(khachHangId);
        for (DiaChi dc : list) {
            dc.setMacDinh(dc.getId().equals(diaChiId));
        }
        diaChiRepository.saveAll(list);
    }

    private String generateRandomPassword(int length) {
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    private void validateUniqueFields(String sdt, String email, Integer currentId) {
        if (sdt != null && !sdt.isBlank()) {
            boolean exists = (currentId == null)
                    ? khachHangRepository.existsBySoDienThoai(sdt)
                    : khachHangRepository.existsBySoDienThoaiAndIdNot(sdt, currentId);
            if (exists) throw new RuntimeException("Số điện thoại '" + sdt + "' đã tồn tại!");
        }

        if (email != null && !email.isBlank()) {
            boolean exists = (currentId == null)
                    ? khachHangRepository.existsByEmail(email)
                    : khachHangRepository.existsByEmailAndIdNot(email, currentId);
            if (exists) throw new RuntimeException("Email '" + email + "' đã tồn tại!");
        }
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
            dc.setTenDiaChi("Địa chỉ khách hàng");
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

    // 👉 TRẢ VỀ NGUYÊN CHUỖI BASE64 CHO FRONTEND
    private String processAvatarUrl(String avatarData) {
        return avatarData;
    }

    // 👉 CHUYỂN FILE THÀNH CHUỖI BASE64 ĐỂ LƯU DB
    private String saveAvatar(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            String base64String = Base64.getEncoder().encodeToString(bytes);
            String contentType = file.getContentType();
            // Nối thêm tiền tố để thẻ <img> của Vue.js có thể đọc trực tiếp
            return "data:" + contentType + ";base64," + base64String;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi chuyển đổi ảnh sang Base64", e);
        }
    }
}
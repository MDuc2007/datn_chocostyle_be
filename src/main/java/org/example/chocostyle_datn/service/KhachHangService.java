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
// 1. IMPORT THÊM PASSWORD ENCODER
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private EmailService emailService;


    @Autowired
    private DiaChiRepository diaChiRepository;


    // 2. INJECT PASSWORD ENCODER VÀO ĐÂY
    @Autowired
    private PasswordEncoder passwordEncoder;


    public List<KhachHang> getKhachHangForExport(String keyword, Integer status) {
        return khachHangRepository.searchKhachHangForExport(keyword, status);
    }


    // 1. LẤY DANH SÁCH
    public Page<KhachHangResponse> getKhachHangs(String keyword, Integer status, Pageable pageable) {
        Page<KhachHang> page = khachHangRepository.searchKhachHang(keyword, status, pageable);


        return page.map(kh -> {
            List<DiaChi> listDiaChi = diaChiRepository.findByKhachHangId(kh.getId());


            String diaChiChinh = listDiaChi.stream().filter(DiaChi::getMacDinh).map(dc -> dc.getDiaChiCuThe() + ", " + dc.getPhuong() + ", " + dc.getQuan() + ", " + dc.getThanhPho()).findFirst().orElse("Chưa có địa chỉ mặc định");


            return KhachHangResponse.builder().id(kh.getId()).maKhachHang(kh.getMaKh()).tenKhachHang(kh.getTenKhachHang()).email(kh.getEmail()).soDienThoai(kh.getSoDienThoai()).ngaySinh(kh.getNgaySinh()).diaChiChinh(diaChiChinh).trangThai(kh.getTrangThai()).avatar(processAvatarUrl(kh.getAvatar())).build();
        });
    }


    // 2. LẤY CHI TIẾT
    public KhachHangDetailResponse getDetailById(Integer id) {
        KhachHang kh = khachHangRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng ID: " + id));


        List<DiaChi> listDiaChiEntities = diaChiRepository.findByKhachHangId(id);


        return KhachHangDetailResponse.builder().id(kh.getId()).avatar(processAvatarUrl(kh.getAvatar())).maKhachHang(kh.getMaKh()).tenKhachHang(kh.getTenKhachHang()).tenTaiKhoan(kh.getTenTaiKhoan()).soDienThoai(kh.getSoDienThoai()).email(kh.getEmail()).diaChiTongQuat(null).gioiTinh(kh.getGioiTinh()).ngaySinh(kh.getNgaySinh()).matKhau(kh.getMatKhau()).trangThai(kh.getTrangThai()).ngayTao(kh.getNgayTao()).ngayCapNhat(kh.getNgayCapNhat()).listDiaChi(listDiaChiEntities.stream().map(dc -> KhachHangDetailResponse.DiaChiDetailResponse.builder().id(dc.getId()).thanhPho(dc.getThanhPho()).quan(dc.getQuan()).phuong(dc.getPhuong()).diaChiCuThe(dc.getDiaChiCuThe()).macDinh(dc.getMacDinh()).build()).collect(Collectors.toList())).build();
    }


    // 3. THÊM MỚI (ĐÃ SỬA MÃ HÓA MẬT KHẨU)
    @Transactional
    public KhachHang addKhachHang(KhachHangRequest request, MultipartFile file) {
        // Trong hàm addKhachHang, sửa dòng đầu tiên thành:
        validateUniqueFields(request.getSoDienThoai(), request.getEmail(), request.getTenTaiKhoan(), null // thêm mới
        );


        KhachHang kh = new KhachHang();
        kh.setTenKhachHang(request.getTenKhachHang());
        kh.setSoDienThoai(request.getSoDienThoai());
        kh.setEmail(request.getEmail());
        kh.setGioiTinh(request.getGioiTinh());
        kh.setNgaySinh(request.getNgaySinh());
        kh.setTenTaiKhoan(request.getTenTaiKhoan());


        // --- BẮT ĐẦU SỬA ---
        // Logic: Nếu có pass thì mã hóa, không có thì set mặc định 123456
        String rawPassword = request.getMatKhau();
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            rawPassword = "123456"; // Mật khẩu mặc định
        }
        // Mã hóa BCrypt trước khi lưu
        kh.setMatKhau(passwordEncoder.encode(rawPassword));
        // --- KẾT THÚC SỬA ---


        kh.setTrangThai(1);
        kh.setVaiTro("USER");
        kh.setNgayTao(LocalDate.now());
        kh.setMaKh("KH" + String.format("%05d", khachHangRepository.count() + 1));


        if (file != null && !file.isEmpty()) kh.setAvatar(saveAvatar(file));


        KhachHang savedKh = khachHangRepository.save(kh);
        saveAddresses(request.getListDiaChi(), savedKh);


        emailService.sendAccountInfo(savedKh.getEmail(), savedKh.getEmail(),   // login bằng email
                rawPassword           // gửi mật khẩu gốc
        );


        return savedKh;
    }


    // 4. CẬP NHẬT
    @Transactional
    public KhachHang updateKhachHang(Integer id, KhachHangRequest request, MultipartFile file) {
        validateUniqueFields(request.getSoDienThoai(), request.getEmail(), request.getTenTaiKhoan(), id // loại trừ chính nó
        );


        KhachHang kh = khachHangRepository.findById(id).orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại"));


        kh.setTenKhachHang(request.getTenKhachHang());
        kh.setSoDienThoai(request.getSoDienThoai());
        kh.setEmail(request.getEmail());
        kh.setGioiTinh(request.getGioiTinh());
        kh.setNgaySinh(request.getNgaySinh());
        kh.setTrangThai(request.getTrangThai());
        kh.setVaiTro("USER");
        kh.setNgayCapNhat(LocalDate.now());


        // Nếu muốn cập nhật mật khẩu ở đây cũng cần mã hóa tương tự (nếu request có gửi mk mới)
        // Hiện tại giữ nguyên logic cũ của bạn (không update pass tại API này)


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
        KhachHang kh = khachHangRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
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


    private String processAvatarUrl(String imageName) {
        if (imageName == null || imageName.isEmpty()) return null;
        if (imageName.startsWith("http://") || imageName.startsWith("https://")) {
            return imageName;
        }
        return convertLocalImageToBase64(imageName);
    }


    private String convertLocalImageToBase64(String fileName) {
        try {
            Path path = Paths.get("uploads").resolve(fileName);
            if (Files.exists(path)) {
                byte[] bytes = Files.readAllBytes(path);
                String extension = "png";
                int i = fileName.lastIndexOf('.');
                if (i > 0) {
                    extension = fileName.substring(i + 1).toLowerCase();
                }
                return "data:image/" + extension + ";base64," + Base64.getEncoder().encodeToString(bytes);
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc ảnh local: " + fileName + " - " + e.getMessage());
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


    private void validateUniqueFields(String sdt, String email, String tenTaiKhoan, Integer currentId) {


        if (sdt != null && !sdt.isBlank()) {
            boolean exists = (currentId == null) ? khachHangRepository.existsBySoDienThoai(sdt) : khachHangRepository.existsBySoDienThoaiAndIdNot(sdt, currentId);


            if (exists) {
                throw new RuntimeException("Số điện thoại '" + sdt + "' đã tồn tại!");
            }
        }


        if (email != null && !email.isBlank()) {
            boolean exists = (currentId == null) ? khachHangRepository.existsByEmail(email) : khachHangRepository.existsByEmailAndIdNot(email, currentId);


            if (exists) {
                throw new RuntimeException("Email '" + email + "' đã tồn tại!");
            }
        }


        if (tenTaiKhoan != null && !tenTaiKhoan.isBlank()) {
            boolean exists = (currentId == null) ? khachHangRepository.existsByTenTaiKhoan(tenTaiKhoan) : khachHangRepository.existsByTenTaiKhoanAndIdNot(tenTaiKhoan, currentId);


            if (exists) {
                throw new RuntimeException("Tên tài khoản '" + tenTaiKhoan + "' đã tồn tại!");
            }
        }
    }

    private String saveAvatar(MultipartFile file) {
        try {
            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename().replaceAll("\\s+", "_") : "avatar.png";
            String fileName = UUID.randomUUID() + "_" + originalName;
            Path uploadPath = Paths.get("uploads");
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload ảnh", e);
        }
    }


    public KhachHangDetailResponse getDetailByEmail(String email) {

        KhachHang kh = khachHangRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với email: " + email));

        List<DiaChi> listDiaChiEntities = diaChiRepository.findByKhachHangId(kh.getId());

        return KhachHangDetailResponse.builder().id(kh.getId()).avatar(processAvatarUrl(kh.getAvatar())).maKhachHang(kh.getMaKh()).tenKhachHang(kh.getTenKhachHang()).tenTaiKhoan(kh.getTenTaiKhoan()).soDienThoai(kh.getSoDienThoai()).email(kh.getEmail()).diaChiTongQuat(null).gioiTinh(kh.getGioiTinh()).ngaySinh(kh.getNgaySinh()).matKhau(null) // ❗ KHÔNG trả mật khẩu
                .trangThai(kh.getTrangThai()).ngayTao(kh.getNgayTao()).ngayCapNhat(kh.getNgayCapNhat()).listDiaChi(listDiaChiEntities.stream().map(dc -> KhachHangDetailResponse.DiaChiDetailResponse.builder().id(dc.getId()).thanhPho(dc.getThanhPho()).quan(dc.getQuan()).phuong(dc.getPhuong()).diaChiCuThe(dc.getDiaChiCuThe()).macDinh(dc.getMacDinh()).build()).collect(Collectors.toList())).build();
    }


}




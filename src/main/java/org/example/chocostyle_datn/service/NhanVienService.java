package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.NhanVien;
import org.example.chocostyle_datn.model.Request.NhanVienRequest;
import org.example.chocostyle_datn.model.Response.NhanVienResponse;
import org.example.chocostyle_datn.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
public class NhanVienService {


    @Autowired
    private NhanVienRepository repo;


    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. LẤY TẤT CẢ (Sắp xếp giảm dần theo ID -> Người mới nhất lên đầu)
    public List<NhanVienResponse> getAllNhanVien() {
        // Sort.by(Sort.Direction.DESC, "id"): Sắp xếp cột 'id' giảm dần
        return repo.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // 2. LẤY CHI TIẾT
    public NhanVienResponse getNhanVienById(Integer id) {
        NhanVien nv = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên: " + id));
        return mapToResponse(nv);
    }

    // --- 3. THÊM MỚI (CÓ VALIDATE) ---
    public NhanVienResponse createNhanVien(NhanVienRequest request) {
        // 1. Validate trùng lặp
        if (repo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email này đã tồn tại trong hệ thống!");
        }
        if (repo.existsBySoDienThoai(request.getSdt())) {
            throw new RuntimeException("Số điện thoại này đã tồn tại!");
        }
        // 👇 THÊM ĐOẠN CHECK KHÁCH HÀNG Ở ĐÂY 👇
        if (repo.countEmailInKhachHang(request.getEmail()) > 0) {
            throw new RuntimeException("Email này đã được đăng ký bởi Khách hàng!");
        }
        NhanVien nv = new NhanVien();
        mapRequestToEntity(request, nv);


        nv.setVaiTro("Nhân viên");
        nv.setNgayVaoLam(LocalDate.now());
        nv.setMaNv(generateNextMaNv(request.getHoTen()));
        String plainPassword = generateRandomPassword(8);
        nv.setMatKhau(passwordEncoder.encode(plainPassword));
        nv.setTrangThai(1);


        NhanVien savedNv = repo.save(nv);


        // Gửi email (Async hoặc Try-catch để không chặn luồng chính)
        try {
            if (savedNv.getEmail() != null) {
                emailService.sendAccountInfo(savedNv.getEmail(), savedNv.getHoTen(), savedNv.getMaNv(), plainPassword);
            }
        } catch (Exception e) {
            System.err.println("Lỗi gửi mail: " + e.getMessage());
        }


        return mapToResponse(savedNv);
    }

    // --- 4. CẬP NHẬT (CÓ VALIDATE TRỪ CHÍNH NÓ) ---
    public NhanVienResponse updateNhanVien(Integer id, NhanVienRequest request) {
        NhanVien nv = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên: " + id));

        // 1. Validate trùng lặp (trừ bản ghi hiện tại)
        if (repo.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new RuntimeException("Email này đang được sử dụng bởi nhân viên khác!");
        }
        if (repo.existsBySoDienThoaiAndIdNot(request.getSdt(), id)) {
            throw new RuntimeException("Số điện thoại này đang được sử dụng bởi nhân viên khác!");
        }
        // Logic: Dù là sửa, cũng không được đổi email thành email của khách hàng
        if (repo.countEmailInKhachHang(request.getEmail()) > 0) {
            throw new RuntimeException("Email này thuộc về một Khách hàng, không thể sử dụng!");
        }
        mapRequestToEntity(request, nv);
        if (request.getTrangThai() != null) nv.setTrangThai(request.getTrangThai());

        return mapToResponse(repo.save(nv));
    }

    // --- LOGIC MAP DỮ LIỆU & GHÉP ĐỊA CHỈ (QUAN TRỌNG) ---
    private void mapRequestToEntity(NhanVienRequest req, NhanVien nv) {
        // 1. Thông tin cơ bản
        if (req.getHoTen() != null) nv.setHoTen(req.getHoTen());
        if (req.getEmail() != null) nv.setEmail(req.getEmail());
        if (req.getSdt() != null) nv.setSoDienThoai(req.getSdt());
//        if (req.getCccd() != null) nv.setCccd(req.getCccd());
        if (req.getGioiTinh() != null) nv.setGioiTinh(req.getGioiTinh());
        if (req.getNgaySinh() != null) nv.setNgaySinh(req.getNgaySinh());
        if (req.getVaiTro() != null) nv.setVaiTro(req.getVaiTro());
        if (req.getNgayVaoLam() != null) nv.setNgayVaoLam(req.getNgayVaoLam());
        if (req.getAvatar() != null) nv.setAvatar(req.getAvatar());


        // 2. Lưu các trường địa chỉ thành phần
        if (req.getDiaChiCuThe() != null) nv.setDiaChiCuThe(req.getDiaChiCuThe());

        // Lưu ID (để bind Combobox)
        if (req.getTinhThanhId() != null) nv.setTinhThanhId(req.getTinhThanhId());
        if (req.getQuanHuyenId() != null) nv.setQuanHuyenId(req.getQuanHuyenId());
        if (req.getXaPhuongId() != null) nv.setXaPhuongId(req.getXaPhuongId());


        // Lưu Tên (để ghép chuỗi)
        if (req.getTinhThanh() != null) nv.setTinhThanh(req.getTinhThanh());
        if (req.getQuanHuyen() != null) nv.setQuanHuyen(req.getQuanHuyen());
        if (req.getXaPhuong() != null) nv.setXaPhuong(req.getXaPhuong());


        // 3. TỰ ĐỘNG GHÉP CHUỖI FULL (Lưu vào cột dia_chi)
        // Logic: "Số 10, Ngõ 5, Xã A, Huyện B, Tỉnh C"
        StringBuilder full = new StringBuilder();


        // Lấy giá trị mới nhất (từ request hoặc từ DB cũ)
        String cuThe = req.getDiaChiCuThe() != null ? req.getDiaChiCuThe() : nv.getDiaChiCuThe();
        String xa = req.getXaPhuong() != null ? req.getXaPhuong() : nv.getXaPhuong();
        String huyen = req.getQuanHuyen() != null ? req.getQuanHuyen() : nv.getQuanHuyen();
        String tinh = req.getTinhThanh() != null ? req.getTinhThanh() : nv.getTinhThanh();


        if (cuThe != null && !cuThe.isEmpty()) full.append(cuThe);
        if (xa != null && !xa.isEmpty()) { if(full.length()>0) full.append(", "); full.append(xa); }
        if (huyen != null && !huyen.isEmpty()) { if(full.length()>0) full.append(", "); full.append(huyen); }
        if (tinh != null && !tinh.isEmpty()) { if(full.length()>0) full.append(", "); full.append(tinh); }


        nv.setDiaChi(full.toString());
    }

    // --- MAP RESPONSE ---
    private NhanVienResponse mapToResponse(NhanVien nv) {
        return new NhanVienResponse(
                nv.getId(),
                nv.getMaNv(),
                nv.getHoTen(),
                nv.getEmail(),
                nv.getSoDienThoai(),


                nv.getDiaChi(), // Trả về chuỗi Full cho bảng hiển thị


                nv.getVaiTro(),
                nv.getTrangThai(),
                nv.getAvatar(),
                nv.getNgaySinh(),
                nv.getGioiTinh(),


                // Trả về chi tiết cho Form Sửa
                nv.getDiaChiCuThe(),
                nv.getTinhThanhId(),
                nv.getQuanHuyenId(),
                nv.getXaPhuongId(),


                nv.getNgayTao(),
                nv.getNgayCapNhat(),
                nv.getNgayVaoLam()
//                nv.getCccd()
        );
    }


    private String generateNextMaNv(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "NV" + System.currentTimeMillis(); // Fallback nếu không có tên
        }


        // B1: Tạo tiền tố (Prefix). VD: "Trần Lê Lệnh Quyết" -> "Quyettll"
        String prefix = generatePrefixFromApps(fullName);


        // B2: Tìm trong DB xem có mã nào bắt đầu bằng "Quyettll" chưa
        String lastMaNv = repo.findMaxMaNvByPrefix(prefix);


        if (lastMaNv == null) {
            // Chưa có ai -> Bắt đầu là 001
            return prefix + "001";
        }


        // B3: Nếu có rồi (VD: Quyettll005) -> Cắt lấy số đuôi, tăng lên 1
        try {
            // Cắt bỏ phần chữ, lấy phần số ở cuối
            String numberPart = lastMaNv.substring(prefix.length());
            int number = Integer.parseInt(numberPart);
            number++;
            // Format lại thành 3 chữ số (VD: 6 -> 006)
            return prefix + String.format("%03d", number);
        } catch (Exception e) {
// Phòng trường hợp lỗi format, trả về ngẫu nhiên để không crash
            return prefix + System.currentTimeMillis();
        }
    }


    // Hàm tách chữ cái: "Trần Lê Lệnh Quyết" -> "Quyettll"
    private String generatePrefixFromApps(String fullName) {
        // 1. Bỏ dấu tiếng Việt: "Trần Lê Lệnh Quyết" -> "Tran Le Lenh Quyet"
        String unaccented = removeAccent(fullName);


        // 2. Tách các từ: ["Tran", "Le", "Lenh", "Quyet"]
        String[] parts = unaccented.trim().split("\\s+");


        if (parts.length == 0) return "NV";


        // 3. Lấy tên chính (Từ cuối cùng) -> "Quyet"
        String mainName = parts[parts.length - 1];
        // Viết hoa chữ cái đầu của tên (Quyet)
        mainName = mainName.substring(0, 1).toUpperCase() + mainName.substring(1).toLowerCase();


        // 4. Lấy ký tự đầu của Họ đệm -> "Tran", "Le", "Lenh" -> "t", "l", "l"
        StringBuilder suffix = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].length() > 0) {
                suffix.append(parts[i].substring(0, 1).toLowerCase());
            }
        }


        // Kết quả: Quyet + tll
        return mainName + suffix.toString();
    }


    // Hàm tiện ích: Loại bỏ dấu Tiếng Việt
    private String removeAccent(String s) {
        if (s == null) return "";
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ','d').replace('Đ','D');
    }


    private String generateRandomPassword(int len) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#";
        StringBuilder sb = new StringBuilder(len);
        Random random = new Random();
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // Thêm hàm search phân trang
    public Page<NhanVienResponse> searchNhanVien(String keyword, Integer trangThai, int page, int size) {
        // Sắp xếp giảm dần theo id (mới nhất lên đầu)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

        Page<NhanVien> nhanVienPage = repo.searchNhanVien(keyword, trangThai, pageable);

        // Chuyển đổi từ Entity sang Response
        return nhanVienPage.map(this::mapToResponse);
    }
}
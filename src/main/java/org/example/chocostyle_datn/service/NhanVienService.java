package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.NhanVien;
import org.example.chocostyle_datn.model.Request.NhanVienRequest;
import org.example.chocostyle_datn.model.Response.NhanVienResponse;
import org.example.chocostyle_datn.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
// 1. IMPORT PASSWORD ENCODER
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

    // 2. INJECT PASSWORD ENCODER
    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. LẤY TẤT CẢ (Sắp xếp giảm dần theo ID -> Người mới nhất lên đầu)
    public List<NhanVienResponse> getAllNhanVien() {
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

    // 3. THÊM MỚI (ĐÃ SỬA LOGIC MÃ HÓA)
    public NhanVienResponse createNhanVien(NhanVienRequest request) {
        NhanVien nv = new NhanVien();

        // Gọi hàm map chung (bao gồm logic ghép địa chỉ)
        mapRequestToEntity(request, nv);

        // 1. Chức vụ luôn là "Nhân viên" (hoặc logic tùy bạn)
        nv.setVaiTro("Nhân viên");

        // 2. Ngày vào làm là ngày hiện tại (Hôm nay)
        nv.setNgayVaoLam(LocalDate.now());

        nv.setMaNv(generateNextMaNv(request.getHoTen()));
        nv.setTrangThai(1);

        // --- BẮT ĐẦU SỬA ---
        // B1: Tạo mật khẩu ngẫu nhiên (Lưu vào biến tạm để gửi mail)
        String rawPassword = generateRandomPassword(8);

        // B2: Mã hóa mật khẩu trước khi lưu vào Database
        nv.setMatKhau(passwordEncoder.encode(rawPassword));
        // --- KẾT THÚC SỬA ---

        NhanVien savedNv = repo.save(nv);

        // 👇 2. GỌI HÀM GỬI EMAIL
        try {
            if (savedNv.getEmail() != null && !savedNv.getEmail().isEmpty()) {
                emailService.sendAccountInfo(
                        savedNv.getEmail(),
                        savedNv.getHoTen(),
                        savedNv.getMaNv(),
                        rawPassword // QUAN TRỌNG: Gửi mật khẩu gốc (chưa mã hóa) cho nhân viên xem
                );
            }
        } catch (Exception e) {
            System.err.println("Không thể gửi email: " + e.getMessage());
        }

        return mapToResponse(savedNv);
    }

    // 4. CẬP NHẬT
    public NhanVienResponse updateNhanVien(Integer id, NhanVienRequest request) {
        NhanVien nv = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên: " + id));

        // Gọi hàm map chung (để cập nhật lại địa chỉ nếu có thay đổi)
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
        // if (req.getCccd() != null) nv.setCccd(req.getCccd());
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
        StringBuilder full = new StringBuilder();

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

                nv.getDiaChi(),

                nv.getVaiTro(),
                nv.getTrangThai(),
                nv.getAvatar(),
                nv.getNgaySinh(),
                nv.getGioiTinh(),

                nv.getDiaChiCuThe(),
                nv.getTinhThanhId(),
                nv.getQuanHuyenId(),
                nv.getXaPhuongId(),

                nv.getNgayTao(),
                nv.getNgayCapNhat(),
                nv.getNgayVaoLam()
                // nv.getCccd()
        );
    }

    private String generateNextMaNv(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "NV" + System.currentTimeMillis();
        }

        String prefix = generatePrefixFromApps(fullName);
        String lastMaNv = repo.findMaxMaNvByPrefix(prefix);

        if (lastMaNv == null) {
            return prefix + "001";
        }

        try {
            String numberPart = lastMaNv.substring(prefix.length());
            int number = Integer.parseInt(numberPart);
            number++;
            return prefix + String.format("%03d", number);
        } catch (Exception e) {
            return prefix + System.currentTimeMillis();
        }
    }

    private String generatePrefixFromApps(String fullName) {
        String unaccented = removeAccent(fullName);
        String[] parts = unaccented.trim().split("\\s+");

        if (parts.length == 0) return "NV";

        String mainName = parts[parts.length - 1];
        mainName = mainName.substring(0, 1).toUpperCase() + mainName.substring(1).toLowerCase();

        StringBuilder suffix = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].length() > 0) {
                suffix.append(parts[i].substring(0, 1).toLowerCase());
            }
        }
        return mainName + suffix.toString();
    }

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
}


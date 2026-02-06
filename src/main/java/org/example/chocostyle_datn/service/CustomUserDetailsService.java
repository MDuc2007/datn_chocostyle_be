package org.example.chocostyle_datn.service;


import org.example.chocostyle_datn.entity.KhachHang;
import org.example.chocostyle_datn.entity.NhanVien;
import org.example.chocostyle_datn.repository.KhachHangRepository;
import org.example.chocostyle_datn.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.util.Collections;
import java.util.Optional;


@Service
public class CustomUserDetailsService implements UserDetailsService {


    @Autowired
    private KhachHangRepository khachHangRepository;


    @Autowired
    private NhanVienRepository nhanVienRepository; // 1. Inject thêm Repo Nhân viên


    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {


        // =========================================================
        // BƯỚC 1: TÌM TRONG BẢNG NHÂN VIÊN TRƯỚC (Ưu tiên Admin/Staff)
        // =========================================================
        // Hàm này tìm theo Email hoặc Mã nhân viên (Bạn nhớ khai báo trong NhanVienRepository nhé)
        Optional<NhanVien> nhanVienOpt = nhanVienRepository.findByEmailOrMaNhanVien(usernameOrEmail);


        if (nhanVienOpt.isPresent()) {
            NhanVien nv = nhanVienOpt.get();


            // Kiểm tra trạng thái (VD: 0 là bị khóa/nghỉ việc)
            if (nv.getTrangThai() != null && nv.getTrangThai() == 0) {
                throw new UsernameNotFoundException("Tài khoản nhân viên đã bị khóa!");
            }


            // Xử lý Vai trò: Map từ DB sang chuẩn Spring Security (ROLE_...)
            String dbRole = nv.getVaiTro(); // Giá trị trong DB: "admin", "Nhan_Vien"
            String springRole = "ROLE_STAFF"; // Mặc định là nhân viên thường


            if ("admin".equalsIgnoreCase(dbRole)) {
                springRole = "ROLE_ADMIN";
            } else if ("Nhan_Vien".equalsIgnoreCase(dbRole)) {
                springRole = "ROLE_STAFF";
            }


            return new User(
                    nv.getEmail(), // Username định danh
                    nv.getMatKhau(), // Mật khẩu hash
                    Collections.singletonList(new SimpleGrantedAuthority(springRole)) // Quyền hạn
            );
        }


        // =========================================================
        // BƯỚC 2: TÌM TRONG BẢNG KHÁCH HÀNG (Nếu không phải nhân viên)
        // =========================================================
        Optional<KhachHang> khachHangOpt = khachHangRepository.findByTenTaiKhoan(usernameOrEmail);
        if (khachHangOpt.isEmpty()) {
            khachHangOpt = khachHangRepository.findByEmail(usernameOrEmail);
        }


        if (khachHangOpt.isPresent()) {
            KhachHang kh = khachHangOpt.get();


            // Kiểm tra trạng thái
            if (kh.getTrangThai() != null && kh.getTrangThai() == 0) {
                throw new UsernameNotFoundException("Tài khoản khách hàng bị khóa!");
            }


            // Xử lý an toàn cho mật khẩu (trường hợp login Google pass có thể null)
            String password = (kh.getMatKhau() == null) ? "" : kh.getMatKhau();


            // Xử lý Vai trò: Lấy từ cột 'vai_tro' trong DB (đã thêm ở bước trước)
            // Giả sử DB lưu "USER" -> Spring hiểu "ROLE_USER"
            String dbRole = kh.getVaiTro();
            String springRole = "ROLE_USER"; // Fallback mặc định nếu null


            if (dbRole != null && !dbRole.isEmpty()) {
                springRole = "ROLE_" + dbRole.toUpperCase();
            }


            return new User(
                    kh.getTenTaiKhoan(),
                    password,
                    Collections.singletonList(new SimpleGrantedAuthority(springRole))
            );
        }


        // =========================================================
        // BƯỚC 3: KHÔNG TÌM THẤY TÀI KHOẢN
        // =========================================================
        throw new UsernameNotFoundException("Không tìm thấy người dùng với thông tin: " + usernameOrEmail);
    }
}


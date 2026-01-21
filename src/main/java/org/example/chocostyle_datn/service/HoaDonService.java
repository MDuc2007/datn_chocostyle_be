package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.*;
import org.example.chocostyle_datn.model.Request.SearchHoaDonRequest;
import org.example.chocostyle_datn.model.Request.UpdateTrangThaiRequest;
import org.example.chocostyle_datn.model.Response.*;
import org.example.chocostyle_datn.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HoaDonService {

    @Autowired private HoaDonRepository hoaDonRepo;
    @Autowired private LichSuHoaDonRepository lichSuRepo;
    @Autowired private HoaDonChiTietRepository hdctRepo;
    @Autowired private ThanhToanRepository thanhToanRepo;
    @Autowired private ChiTietSanPhamRepository spctRepo;
    @Autowired private NhanVienRepository nhanVienRepo;
    @Autowired private KhachHangRepository khachHangRepo;
    @Autowired private PhieuGiamGiaRepository pggRepo;


    // =================================================================
    // 1. LẤY CHI TIẾT (GET DETAIL) - PHIÊN BẢN CHỐNG NULL
    // =================================================================
    @Transactional(readOnly = true)
    public HoaDonDetailResponse getDetail(Integer id) {
        // Tìm hóa đơn
        HoaDon hd = hoaDonRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // Lấy danh sách liên quan
        List<HoaDonChiTiet> hdcts = hdctRepo.findByIdHoaDon_Id(id);
        List<LichSuHoaDon> lichSus = lichSuRepo.findByIdHoaDon_IdOrderByThoiGianDesc(id);
        List<ThanhToan> thanhToans = thanhToanRepo.findByIdHoaDon_Id(id);

        // Xử lý an toàn cho Nhân viên (tránh lỗi nếu nhân viên bị xóa)
        String tenNhanVien = "Không xác định";
        if (hd.getIdNhanVien() != null) {
            tenNhanVien = hd.getIdNhanVien().getHoTen();
        }

        return HoaDonDetailResponse.builder()
                .id(hd.getId())
                .maHoaDon(hd.getMaHoaDon())
                .tenKhachHang(hd.getTenKhachHang())
                .soDienThoai(hd.getSoDienThoai())
                .diaChi(hd.getDiaChiKhachHang())
                .tenNhanVien(tenNhanVien)
                .trangThai(hd.getTrangThai())
                .loaiDon(hd.getLoaiDon())
                .ngayTao(hd.getNgayTao())
                .ghiChu(hd.getGhiChu())
                .tongTienHang(hd.getTongTienGoc())
                .phiShip(hd.getPhiVanChuyen() != null ? hd.getPhiVanChuyen() : BigDecimal.ZERO)
                .giamGia(hd.getSoTienGiam() != null ? hd.getSoTienGiam() : BigDecimal.ZERO)
                .tongThanhToan(hd.getTongTienThanhToan())

                // --- LOGIC MAPPING SẢN PHẨM AN TOÀN ---
                .sanPhamList(hdcts.stream().map(ct -> {
                    // Mặc định giá trị nếu dữ liệu null
                    String tenSp = "Sản phẩm ẩn/Đã xóa";
                    String tenMau = "-";
                    String tenSize = "-";

                    // Kiểm tra từng cấp độ để tránh NullPointerException
                    if (ct.getIdSpct() != null) {
                        if (ct.getIdSpct().getIdSanPham() != null) {
                            tenSp = ct.getIdSpct().getIdSanPham().getTenSp();
                        }
                        if (ct.getIdSpct().getIdMauSac() != null) {
                            tenMau = ct.getIdSpct().getIdMauSac().getTenMauSac();
                        }
                        if (ct.getIdSpct().getIdKichCo() != null) {
                            tenSize = ct.getIdSpct().getIdKichCo().getTenKichCo();
                        }
                    }

                    return HoaDonSanPhamResponse.builder()
                            .tenSanPham(tenSp)
                            .mauSac(tenMau)
                            .kichCo(tenSize)
                            .soLuong(ct.getSoLuong())
                            .donGia(ct.getDonGia())
                            .thanhTien(ct.getThanhTien())
                            .build();
                }).collect(Collectors.toList()))
                // --------------------------------------

                .lichSuList(lichSus.stream().map(ls -> HoaDonLichSuResponse.builder()
                        .trangThai(ls.getTrangThai())
                        .hanhDong(ls.getHanhDong())
                        .ghiChu(ls.getGhiChu())
                        .thoiGian(ls.getThoiGian() != null ? ls.getThoiGian().toString() : "")
                        .nguoiThucHien("Hệ thống")
                        .build()).collect(Collectors.toList()))

                .thanhToanList(thanhToans.stream().map(tt -> HoaDonThanhToanResponse.builder()
                        .phuongThuc((tt.getIdPttt() != null) ? tt.getIdPttt().getTenPttt() : "Khác")
                        .soTien(tt.getSoTien())
                        .trangThai(tt.getTrangThai())
                        .thoiGian(tt.getThoiGianThanhToan() != null ? tt.getThoiGianThanhToan().toString() : "")
                        .build()).collect(Collectors.toList()))
                .build();
    }

    // =================================================================
    // 3. CẬP NHẬT TRẠNG THÁI (PUT) - Đã thêm cập nhật ngày giờ
    // =================================================================
    @Transactional
    public void updateStatus(Integer id, UpdateTrangThaiRequest req) {
        HoaDon hd = hoaDonRepo.findById(id).orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        if (hd.getTrangThai() == 5) {
            throw new RuntimeException("Hóa đơn đã hủy, không thể cập nhật!");
        }

        hd.setTrangThai(req.getTrangThaiMoi());

        // --- ĐÃ BỔ SUNG THEO YÊU CẦU ---
        hd.setNgayCapNhat(LocalDate.now());

        if(req.getTrangThaiMoi() == 4) { // 4 = Hoàn thành
            hd.setNgayThanhToan(LocalDate.now());
        }
        // -------------------------------

        hoaDonRepo.save(hd);

        ghiLichSu(hd, req.getTrangThaiMoi(), getActionName(req.getTrangThaiMoi()), req.getGhiChu());
    }

    // =================================================================
    // 4. LẤY DANH SÁCH (GET ALL) - Giữ nguyên
    // =================================================================
    @Transactional(readOnly = true)
    public Page<HoaDonResponse> getAll(SearchHoaDonRequest req, Pageable pageable) {
        return hoaDonRepo.findAllByFilter(req.getKeyword(), req.getLoaiDon(), req.getTrangThai(),
                        req.getStartDate(), req.getEndDate(), pageable)
                .map(hd -> HoaDonResponse.builder()
                        .id(hd.getId())
                        .maHoaDon(hd.getMaHoaDon())
                        .tenKhachHang(hd.getTenKhachHang())
                        .tenNhanVien(hd.getIdNhanVien() != null ? hd.getIdNhanVien().getHoTen() : "N/A")
                        .tongTien(hd.getTongTienThanhToan())
                        .loaiDon(hd.getLoaiDon())
                        .trangThai(hd.getTrangThai())
                        .ngayTao(hd.getNgayTao())
                        .build());
    }

    // Hàm phụ ghi lịch sử
    private void ghiLichSu(HoaDon hd, Integer trangThai, String hanhDong, String ghiChu) {
        LichSuHoaDon ls = new LichSuHoaDon();
        ls.setIdHoaDon(hd);
        ls.setTrangThai(trangThai);
        ls.setThoiGian(LocalDateTime.now());
        ls.setHanhDong(hanhDong);
        ls.setGhiChu(ghiChu);
        lichSuRepo.save(ls);
    }

    private String getActionName(Integer status) {
        switch (status) {
            case 1: return "Xác nhận đơn hàng";
            case 2: return "Đã giao cho vận chuyển";
            case 3: return "Đang giao hàng";
            case 4: return "Giao hàng thành công";
            case 5: return "Đã hủy đơn hàng";
            default: return "Cập nhật trạng thái";
        }
    }

    // ========================================================================
    // 1. HÀM PHỤ TRỢ: TÍNH TIỀN GIẢM (Xử lý Tiền mặt & Phần trăm)
    // ========================================================================
    private BigDecimal tinhToanGiamGia(PhieuGiamGia voucher, BigDecimal tongTienHang) {
        if (voucher == null) return BigDecimal.ZERO;

        BigDecimal tienGiam = BigDecimal.ZERO;
        String loai = voucher.getLoaiGiam(); // Lấy giá trị cột 'loai_giam' từ DB

        // --- TRƯỜNG HỢP 1: GIẢM THEO PHẦN TRĂM ---
        // SQL của bạn lưu là: N'Phần trăm'
        if (loai != null && (loai.equalsIgnoreCase("Phần trăm") || loai.contains("%") || loai.equalsIgnoreCase("PERCENT"))) {
            // Công thức: Tổng tiền * (Giá trị / 100)
            // Ví dụ: 500.000 * 10 / 100 = 50.000
            tienGiam = tongTienHang.multiply(voucher.getGiaTri())
                    .divide(BigDecimal.valueOf(100));

            // Kiểm tra Giảm Tối Đa (nếu có)
            if (voucher.getGiaTriToiDa() != null && tienGiam.compareTo(voucher.getGiaTriToiDa()) > 0) {
                tienGiam = voucher.getGiaTriToiDa();
            }
        }
        // --- TRƯỜNG HỢP 2: GIẢM TIỀN MẶT ---
        // SQL của bạn lưu là: N'Tiền mặt'
        else {
            // Trừ thẳng số tiền quy định
            tienGiam = voucher.getGiaTri();
        }

        return tienGiam;
    }

    // ========================================================================
    // 2. FULL CODE: TẠO HÓA ĐƠN MỚI
    // ========================================================================
    @Transactional
    public Integer taoHoaDonMoi(org.example.chocostyle_datn.model.Request.CreateOrderRequest req) {

        // --- BƯỚC 1: KHỞI TẠO HÓA ĐƠN ---
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon(generateMaHoaDon()); // Hàm sinh mã tự động (HD001, HD002...)
        hd.setNgayTao(LocalDate.now());
        hd.setLoaiDon(req.getLoaiDon());    // 0: Online, 1: Tại quầy
        hd.setTongTienGoc(req.getTongTienHang());
        hd.setGhiChu(req.getGhiChu());

        // Set trạng thái ban đầu
        if (req.getLoaiDon() != null && req.getLoaiDon() == 1) {
            // Tại quầy -> Mặc định Hoàn thành (4) + Đã thanh toán
            hd.setTrangThai(4);
            hd.setNgayThanhToan(LocalDate.now());
        } else {
            // Online -> Mặc định Chờ xác nhận (0)
            hd.setTrangThai(0);
        }

        // --- BƯỚC 2: XỬ LÝ PHIẾU GIẢM GIÁ (VOUCHER) ---
        BigDecimal tienGiam = BigDecimal.ZERO;

        // Kiểm tra xem request có mã voucher không (Khác null và không rỗng)
        if (req.getMaVoucher() != null && !req.getMaVoucher().trim().isEmpty()) {

            // Tìm voucher trong DB theo mã (VD: KM50K)
            PhieuGiamGia voucher = pggRepo.findByMaPgg(req.getMaVoucher())
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá '" + req.getMaVoucher() + "' không tồn tại!"));

            // Validate: Còn số lượng không?
            if (voucher.getSoLuong() <= voucher.getSoLuongDaDung()) {
                throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng!");
            }

            // Validate: Đủ điều kiện đơn tối thiểu không?
            if (voucher.getDieuKienDonHang() != null
                    && req.getTongTienHang().compareTo(voucher.getDieuKienDonHang()) < 0) {
                throw new RuntimeException("Đơn hàng chưa đủ điều kiện áp dụng (Tối thiểu: "
                        + voucher.getDieuKienDonHang() + ")");
            }

            // Tính tiền được giảm (Gọi hàm phụ trợ ở trên)
            tienGiam = tinhToanGiamGia(voucher, req.getTongTienHang());

            // Lưu thông tin voucher vào hóa đơn
            hd.setIdPhieuGiamGia(voucher);
            hd.setSoTienGiam(tienGiam);

            // Cập nhật: Tăng số lượng đã dùng của Voucher lên 1
            voucher.setSoLuongDaDung(voucher.getSoLuongDaDung() + 1);
            pggRepo.save(voucher);
        }
        else {
            // Không dùng voucher
            hd.setSoTienGiam(BigDecimal.ZERO);
        }

        // --- BƯỚC 3: TÍNH TỔNG THANH TOÁN CUỐI CÙNG ---
        // Công thức: (Tiền hàng + Phí Ship) - Tiền Giảm
        BigDecimal phiShip = req.getPhiShip() != null ? req.getPhiShip() : BigDecimal.ZERO;
        hd.setPhiVanChuyen(phiShip);

        BigDecimal tongCuoiCung = req.getTongTienHang().add(phiShip).subtract(tienGiam);

        // Đảm bảo không bị âm tiền (nếu giảm giá lớn hơn đơn hàng)
        if (tongCuoiCung.compareTo(BigDecimal.ZERO) < 0) {
            tongCuoiCung = BigDecimal.ZERO;
        }
        hd.setTongTienThanhToan(tongCuoiCung);

        // --- BƯỚC 4: MAP THÔNG TIN NHÂN VIÊN & KHÁCH HÀNG ---
        if (req.getIdNhanVien() != null) {
            hd.setIdNhanVien(nhanVienRepo.findById(req.getIdNhanVien()).orElse(null));
        }

        if (req.getIdKhachHang() != null) {
            KhachHang kh = khachHangRepo.findById(req.getIdKhachHang()).orElse(null);
            if (kh != null) {
                hd.setIdKhachHang(kh);
                hd.setTenKhachHang(kh.getTenKhachHang());
                hd.setSoDienThoai(kh.getSoDienThoai());
                hd.setDiaChiKhachHang(kh.getDiaChi());
            }
        } else {
            hd.setTenKhachHang("Khách lẻ"); // Khách vãng lai
        }

        // Lưu Hóa đơn lần 1 để lấy ID
        HoaDon savedHd = hoaDonRepo.save(hd);

        // --- BƯỚC 5: LƯU CHI TIẾT SẢN PHẨM & TRỪ KHO ---
        if (req.getSanPhamChiTiet() != null) {
            for (org.example.chocostyle_datn.model.Request.CartItemRequest item : req.getSanPhamChiTiet()) {

                // Tìm chi tiết sản phẩm
                ChiTietSanPham sp = spctRepo.findById(item.getIdChiTietSanPham())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm ID " + item.getIdChiTietSanPham() + " không tồn tại"));

                // Kiểm tra tồn kho
                if (sp.getSoLuongTon() < item.getSoLuong()) {
                    throw new RuntimeException("Sản phẩm " + sp.getMaChiTietSanPham() + " không đủ số lượng tồn kho!");
                }

                // Trừ kho
                sp.setSoLuongTon(sp.getSoLuongTon() - item.getSoLuong());
                spctRepo.save(sp);

                // Tạo Hóa Đơn Chi Tiết
                HoaDonChiTiet hdct = new HoaDonChiTiet();
                hdct.setIdHoaDon(savedHd);
                hdct.setIdSpct(sp);
                hdct.setSoLuong(item.getSoLuong());
                hdct.setDonGia(item.getDonGia());

                // Thành tiền = Số lượng * Đơn giá
                hdct.setThanhTien(item.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong())));

                hdctRepo.save(hdct);
            }
        }

        // --- BƯỚC 6: GHI LỊCH SỬ HOẠT ĐỘNG ---
        ghiLichSu(savedHd, savedHd.getTrangThai(), "Tạo mới đơn hàng", req.getGhiChu());

        return savedHd.getId();
    }

    // Hàm sinh mã: Bắt đầu từ HD000, HD001, HD002...
    private String generateMaHoaDon() {
        // 1. Lấy hóa đơn mới nhất trong DB
        HoaDon lastHoaDon = hoaDonRepo.findTopByOrderByIdDesc();
        // 2. Nếu chưa có hóa đơn nào -> Trả về HD000
        if (lastHoaDon == null) {
            return "HD001";
        }

        try {
            // 3. Lấy mã cũ (Ví dụ: HD000)
            String lastMa = lastHoaDon.getMaHoaDon();

            // Cắt bỏ chữ "HD" (2 ký tự đầu), lấy phần số
            String numberPart = lastMa.substring(2);

            // Chuyển thành số nguyên và cộng thêm 1
            int nextNumber = Integer.parseInt(numberPart) + 1;

            // Format lại thành 3 chữ số (Ví dụ: 0 -> 000, 1 -> 001, 99 -> 099)
            return String.format("HD%03d", nextNumber);

        } catch (Exception e) {
            // Trường hợp lỗi (ví dụ mã cũ không đúng định dạng) thì fallback về time
            return "HD" + System.currentTimeMillis();
        }
    }
}
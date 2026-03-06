package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.*;
import org.example.chocostyle_datn.model.Request.CreateOrderRequest;
import org.example.chocostyle_datn.model.Request.RefundRequest;
import org.example.chocostyle_datn.model.Request.SearchHoaDonRequest;
import org.example.chocostyle_datn.model.Request.UpdateTrangThaiRequest;
import org.example.chocostyle_datn.model.Response.*;
import org.example.chocostyle_datn.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HoaDonService {

    @Autowired
    private HoaDonRepository hoaDonRepo;
    @Autowired
    private LichSuHoaDonRepository lichSuRepo;
    @Autowired
    private HoaDonChiTietRepository hdctRepo;
    @Autowired
    private ThanhToanRepository thanhToanRepo;
    @Autowired
    private ChiTietSanPhamRepository spctRepo;
    @Autowired
    private NhanVienRepository nhanVienRepo;
    @Autowired
    private KhachHangRepository khachHangRepo;
    @Autowired
    private PhieuGiamGiaRepository pggRepo;
    @Autowired
    private PhieuGiamGiaKhachHangRepository pggKhRepository;
    @Autowired
    private PhuongThucThanhToanRepository ptttRepo;
    @Autowired
    private EmailService emailService;

    // =================================================================
    // 1. LẤY CHI TIẾT (GET DETAIL)
    // =================================================================
    @Transactional(readOnly = true)
    public HoaDonDetailResponse getDetail(Integer id) {
        HoaDon hd = hoaDonRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        List<HoaDonChiTiet> hdcts = hdctRepo.findByIdHoaDon_Id(id);
        List<LichSuHoaDon> lichSus = lichSuRepo.findByIdHoaDon_IdOrderByThoiGianDesc(id);
        List<ThanhToan> thanhToans = thanhToanRepo.findByIdHoaDon_Id(id);

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
                .sanPhamList(hdcts.stream().map(ct -> {
                    String tenSp = "Sản phẩm ẩn/Đã xóa";
                    String tenMau = "-";
                    String tenSize = "-";

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
                        // Gán thêm loại giao dịch để FE biết hoàn tiền hay thu tiền
                        .loaiGiaoDich(tt.getLoaiGiaoDich())
                        .ghiChu(tt.getGhiChu())
                        .maGiaoDich(tt.getMaGiaoDich())
                        .build()).collect(Collectors.toList()))
                .build();
    }

    // =================================================================
    // 2. CẬP NHẬT TRẠNG THÁI (PUT)
    // =================================================================
    @Transactional
    public void updateStatus(Integer id, UpdateTrangThaiRequest req) {
        HoaDon hd = hoaDonRepo.findById(id).orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        if (hd.getTrangThai() == 5) {
            throw new RuntimeException("Hóa đơn đã hủy, không thể cập nhật!");
        }

        // Logic quan trọng: Trừ kho khi chuyển sang trạng thái 1 (Xác nhận)
        if (req.getTrangThaiMoi() == 1 && hd.getTrangThai() == 0) {
            List<HoaDonChiTiet> chiTiets = hdctRepo.findByIdHoaDon_Id(id);
            for (HoaDonChiTiet ct : chiTiets) {
                ChiTietSanPham sp = ct.getIdSpct();
                if (sp != null) {
                    // Kiểm tra hàng tồn trước khi trừ
                    if (sp.getSoLuongTon() < ct.getSoLuong()) {
                        throw new RuntimeException("Sản phẩm " + sp.getMaChiTietSanPham() + " không đủ hàng để xác nhận!");
                    }
                    sp.setSoLuongTon(sp.getSoLuongTon() - ct.getSoLuong());
                    spctRepo.save(sp);
                }
            }
            // Đồng thời cập nhật lại id_nhan_vien là người thực hiện bấm nút xác nhận
            hd.setIdNhanVien(getNhanVienDangLogin());
        }

        // Logic hoàn kho khi Hủy đơn (Trạng thái 5)
        if (req.getTrangThaiMoi() == 5 && hd.getTrangThai() != 0) {
            // Chỉ hoàn kho nếu đơn đã từng được xác nhận (đã trừ kho trước đó)
            List<HoaDonChiTiet> chiTiets = hdctRepo.findByIdHoaDon_Id(id);
            for (HoaDonChiTiet ct : chiTiets) {
                ChiTietSanPham sp = ct.getIdSpct();
                if (sp != null) {
                    sp.setSoLuongTon(sp.getSoLuongTon() + ct.getSoLuong());
                    spctRepo.save(sp);
                }
            }
        }

        hd.setTrangThai(req.getTrangThaiMoi());
        hd.setNgayCapNhat(LocalDateTime.now());

        if (req.getTrangThaiMoi() == 4) { // 4 = Hoàn thành
            hd.setNgayThanhToan(LocalDateTime.now());
        }

        hoaDonRepo.save(hd);
        ghiLichSu(hd, req.getTrangThaiMoi(), getActionName(req.getTrangThaiMoi()), req.getGhiChu());
    }

    // =================================================================
    // 3. LẤY DANH SÁCH (GET ALL)
    // =================================================================
    @Transactional(readOnly = true)
    public Page<HoaDonResponse> getAll(SearchHoaDonRequest req, Pageable pageable) {
        return hoaDonRepo.findAllByFilter(req.getKeyword(), req.getLoaiDon(), req.getTrangThai(),
                        req.getStartDate(), req.getEndDate(), pageable)
                .map(hd -> HoaDonResponse.builder()
                        .id(hd.getId())
                        .maHoaDon(hd.getMaHoaDon())
                        .tenKhachHang(hd.getTenKhachHang())
                        .soDienThoai(hd.getSoDienThoai())
                        .tongTien(hd.getTongTienThanhToan())
                        .loaiDon(hd.getLoaiDon())
                        .trangThai(hd.getTrangThai())
                        .ngayTao(hd.getNgayTao())
                        .build());
    }

    private BigDecimal xuLyVoucher(CreateOrderRequest req, HoaDon hd) {
        BigDecimal tienGiam = BigDecimal.ZERO;

        if (req.getMaVoucher() == null || req.getMaVoucher().trim().isEmpty()) {
            hd.setSoTienGiam(BigDecimal.ZERO);
            return BigDecimal.ZERO;
        }

        PhieuGiamGia voucher = pggRepo
                .findFirstByMaPggOrderByTrangThaiDesc(req.getMaVoucher())
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại!"));

        LocalDate today = LocalDate.now();

        if (voucher.getTrangThai() == null || voucher.getTrangThai() != 1)
            throw new RuntimeException("Mã giảm giá hiện không hoạt động!");

        if (today.isBefore(voucher.getNgayBatDau()))
            throw new RuntimeException("Mã giảm giá chưa đến thời gian sử dụng!");

        if (today.isAfter(voucher.getNgayKetThuc()))
            throw new RuntimeException("Mã giảm giá đã hết hạn!");

        if (voucher.getSoLuong() <= voucher.getSoLuongDaDung())
            throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng!");

        if (voucher.getDieuKienDonHang() != null &&
                req.getTongTienHang().compareTo(voucher.getDieuKienDonHang()) < 0)
            throw new RuntimeException("Đơn hàng chưa đủ điều kiện áp dụng!");

        if ("PERSONAL".equalsIgnoreCase(voucher.getKieuApDung())) {

            if (req.getIdKhachHang() == null)
                throw new RuntimeException("Voucher này chỉ áp dụng cho khách hàng cụ thể!");

            PhieuGiamGiaKhachHang pggKh =
                    pggKhRepository.findByPhieuGiamGiaIdAndKhachHangId(
                            voucher.getId(),
                            req.getIdKhachHang()
                    );

            if (pggKh == null)
                throw new RuntimeException("Khách hàng không được cấp voucher này!");

            if (pggKh.getDaSuDung())
                throw new RuntimeException("Voucher này đã được sử dụng!");

            pggKh.setDaSuDung(true);
            pggKhRepository.save(pggKh);
        }

        tienGiam = tinhToanGiamGia(voucher, req.getTongTienHang());

        hd.setIdPhieuGiamGia(voucher);
        hd.setSoTienGiam(tienGiam);

        voucher.setSoLuongDaDung(voucher.getSoLuongDaDung() + 1);
        pggRepo.save(voucher);

        return tienGiam;
    }

    // =================================================================
    // 4. LUỒNG BÁN HÀNG TẠI QUẦY: TẠO TAB HÓA ĐƠN TRỐNG (NHÁP)
    // =================================================================
    @Transactional
    public HoaDon taoHoaDonChoTaiQuay() {
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon(generateMaHoaDon());
        hd.setNgayTao(LocalDateTime.now());
        hd.setNgayCapNhat(LocalDateTime.now());

        hd.setLoaiDon(1);
        hd.setTrangThai(0);

        hd.setTongTienGoc(BigDecimal.ZERO);
        hd.setTongTienThanhToan(BigDecimal.ZERO);
        hd.setPhiVanChuyen(BigDecimal.ZERO);
        hd.setSoTienGiam(BigDecimal.ZERO);

        hd.setIdNhanVien(getNhanVienDangLogin());

        HoaDon savedHd = hoaDonRepo.save(hd);
        ghiLichSu(savedHd, 0, "Tạo đơn", "Tạo tab hóa đơn mới tại quầy");
        return savedHd;
    }

    // =================================================================
    // 5. LUỒNG BÁN HÀNG TẠI QUẦY: XÁC NHẬN ĐẶT HÀNG (CẬP NHẬT ĐƠN NHÁP) - ĐÃ BỔ SUNG LOẠI 3 VÀ COD
    // =================================================================
    @Transactional
    public void xacNhanDatHangTaiQuay(Integer idHoaDon, CreateOrderRequest req) {
        HoaDon hd = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn nháp!"));

        // Đảm bảo cập nhật đúng loại đơn (1: Tại quầy, 3: Tại quầy - Giao hàng)
        int loaiDon = req.getLoaiDon() != null ? req.getLoaiDon() : 1;
        hd.setLoaiDon(loaiDon);
        hd.setTongTienGoc(req.getTongTienHang());
        hd.setGhiChu(req.getGhiChu());

        // Validate khắt khe cho loại đơn 3 (Giao hàng)
        if (loaiDon == 3) {
            if (req.getDiaChiGiaoHang() == null || req.getDiaChiGiaoHang().trim().isEmpty()) {
                throw new RuntimeException("Đơn hàng tại quầy - Giao hàng phải có địa chỉ nhận hàng!");
            }
            if (req.getSdtNguoiNhan() == null || req.getSdtNguoiNhan().trim().isEmpty()) {
                throw new RuntimeException("Đơn hàng tại quầy - Giao hàng phải có số điện thoại người nhận!");
            }
        }

        // Xử lý Voucher
        BigDecimal tienGiam = xuLyVoucher(req, hd);

        // Tính tổng tiền
        BigDecimal phiShip = req.getPhiShip() != null ? req.getPhiShip() : BigDecimal.ZERO;
        hd.setPhiVanChuyen(phiShip);
        BigDecimal tongCuoiCung = req.getTongTienHang().add(phiShip).subtract(tienGiam);
        if (tongCuoiCung.compareTo(BigDecimal.ZERO) < 0) tongCuoiCung = BigDecimal.ZERO;
        hd.setTongTienThanhToan(tongCuoiCung);

        // Map khách hàng
        if (req.getIdKhachHang() != null) {
            KhachHang kh = khachHangRepo.findById(req.getIdKhachHang()).orElse(null);
            if (kh != null) {
                hd.setIdKhachHang(kh);
                hd.setTenKhachHang(loaiDon == 3 && req.getTenNguoiNhan() != null ? req.getTenNguoiNhan() : kh.getTenKhachHang());
                hd.setSoDienThoai(loaiDon == 3 && req.getSdtNguoiNhan() != null ? req.getSdtNguoiNhan() : kh.getSoDienThoai());

                if (loaiDon == 3) {
                    hd.setDiaChiKhachHang(req.getDiaChiGiaoHang());
                } else {
                    String diaChiMacDinh = kh.getListDiaChiObj().stream()
                            .filter(dc -> dc.getMacDinh() != null && dc.getMacDinh())
                            .map(dc -> dc.getDiaChiCuThe() + ", " + dc.getPhuong() + ", " + dc.getQuan() + ", " + dc.getThanhPho())
                            .findFirst()
                            .orElse("Mua trực tiếp tại quầy");
                    hd.setDiaChiKhachHang(diaChiMacDinh);
                }
            }
        } else {
            hd.setTenKhachHang(loaiDon == 3 && req.getTenNguoiNhan() != null ? req.getTenNguoiNhan() : "Khách lẻ");
            if (loaiDon == 3) {
                hd.setSoDienThoai(req.getSdtNguoiNhan());
                hd.setDiaChiKhachHang(req.getDiaChiGiaoHang());
            }
        }

        // Cập nhật trạng thái
        if (loaiDon == 1) {
            hd.setTrangThai(4); // Hoàn thành luôn
            hd.setNgayThanhToan(LocalDateTime.now());
        } else if (loaiDon == 3) {
            hd.setTrangThai(0); // Bắt đầu từ 0 y hệt Online
        }
        hd.setNgayCapNhat(LocalDateTime.now());

        hoaDonRepo.save(hd);

        // ===============================================================
        // MỚI THÊM TỪ TRƯỚC: GHI LẠI LỊCH SỬ THANH TOÁN
        // ===============================================================
        Integer ptttId = 1; // 1 = Tiền mặt (Mặc định)
        boolean isCOD = false;

        if (req.getGhiChu() != null) {
            String noteLower = req.getGhiChu().toLowerCase();
            if (noteLower.contains("chuyển khoản")) {
                ptttId = 2; // 2 = Chuyển khoản
            } else if (noteLower.contains("cod") || noteLower.contains("khi nhận hàng")) {
                isCOD = true;
            }
        }

        PhuongThucThanhToan pttt = ptttRepo.findById(ptttId).orElse(null);
        if (pttt == null) {
            pttt = ptttRepo.findById(1).orElseThrow(() -> new RuntimeException("Không tìm thấy Phương thức thanh toán!"));
        }

        ThanhToan thanhToan = new ThanhToan();
        thanhToan.setIdHoaDon(hd);
        thanhToan.setIdPttt(pttt);
        thanhToan.setSoTien(tongCuoiCung);

        try {
            thanhToan.setLoaiGiaoDich(1);
        } catch (Exception e) {
        }

        if (isCOD) {
            thanhToan.setTrangThai(0); // COD -> Chờ thanh toán
        } else {
            thanhToan.setTrangThai(1); // Tiền mặt / CK -> Đã thanh toán
        }

        thanhToan.setThoiGianThanhToan(LocalDateTime.now());
        thanhToan.setThoiGianTao(LocalDateTime.now());
        thanhToan.setGhiChu(req.getGhiChu());
        thanhToan.setMaGiaoDich("PAY-" + System.currentTimeMillis());

        thanhToanRepo.save(thanhToan);
        // ===============================================================

        // Xóa chi tiết cũ nếu có (trường hợp update lại)
        List<HoaDonChiTiet> oldDetails = hdctRepo.findByIdHoaDon_Id(idHoaDon);
        if (!oldDetails.isEmpty()) {
            hdctRepo.deleteAll(oldDetails);
        }

        // Lưu sản phẩm và trừ kho
        if (req.getSanPhamChiTiet() != null) {
            for (org.example.chocostyle_datn.model.Request.CartItemRequest item : req.getSanPhamChiTiet()) {
                ChiTietSanPham sp = spctRepo.findByIdForUpdate(item.getIdChiTietSanPham())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

                if (sp.getSoLuongTon() < item.getSoLuong()) {
                    throw new RuntimeException("Sản phẩm " + sp.getMaChiTietSanPham() + " không đủ số lượng!");
                }

                HoaDonChiTiet hdct = new HoaDonChiTiet();
                hdct.setIdHoaDon(hd);
                hdct.setIdSpct(sp);
                hdct.setSoLuong(item.getSoLuong());
                hdct.setDonGia(item.getDonGia());
                hdct.setThanhTien(item.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong())));
                hdctRepo.save(hdct);
            }
        }

        String actionName = loaiDon == 1 ? "Xác nhận đặt hàng" : "Tạo đơn giao hàng tại quầy";
        String note = loaiDon == 1 ? "Khách hàng hoàn tất mua tại quầy" : "Tạo đơn POS - chờ xác nhận để giao hàng";
        ghiLichSu(hd, hd.getTrangThai(), actionName, note);
    }

    // =================================================================
    // 6. LUỒNG ONLINE: TẠO HÓA ĐƠN MỚI TỪ ĐẦU
    // =================================================================
    @Transactional
    public Integer taoHoaDonMoi(CreateOrderRequest req) {
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon(generateMaHoaDon());
        hd.setNgayTao(LocalDateTime.now());
        hd.setLoaiDon(req.getLoaiDon());
        hd.setTongTienGoc(req.getTongTienHang());
        hd.setGhiChu(req.getGhiChu());
        hd.setTrangThai(0); // 0: Chờ xác nhận/Thanh toán
        hd.setNgayCapNhat(LocalDateTime.now());

        // Xử lý Voucher
        BigDecimal tienGiam = xuLyVoucher(req, hd);

        // Tính tiền
        BigDecimal phiShip = req.getPhiShip() != null ? req.getPhiShip() : BigDecimal.ZERO;
        hd.setPhiVanChuyen(phiShip);
        BigDecimal tongCuoiCung = req.getTongTienHang().add(phiShip).subtract(tienGiam);
        if (tongCuoiCung.compareTo(BigDecimal.ZERO) < 0) tongCuoiCung = BigDecimal.ZERO;
        hd.setTongTienThanhToan(tongCuoiCung);

        // Nhân viên
        if (req.getLoaiDon() == 0) {
            NhanVien nvMacDinh = nhanVienRepo.findById(1).orElse(null);
            hd.setIdNhanVien(nvMacDinh);
        } else {
            hd.setIdNhanVien(getNhanVienDangLogin());
        }

        // --- XỬ LÝ KHÁCH HÀNG & LẤY EMAIL ĐỂ GỬI ---
        String emailNhan = req.getEmailNguoiNhan(); // Lấy từ form khách nhập (Guest)

        if (req.getIdKhachHang() != null) {
            // Khách quen (Đã login)
            KhachHang kh = khachHangRepo.findById(req.getIdKhachHang()).orElse(null);
            if (kh != null) {
                hd.setIdKhachHang(kh);
                hd.setTenKhachHang(kh.getTenKhachHang());
                hd.setSoDienThoai(kh.getSoDienThoai());
                hd.setDiaChiKhachHang(req.getDiaChiGiaoHang());
                // Nếu form không nhập email mới, ưu tiên lấy email từ tài khoản
                if (emailNhan == null || emailNhan.isEmpty()) {
                    emailNhan = kh.getEmail();
                }
            }
        } else {
            // Khách lẻ (Guest)
            hd.setIdKhachHang(null);
            hd.setTenKhachHang(req.getTenNguoiNhan());
            hd.setSoDienThoai(req.getSdtNguoiNhan());
            hd.setDiaChiKhachHang(req.getDiaChiGiaoHang());
        }

        // [QUAN TRỌNG]: Đã xóa dòng hd.setEmail(emailNhan) để tránh lỗi
        // Chúng ta dùng biến local `emailNhan` để gửi mail ngay bên dưới.

        HoaDon savedHd = hoaDonRepo.save(hd);

        // Lưu sản phẩm chi tiết
        if (req.getSanPhamChiTiet() != null) {
            for (org.example.chocostyle_datn.model.Request.CartItemRequest item : req.getSanPhamChiTiet()) {
                ChiTietSanPham sp = spctRepo.findById(item.getIdChiTietSanPham()).orElseThrow();
                HoaDonChiTiet hdct = new HoaDonChiTiet();
                hdct.setIdHoaDon(savedHd);
                hdct.setIdSpct(sp);
                hdct.setSoLuong(item.getSoLuong());
                hdct.setDonGia(item.getDonGia());
                hdct.setThanhTien(item.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong())));
                hdctRepo.save(hdct);
            }
        }

        ghiLichSu(savedHd, 0, "Tạo đơn hàng", req.getGhiChu());

        // --- GỬI MAIL XÁC NHẬN NGAY LẬP TỨC (CHO CẢ COD VÀ VNPAY) ---
        if (emailNhan != null && !emailNhan.isEmpty()) {
            emailService.sendOrderConfirmation(emailNhan, savedHd);
        }

        return savedHd.getId();
    }

    // =================================================================
    // 2. XỬ LÝ THANH TOÁN THÀNH CÔNG (CHỈ ĐỔI TRẠNG THÁI)
    // =================================================================
    @Transactional
    public void xuLyThanhToanThanhCong(Integer idHoaDon, String maGiaoDichVnp) {
        HoaDon hd = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        if (hd.getTrangThai() == 0) {
            hd.setTrangThai(1); // Đã thanh toán / Chờ giao hàng
            hd.setNgayThanhToan(LocalDateTime.now());
            hd.setNgayCapNhat(LocalDateTime.now());

            // Lưu lịch sử giao dịch
            PhuongThucThanhToan vnpay = ptttRepo.findById(2).orElse(null);
            ThanhToan tt = new ThanhToan();
            tt.setIdHoaDon(hd);
            tt.setIdPttt(vnpay);
            tt.setSoTien(hd.getTongTienThanhToan());
            tt.setMaGiaoDich(maGiaoDichVnp);
            tt.setTrangThai(1);
            tt.setThoiGianThanhToan(LocalDateTime.now());
            thanhToanRepo.save(tt);

            ghiLichSu(hd, 1, "Thanh toán Online", "VNPAY Success: " + maGiaoDichVnp);
            hoaDonRepo.save(hd);

            // [QUAN TRỌNG]: Đã xóa đoạn gửi mail ở đây
            // Lý do: Mail đã được gửi lúc tạo đơn (taoHoaDonMoi) rồi.
            // Tránh gửi 2 lần và tránh lỗi hd.getEmail() không tồn tại.
        }
    }

    // =================================================================
    // 7. HOÀN TIỀN
    // =================================================================
    @Transactional
    public void hoanTien(RefundRequest req) {
        HoaDon hd = hoaDonRepo.findById(req.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        PhuongThucThanhToan pttt = ptttRepo.findById(1)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy phương thức thanh toán (ID=1)"));

        ThanhToan refund = new ThanhToan();
        refund.setIdHoaDon(hd);
        refund.setIdPttt(pttt);
        refund.setSoTien(req.getSoTien());
        refund.setLoaiGiaoDich(2);
        refund.setTrangThai(1);
        refund.setThoiGianThanhToan(LocalDateTime.now());
        refund.setGhiChu(req.getGhiChu());
        refund.setMaGiaoDich("REFUND-" + System.currentTimeMillis());

        thanhToanRepo.save(refund);

        ghiLichSu(hd, hd.getTrangThai(), "Hoàn tiền cho khách",
                "Hoàn " + req.getSoTien() + " - Lý do: " + req.getGhiChu());
    }

    // =================================================================
    // 8. XÓA ĐƠN NHÁP (Dùng khi đóng Tab bán hàng tại quầy)
    // =================================================================
    @Transactional
    public void xoaDonQuay(Integer id) {
        HoaDon hd = hoaDonRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        List<LichSuHoaDon> lichSus = lichSuRepo.findByIdHoaDon_IdOrderByThoiGianDesc(id);
        if (!lichSus.isEmpty()) {
            lichSuRepo.deleteAll(lichSus);
        }

        List<HoaDonChiTiet> chiTiets = hdctRepo.findByIdHoaDon_Id(id);
        if (!chiTiets.isEmpty()) {
            for (HoaDonChiTiet ct : chiTiets) {
                ChiTietSanPham sp = ct.getIdSpct();
                if (sp != null) {
                    sp.setSoLuongTon(sp.getSoLuongTon() + ct.getSoLuong());
                    spctRepo.save(sp);
                }
            }
            hdctRepo.deleteAll(chiTiets);
        }

        List<ThanhToan> thanhToans = thanhToanRepo.findByIdHoaDon_Id(id);
        if (!thanhToans.isEmpty()) {
            thanhToanRepo.deleteAll(thanhToans);
        }

        hoaDonRepo.delete(hd);
    }

    // =================================================================
    // CÁC HÀM PHỤ TRỢ (HELPER METHODS)
    // =================================================================
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
            case 1:
                return "Xác nhận đơn hàng";
            case 2:
                return "Đã giao cho vận chuyển";
            case 3:
                return "Đang giao hàng";
            case 4:
                return "Giao hàng thành công";
            case 5:
                return "Đã hủy đơn hàng";
            default:
                return "Cập nhật trạng thái";
        }
    }

    private BigDecimal tinhToanGiamGia(PhieuGiamGia voucher, BigDecimal tongTienHang) {
        if (voucher == null) return BigDecimal.ZERO;

        BigDecimal tienGiam = BigDecimal.ZERO;
        String loai = voucher.getLoaiGiam();

        if (loai != null && (loai.equalsIgnoreCase("Phần trăm") || loai.contains("%") || loai.equalsIgnoreCase("PERCENT") || loai.equalsIgnoreCase("PHAN_TRAM") || loai.equals("0"))) {
            tienGiam = tongTienHang.multiply(voucher.getGiaTri()).divide(BigDecimal.valueOf(100));
            if (voucher.getGiaTriToiDa() != null && tienGiam.compareTo(voucher.getGiaTriToiDa()) > 0) {
                tienGiam = voucher.getGiaTriToiDa();
            }
        } else {
            tienGiam = voucher.getGiaTri();
        }
        return tienGiam;
    }

    private String generateMaHoaDon() {
        HoaDon lastHoaDon = hoaDonRepo.findTopByOrderByIdDesc();
        if (lastHoaDon == null) return "HD001";
        try {
            String lastMa = lastHoaDon.getMaHoaDon();
            String numberPart = lastMa.substring(2);
            int nextNumber = Integer.parseInt(numberPart) + 1;
            return String.format("HD%03d", nextNumber);
        } catch (Exception e) {
            return "HD" + System.currentTimeMillis();
        }
    }

    private NhanVien getNhanVienDangLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("Chưa đăng nhập hoặc phiên làm việc đã hết hạn!");
        }

        String username = authentication.getName(); // Đây thường là email hoặc username từ Token

        return nhanVienRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên đang đăng nhập trong hệ thống"));
    }

    @Transactional
    public void capNhatSoLuongTamThoi(Integer idChiTietSanPham, Integer soLuongThayDoi) {
        ChiTietSanPham sp = spctRepo.findByIdForUpdate(idChiTietSanPham)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        if (soLuongThayDoi < 0 && sp.getSoLuongTon() < Math.abs(soLuongThayDoi)) {
            throw new RuntimeException("Không đủ tồn kho!");
        }

        sp.setSoLuongTon(sp.getSoLuongTon() + soLuongThayDoi);
        spctRepo.save(sp);
    }

    // =================================================================
    // 9. AUTO XÓA HÓA ĐƠN NHÁP LÚC 00:00
    // =================================================================
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void autoXoaHoaDonNhacMoiNgay() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        List<HoaDon> hoaDonNhac = hoaDonRepo
                .findByLoaiDonAndTrangThaiAndNgayTaoBefore(
                        1,
                        0,
                        startOfToday
                );

        for (HoaDon hd : hoaDonNhac) {
            xoaDonQuay(hd.getId());
        }

        System.out.println("Đã tự động xóa " + hoaDonNhac.size() + " hóa đơn nháp lúc 00:00");
    }

    // =================================================================
    // 10. THÊM SẢN PHẨM VÀO ĐƠN NHÁP (GHI LOG)
    // =================================================================
    @Transactional
    public void themSanPhamVaoDonNhap(Integer idHoaDon, Integer idSpct, int soLuongThem) {
        HoaDon hd = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        ChiTietSanPham sp = spctRepo.findById(idSpct)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

        if (sp.getSoLuongTon() < soLuongThem) {
            throw new RuntimeException("Sản phẩm không đủ tồn kho!");
        }

        // Kiểm tra xem SP này đã có trong đơn chưa
        HoaDonChiTiet hdct = hdctRepo.findByIdHoaDon_Id(idHoaDon).stream()
                .filter(ct -> ct.getIdSpct().getId().equals(idSpct))
                .findFirst()
                .orElse(null);

        if (hdct != null) {
            // Nếu đã có -> Cộng dồn số lượng
            hdct.setSoLuong(hdct.getSoLuong() + soLuongThem);
            hdct.setThanhTien(hdct.getDonGia().multiply(BigDecimal.valueOf(hdct.getSoLuong())));
        } else {
            // Nếu chưa có -> Tạo mới
            hdct = new HoaDonChiTiet();
            hdct.setIdHoaDon(hd);
            hdct.setIdSpct(sp);
            hdct.setSoLuong(soLuongThem);
            hdct.setDonGia(sp.getGiaBan()); // Lấy giá bán hiện tại
            hdct.setThanhTien(sp.getGiaBan().multiply(BigDecimal.valueOf(soLuongThem)));
        }
        hdctRepo.save(hdct);

        // --- GHI LỊCH SỬ THAO TÁC ---
        // Lấy tên SP, màu sắc, size để ghi chú cho rõ ràng
        String tenSp = sp.getIdSanPham() != null ? sp.getIdSanPham().getTenSp() : "Sản phẩm";
        String mauSac = sp.getIdMauSac() != null ? sp.getIdMauSac().getTenMauSac() : "";
        String kichCo = sp.getIdKichCo() != null ? sp.getIdKichCo().getTenKichCo() : "";

        String ghiChu = String.format("Thêm %d x [%s - %s - %s] vào giỏ hàng", soLuongThem, tenSp, mauSac, kichCo);

        ghiLichSu(hd, hd.getTrangThai(), "Thêm sản phẩm", ghiChu);
    }

    // =================================================================
    // 11. XÓA SẢN PHẨM KHỎI ĐƠN NHÁP DỰA VÀO ID SẢN PHẨM CHI TIẾT (GHI LOG)
    // =================================================================
    @Transactional
    public void xoaSanPhamKhoiDonNhap(Integer idHoaDon, Integer idSpct) {
        HoaDon hd = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // Tìm dòng sản phẩm đó trong giỏ hàng hiện tại
        HoaDonChiTiet hdct = hdctRepo.findByIdHoaDon_Id(idHoaDon).stream()
                .filter(ct -> ct.getIdSpct().getId().equals(idSpct))
                .findFirst()
                .orElse(null);

        if (hdct != null) {
            ChiTietSanPham sp = hdct.getIdSpct();
            String tenSp = sp.getIdSanPham() != null ? sp.getIdSanPham().getTenSp() : "Sản phẩm";
            String mauSac = sp.getIdMauSac() != null ? sp.getIdMauSac().getTenMauSac() : "";
            String kichCo = sp.getIdKichCo() != null ? sp.getIdKichCo().getTenKichCo() : "";

            // Xóa khỏi DB để giỏ hàng trống
            hdctRepo.delete(hdct);

            // --- GHI LỊCH SỬ THAO TÁC RÕ RÀNG ---
            String ghiChu = String.format("Khách bỏ chọn, xóa toàn bộ [%s - %s - %s] khỏi đơn", tenSp, mauSac, kichCo);
            ghiLichSu(hd, hd.getTrangThai(), "Bỏ sản phẩm", ghiChu);
        }
    }

    // =================================================================
    // 12. TRA CỨU ĐƠN HÀNG (CÓ CHECK QUYỀN SỞ HỮU)
    // =================================================================
    @Transactional(readOnly = true)
    public TraCuuDonHangResponse traCuuDonHang(String maDonHang) {
        // 1. Tìm hóa đơn theo mã
        HoaDon hd = hoaDonRepo.findByMaHoaDon(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã: " + maDonHang));

        // 2. CHECK QUYỀN SỞ HỮU (MỚI THÊM)
        // Lấy thông tin người đang đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("Vui lòng đăng nhập để tra cứu đơn hàng!");
        }

        String email = auth.getName();
        KhachHang currentKhach = khachHangRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin khách hàng!"));

        // So sánh: Nếu hóa đơn không có khách hàng (khách lẻ) HOẶC ID khách hàng không khớp
        if (hd.getIdKhachHang() == null || !hd.getIdKhachHang().getId().equals(currentKhach.getId())) {
            throw new RuntimeException("Bạn không có quyền truy cập đơn hàng này!");
        }

        // 3. Nếu khớp, trả về dữ liệu như cũ
        TraCuuDonHangResponse response = new TraCuuDonHangResponse();
        response.setId(hd.getId());
        response.setMaDonHang(hd.getMaHoaDon());
        response.setNgayTao(hd.getNgayTao());

        // Map trạng thái cho Vue Timeline
        String trangThaiVue = switch (hd.getTrangThai()) {
            case 0 -> "PENDING";
            case 1 -> "PROCESSING";
            case 2, 3 -> "SHIPPING";
            case 4 -> "DELIVERED";
            case 5 -> "CANCELLED";
            default -> "PENDING";
        };
        response.setTrangThai(trangThaiVue);

        // ... (Giữ nguyên phần map dữ liệu còn lại bên dưới của bạn) ...
        response.setNguoiNhan(hd.getTenKhachHang());
        response.setSoDienThoai(hd.getSoDienThoai());
        response.setDiaChi(hd.getDiaChiKhachHang());

        response.setTongTienHang(hd.getTongTienGoc());
        response.setPhiVanChuyen(hd.getPhiVanChuyen() != null ? hd.getPhiVanChuyen() : BigDecimal.ZERO);
        response.setTienGiamGia(hd.getSoTienGiam() != null ? hd.getSoTienGiam() : BigDecimal.ZERO);
        response.setTongTienThanhToan(hd.getTongTienThanhToan());

        String phuongThuc = "Thanh toán khi nhận hàng (COD)";
        List<ThanhToan> thanhToans = thanhToanRepo.findByIdHoaDon_Id(hd.getId());
        if (!thanhToans.isEmpty() && thanhToans.get(0).getIdPttt() != null) {
            phuongThuc = thanhToans.get(0).getIdPttt().getTenPttt();
        }
        response.setPhuongThucThanhToan(phuongThuc);

        // Map sản phẩm
        List<HoaDonChiTiet> chiTiets = hdctRepo.findByIdHoaDon_Id(hd.getId());
        List<SanPhamTraCuuDto> sanPhamList = chiTiets.stream().map(ct -> {
            SanPhamTraCuuDto dto = new SanPhamTraCuuDto();
            if (ct.getIdSpct() != null && ct.getIdSpct().getIdSanPham() != null) {
                dto.setTenSp(ct.getIdSpct().getIdSanPham().getTenSp());
                dto.setHinhAnh(ct.getIdSpct().getIdSanPham().getHinhAnh() != null ? ct.getIdSpct().getIdSanPham().getHinhAnh() : "");
                dto.setMauSac(ct.getIdSpct().getIdMauSac() != null ? ct.getIdSpct().getIdMauSac().getTenMauSac() : "-");
                dto.setKichCo(ct.getIdSpct().getIdKichCo() != null ? ct.getIdSpct().getIdKichCo().getTenKichCo() : "-");
            } else {
                dto.setTenSp("Sản phẩm đã bị xóa");
                dto.setHinhAnh("");
                dto.setMauSac("-");
                dto.setKichCo("-");
            }
            dto.setSoLuong(ct.getSoLuong());
            dto.setGiaBan(ct.getDonGia());
            return dto;
        }).collect(Collectors.toList());

        response.setSanPhamList(sanPhamList);

        return response;
    }

    @Transactional(readOnly = true)
    public List<HoaDonResponse> getMyOrders() {
        // 1. Lấy Email từ Security Context (Token)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Chỉ tìm khách hàng theo Email
        KhachHang kh = khachHangRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy thông tin khách hàng với email: " + email));

        // 3. Lấy danh sách hóa đơn theo ID Khách hàng vừa tìm được
        List<HoaDon> list = hoaDonRepo.findByIdKhachHang_IdOrderByIdAsc(kh.getId());

        // 4. Convert sang Response
        return list.stream().map(hd -> HoaDonResponse.builder()
                .id(hd.getId())
                .maHoaDon(hd.getMaHoaDon())
                .tenKhachHang(hd.getTenKhachHang())
                .soDienThoai(hd.getSoDienThoai())
                .tongTien(hd.getTongTienThanhToan())
                .loaiDon(hd.getLoaiDon())
                .trangThai(hd.getTrangThai())
                .ngayTao(hd.getNgayTao())
                .build()).collect(Collectors.toList());
    }

    // Trong HoaDonService.java
    @Transactional
    public void capNhatThongTinGiaoHang(Integer id, String ten, String sdt, String diaChi) {
        HoaDon hd = hoaDonRepo.findById(id).orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        if (hd.getTrangThai() != 0) {
            throw new RuntimeException("Chỉ được phép sửa thông tin khi đơn hàng chờ xác nhận!");
        }

        String oldInfo = String.format("Cũ: %s - %s - %s", hd.getTenKhachHang(), hd.getSoDienThoai(), hd.getDiaChiKhachHang());

        hd.setTenKhachHang(ten);
        hd.setSoDienThoai(sdt);
        hd.setDiaChiKhachHang(diaChi);
        hoaDonRepo.save(hd);

        ghiLichSu(hd, 0, "Sửa thông tin nhận hàng", oldInfo + " -> Mới: " + ten + " - " + sdt + " - " + diaChi);
    }

    // Trong HoaDonService.java
    @Transactional
    public void capNhatSoLuongChiTiet(Integer idHoaDon, Integer idSpct, int soLuongMoi) {
        HoaDon hd = hoaDonRepo.findById(idHoaDon).orElseThrow();
        if (hd.getTrangThai() != 0) {
            throw new RuntimeException("Chỉ được sửa số lượng khi đơn hàng chờ xác nhận!");
        }

        if (soLuongMoi < 1) {
            throw new RuntimeException("Số lượng sản phẩm phải >= 1. Không thể để rỗng!");
        }

        HoaDonChiTiet hdct = hdctRepo.findByIdHoaDon_Id(idHoaDon).stream()
                .filter(ct -> ct.getIdSpct().getId().equals(idSpct))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong đơn"));

        int oldQty = hdct.getSoLuong();
        hdct.setSoLuong(soLuongMoi);
        hdct.setThanhTien(hdct.getDonGia().multiply(BigDecimal.valueOf(soLuongMoi)));
        hdctRepo.save(hdct);

        // Tính lại tổng tiền hóa đơn
        tinhLaiTongTienHoaDon(hd);

        ghiLichSu(hd, 0, "Sửa số lượng SP", "Sản phẩm ID " + idSpct + " thay đổi SL từ " + oldQty + " -> " + soLuongMoi);
    }

    // Hàm dùng chung cho Case 3 và 4
    private void tinhLaiTongTienHoaDon(HoaDon hd) {
        List<HoaDonChiTiet> chiTiets = hdctRepo.findByIdHoaDon_Id(hd.getId());
        BigDecimal tongTienMoi = chiTiets.stream()
                .map(HoaDonChiTiet::getThanhTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        hd.setTongTienGoc(tongTienMoi);
        BigDecimal phiShip = hd.getPhiVanChuyen() != null ? hd.getPhiVanChuyen() : BigDecimal.ZERO;
        BigDecimal tienGiam = hd.getSoTienGiam() != null ? hd.getSoTienGiam() : BigDecimal.ZERO;

        hd.setTongTienThanhToan(tongTienMoi.add(phiShip).subtract(tienGiam));
        hoaDonRepo.save(hd);
    }

    // Trong HoaDonService.java
    @Transactional
    public void thayDoiGiaChiTiet(Integer idHoaDon, Integer idSpct, BigDecimal giaMoi) {
        HoaDon hd = hoaDonRepo.findById(idHoaDon).orElseThrow();
        if (hd.getTrangThai() != 0) {
            throw new RuntimeException("Chỉ được đổi giá khi chờ xác nhận!");
        }

        HoaDonChiTiet hdct = hdctRepo.findByIdHoaDon_Id(idHoaDon).stream()
                .filter(ct -> ct.getIdSpct().getId().equals(idSpct))
                .findFirst()
                .orElseThrow();

        BigDecimal giaCu = hdct.getDonGia();

        // Nếu entity HoaDonChiTiet có trường ghi chú, bạn lưu vào đó. Nếu không, chỉ cần update đơn giá.
        hdct.setDonGia(giaMoi);
        hdct.setThanhTien(giaMoi.multiply(BigDecimal.valueOf(hdct.getSoLuong())));
        hdctRepo.save(hdct);

        tinhLaiTongTienHoaDon(hd);

        // Ghi lịch sử đặc biệt để FE có thể bắt chuỗi này hiển thị màu vàng
        String message = String.format("[PRICE_CHANGE] ID %d: Từ %s thành %s", idSpct, giaCu.toString(), giaMoi.toString());
        ghiLichSu(hd, 0, "Thay đổi giá sản phẩm", message);
    }
}
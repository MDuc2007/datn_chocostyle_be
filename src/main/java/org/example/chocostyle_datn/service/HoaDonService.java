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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

    // =================================================================
    // 1. LẤY CHI TIẾT (GET DETAIL) - CHỐNG NULL
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

        // Nếu hủy đơn (Trạng thái 5) -> Cộng lại số lượng tồn kho
        if (req.getTrangThaiMoi() == 5) {
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

    // =================================================================
    // 4. LUỒNG BÁN HÀNG TẠI QUẦY: TẠO TAB HÓA ĐƠN TRỐNG (NHÁP)
    // =================================================================
    @Transactional
    public HoaDon taoHoaDonChoTaiQuay(Integer idNhanVien) {
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon(generateMaHoaDon());
        hd.setNgayTao(LocalDateTime.now());
        hd.setNgayCapNhat(LocalDateTime.now());

        hd.setLoaiDon(1); // 1 = Tại quầy
        hd.setTrangThai(0); // 0 = Chờ xác nhận (Đơn nháp)

        hd.setTongTienGoc(BigDecimal.ZERO);
        hd.setTongTienThanhToan(BigDecimal.ZERO);
        hd.setPhiVanChuyen(BigDecimal.ZERO);
        hd.setSoTienGiam(BigDecimal.ZERO);

        if (idNhanVien != null) {
            NhanVien nv = nhanVienRepo.findById(idNhanVien)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên!"));
            hd.setIdNhanVien(nv);
        } else {
            throw new RuntimeException("ID Nhân viên không được để trống!");
        }

        HoaDon savedHd = hoaDonRepo.save(hd);
        ghiLichSu(savedHd, 0, "Tạo đơn nháp", "Tạo tab hóa đơn mới tại quầy");
        return savedHd;
    }

    // =================================================================
    // 5. LUỒNG BÁN HÀNG TẠI QUẦY: XÁC NHẬN ĐẶT HÀNG (CẬP NHẬT ĐƠN NHÁP)
    // =================================================================
    @Transactional
    public void xacNhanDatHangTaiQuay(Integer idHoaDon, CreateOrderRequest req) {
        HoaDon hd = hoaDonRepo.findById(idHoaDon)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn nháp!"));

        hd.setTongTienGoc(req.getTongTienHang());
        hd.setGhiChu(req.getGhiChu());

        // Xử lý Voucher
        BigDecimal tienGiam = BigDecimal.ZERO;
        if (req.getMaVoucher() != null && !req.getMaVoucher().trim().isEmpty()) {
            PhieuGiamGia voucher = pggRepo.findByMaPggForUpdate(req.getMaVoucher())
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại!"));

            // (Bạn có thể ném logic check ngày, số lượng voucher vào đây tương tự hàm taoHoaDonMoi)
            tienGiam = tinhToanGiamGia(voucher, req.getTongTienHang());
            hd.setIdPhieuGiamGia(voucher);
            hd.setSoTienGiam(tienGiam);
            voucher.setSoLuongDaDung(voucher.getSoLuongDaDung() + 1);
            pggRepo.save(voucher);
        } else {
            hd.setSoTienGiam(BigDecimal.ZERO);
        }

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
                hd.setTenKhachHang(kh.getTenKhachHang());
                hd.setSoDienThoai(kh.getSoDienThoai());
                String diaChiMacDinh = kh.getListDiaChiObj().stream()
                        .filter(dc -> dc.getMacDinh() != null && dc.getMacDinh()) // Tìm địa chỉ có macDinh = true
                        .map(dc -> dc.getDiaChiCuThe() + ", " + dc.getPhuong() + ", " + dc.getQuan() + ", " + dc.getThanhPho())
                        .findFirst()
                        .orElse("Chưa có địa chỉ mặc định"); // Trường hợp khách chưa có địa chỉ nào


                hd.setDiaChiKhachHang(diaChiMacDinh);
            }
        } else {
            hd.setTenKhachHang("Khách lẻ");
        }

        // Cập nhật trạng thái thành Hoàn Thành (Tại quầy thanh toán xong là lấy hàng đi luôn)
        hd.setTrangThai(4);
        hd.setNgayThanhToan(LocalDateTime.now());
        hd.setNgayCapNhat(LocalDateTime.now());

        hoaDonRepo.save(hd);

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

                sp.setSoLuongTon(sp.getSoLuongTon() - item.getSoLuong());
                spctRepo.save(sp);

                HoaDonChiTiet hdct = new HoaDonChiTiet();
                hdct.setIdHoaDon(hd);
                hdct.setIdSpct(sp);
                hdct.setSoLuong(item.getSoLuong());
                hdct.setDonGia(item.getDonGia());
                hdct.setThanhTien(item.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong())));
                hdctRepo.save(hdct);
            }
        }

        ghiLichSu(hd, 4, "Xác nhận đặt hàng", "Khách hàng hoàn tất mua tại quầy");
    }

    private NhanVien getNhanVienDangLogin() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return nhanVienRepo.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
    }

    // =================================================================
    // 6. LUỒNG ONLINE: TẠO HÓA ĐƠN MỚI TỪ ĐẦU
    // =================================================================
    @Transactional
    public Integer taoHoaDonMoi(org.example.chocostyle_datn.model.Request.CreateOrderRequest req) {
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon(generateMaHoaDon());
        hd.setNgayTao(LocalDateTime.now());
        hd.setLoaiDon(req.getLoaiDon());
        hd.setTongTienGoc(req.getTongTienHang());
        hd.setGhiChu(req.getGhiChu());

        // CHỈ GÁN 0 NHƯ YÊU CẦU
        hd.setTrangThai(0);
        hd.setNgayCapNhat(LocalDateTime.now());

        BigDecimal tienGiam = BigDecimal.ZERO;
        if (req.getMaVoucher() != null && !req.getMaVoucher().trim().isEmpty()) {
            PhieuGiamGia voucher = pggRepo.findByMaPggForUpdate(req.getMaVoucher())
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại!"));

            LocalDate today = LocalDate.now();
            if (voucher.getTrangThai() == null || voucher.getTrangThai() != 1)
                throw new RuntimeException("Mã giảm giá hiện không hoạt động!");
            if (today.isBefore(voucher.getNgayBatDau()))
                throw new RuntimeException("Mã giảm giá chưa đến thời gian sử dụng!");
            if (today.isAfter(voucher.getNgayKetThuc())) throw new RuntimeException("Mã giảm giá đã hết hạn!");
            if (voucher.getSoLuong() <= voucher.getSoLuongDaDung())
                throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng!");
            if (voucher.getDieuKienDonHang() != null && req.getTongTienHang().compareTo(voucher.getDieuKienDonHang()) < 0) {
                throw new RuntimeException("Đơn hàng chưa đủ điều kiện áp dụng (Tối thiểu: " + voucher.getDieuKienDonHang() + ")");
            }

            if ("PERSONAL".equalsIgnoreCase(voucher.getKieuApDung())) {
                if (req.getIdKhachHang() == null)
                    throw new RuntimeException("Voucher này chỉ áp dụng cho khách hàng cụ thể!");
                PhieuGiamGiaKhachHang pggKh = pggKhRepository.findByPhieuGiamGiaIdAndKhachHangId(voucher.getId(), req.getIdKhachHang());
                if (pggKh == null) throw new RuntimeException("Khách hàng không được cấp voucher này!");
                if (pggKh.getDaSuDung()) throw new RuntimeException("Voucher này đã được sử dụng!");
                pggKh.setDaSuDung(true);
                pggKhRepository.save(pggKh);
            }

            tienGiam = tinhToanGiamGia(voucher, req.getTongTienHang());
            hd.setIdPhieuGiamGia(voucher);
            hd.setSoTienGiam(tienGiam);
            voucher.setSoLuongDaDung(voucher.getSoLuongDaDung() + 1);
            pggRepo.save(voucher);
        } else {
            hd.setSoTienGiam(BigDecimal.ZERO);
        }

        BigDecimal phiShip = req.getPhiShip() != null ? req.getPhiShip() : BigDecimal.ZERO;
        hd.setPhiVanChuyen(phiShip);
        BigDecimal tongCuoiCung = req.getTongTienHang().add(phiShip).subtract(tienGiam);
        if (tongCuoiCung.compareTo(BigDecimal.ZERO) < 0) tongCuoiCung = BigDecimal.ZERO;
        hd.setTongTienThanhToan(tongCuoiCung);

//        if (req.getIdNhanVien() != null) {
//            hd.setIdNhanVien(nhanVienRepo.findById(req.getIdNhanVien()).orElse(null));
//        }

        // Set nhân viên đang đăng nhập
        hd.setIdNhanVien(getNhanVienDangLogin());

        if (req.getIdKhachHang() != null) {
            KhachHang kh = khachHangRepo.findById(req.getIdKhachHang()).orElse(null);
            if (kh != null) {
                hd.setIdKhachHang(kh);
                hd.setTenKhachHang(kh.getTenKhachHang());
                hd.setSoDienThoai(kh.getSoDienThoai());
                String diaChiMacDinh = kh.getListDiaChiObj().stream()
                        .filter(dc -> dc.getMacDinh() != null && dc.getMacDinh()) // Tìm địa chỉ có macDinh = true
                        .map(dc -> dc.getDiaChiCuThe() + ", " + dc.getPhuong() + ", " + dc.getQuan() + ", " + dc.getThanhPho())
                        .findFirst()
                        .orElse("Chưa có địa chỉ mặc định"); // Trường hợp khách chưa có địa chỉ nào


                hd.setDiaChiKhachHang(diaChiMacDinh);
            }
        } else {
            hd.setTenKhachHang("Khách lẻ");
        }

        HoaDon savedHd = hoaDonRepo.save(hd);

        if (req.getSanPhamChiTiet() != null) {
            for (org.example.chocostyle_datn.model.Request.CartItemRequest item : req.getSanPhamChiTiet()) {
                ChiTietSanPham sp = spctRepo.findByIdForUpdate(item.getIdChiTietSanPham())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm ID " + item.getIdChiTietSanPham() + " không tồn tại"));

                if (sp.getSoLuongTon() < item.getSoLuong()) {
                    throw new RuntimeException("Sản phẩm " + sp.getMaChiTietSanPham() + " không đủ số lượng tồn kho!");
                }

                sp.setSoLuongTon(sp.getSoLuongTon() - item.getSoLuong());
                spctRepo.save(sp);

                HoaDonChiTiet hdct = new HoaDonChiTiet();
                hdct.setIdHoaDon(savedHd);
                hdct.setIdSpct(sp);
                hdct.setSoLuong(item.getSoLuong());
                hdct.setDonGia(item.getDonGia());
                hdct.setThanhTien(item.getDonGia().multiply(BigDecimal.valueOf(item.getSoLuong())));
                hdctRepo.save(hdct);
            }
        }

        ghiLichSu(savedHd, savedHd.getTrangThai(), "Tạo mới đơn hàng", req.getGhiChu());

        return savedHd.getId();
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

        if (loai != null && (loai.equalsIgnoreCase("Phần trăm") || loai.contains("%") || loai.equalsIgnoreCase("PERCENT"))) {
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
}
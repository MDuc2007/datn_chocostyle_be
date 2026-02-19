
package org.example.chocostyle_datn.service;


import org.example.chocostyle_datn.entity.*;
import org.example.chocostyle_datn.model.Request.CreateOrderRequest;
import org.example.chocostyle_datn.model.Request.RefundRequest; // Nhớ import
import org.example.chocostyle_datn.model.Request.SearchHoaDonRequest;
import org.example.chocostyle_datn.model.Request.UpdateTrangThaiRequest;
import org.example.chocostyle_datn.model.Request.CartItemRequest;
import org.example.chocostyle_datn.model.Response.*;
import org.example.chocostyle_datn.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
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


    // [MỚI] Thêm Repo này để lấy phương thức thanh toán khi hoàn tiền
    @Autowired private PhuongThucThanhToanRepository ptttRepo;


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


                // Map thêm loaiGiaoDich để FE biết đâu là hoàn tiền
                .thanhToanList(thanhToans.stream().map(tt -> HoaDonThanhToanResponse.builder()
                        .phuongThuc((tt.getIdPttt() != null) ? tt.getIdPttt().getTenPttt() : "Khác")
                        .soTien(tt.getSoTien())
                        .trangThai(tt.getTrangThai())
                        .thoiGian(tt.getThoiGianThanhToan() != null ? tt.getThoiGianThanhToan().toString() : "")
                        .loaiGiaoDich(tt.getLoaiGiaoDich()) // [Quan trọng]
                        .ghiChu(tt.getGhiChu())
                        .maGiaoDich(tt.getMaGiaoDich())
                        .build()).collect(Collectors.toList()))
                .build();
    }


    // =================================================================
    // 3. CẬP NHẬT TRẠNG THÁI (PUT) - ĐÃ SỬA LỖI
    // =================================================================
    @Transactional
    public void updateStatus(Integer id, UpdateTrangThaiRequest req) {
        HoaDon hd = hoaDonRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));

        if (hd.getTrangThai() == 5) {
            throw new RuntimeException("Hóa đơn đã hủy, không thể cập nhật!");
        }

        // 1. Cập nhật trạng thái mới
        hd.setTrangThai(req.getTrangThaiMoi());
        hd.setNgayCapNhat(LocalDateTime.now());

        // --- [QUAN TRỌNG] SỬA LỖI TẠI ĐÂY ---
        // Phải gán ghi chú từ Request vào Hóa Đơn thì nó mới lưu lý do hủy/lý do cập nhật
        if (req.getGhiChu() != null && !req.getGhiChu().trim().isEmpty()) {
            hd.setGhiChu(req.getGhiChu());
        }
        // ------------------------------------

        if(req.getTrangThaiMoi() == 4) { // 4 = Hoàn thành
            hd.setNgayThanhToan(LocalDateTime.now());
        }

        // 2. Lưu hóa đơn (Lúc này mới lưu cả Status và Ghi chú mới vào DB)
        hoaDonRepo.save(hd);

        // 3. Ghi log lịch sử (Cái này bạn đã làm đúng rồi)
        ghiLichSu(hd, req.getTrangThaiMoi(), getActionName(req.getTrangThaiMoi()), req.getGhiChu());
    }


    // =================================================================
    // 4. LẤY DANH SÁCH (GET ALL)
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
    // 5. TẠO HÓA ĐƠN MỚI
    // =================================================================
    @Transactional
    public Integer taoHoaDonMoi(CreateOrderRequest req) {
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon(generateMaHoaDon());
        hd.setNgayTao(LocalDateTime.now());
        hd.setLoaiDon(req.getLoaiDon());
        hd.setTongTienGoc(req.getTongTienHang());
        hd.setGhiChu(req.getGhiChu());


        if (req.getLoaiDon() != null && req.getLoaiDon() == 1) {
            hd.setTrangThai(4);
            hd.setNgayThanhToan(LocalDateTime.now());
        } else {
            hd.setTrangThai(0);
        }


        // Xử lý Voucher
        BigDecimal tienGiam = BigDecimal.ZERO;
        if (req.getMaVoucher() != null && !req.getMaVoucher().trim().isEmpty()) {
            PhieuGiamGia voucher = pggRepo.findByMaPgg(req.getMaVoucher())
                    .orElseThrow(() -> new RuntimeException("Mã giảm giá '" + req.getMaVoucher() + "' không tồn tại!"));


            if (voucher.getSoLuong() <= voucher.getSoLuongDaDung()) {
                throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng!");
            }
            if (voucher.getDieuKienDonHang() != null && req.getTongTienHang().compareTo(voucher.getDieuKienDonHang()) < 0) {
                throw new RuntimeException("Đơn hàng chưa đủ điều kiện áp dụng (Tối thiểu: " + voucher.getDieuKienDonHang() + ")");
            }
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


        // Map Nhân viên & Khách
        if (req.getIdNhanVien() != null) {
            hd.setIdNhanVien(nhanVienRepo.findById(req.getIdNhanVien()).orElse(null));
        }
        if (req.getIdKhachHang() != null) {
            KhachHang kh = khachHangRepo.findById(req.getIdKhachHang()).orElse(null);
            if (kh != null) {
                hd.setIdKhachHang(kh);
                hd.setTenKhachHang(kh.getTenKhachHang());
                hd.setSoDienThoai(kh.getSoDienThoai());


                // ✅ SỬA TẠI ĐÂY: Lấy địa chỉ mặc định từ danh sách địa chỉ của khách
                String diaChiMacDinh = kh.getListDiaChiObj().stream()
                        .filter(dc -> dc.getMacDinh() != null && dc.getMacDinh()) // Tìm địa chỉ có macDinh = true
                        .map(dc -> dc.getDiaChiCuThe() + ", " + dc.getPhuong() + ", " + dc.getQuan() + ", " + dc.getThanhPho())
                        .findFirst()
                        .orElse("Chưa có địa chỉ mặc định"); // Trường hợp khách chưa có địa chỉ nào


                hd.setDiaChiKhachHang(diaChiMacDinh);
            }
        } else {
            hd.setTenKhachHang("Khách lẻ"); // Khách vãng lai
        }



        HoaDon savedHd = hoaDonRepo.save(hd);


        // Lưu sản phẩm
        if (req.getSanPhamChiTiet() != null) {
            for (CartItemRequest item : req.getSanPhamChiTiet()) {
                ChiTietSanPham sp = spctRepo.findById(item.getIdChiTietSanPham())
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
    // 6. XỬ LÝ HOÀN TIỀN (MỚI THÊM)
    // =================================================================
    @Transactional
    public void hoanTien(RefundRequest req) {
        // 1. Kiểm tra hóa đơn
        HoaDon hd = hoaDonRepo.findById(req.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại"));


        // 2. Lấy phương thức thanh toán (Mặc định Tiền mặt ID=1 hoặc lấy theo logic của bạn)
        PhuongThucThanhToan pttt = ptttRepo.findById(1)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy phương thức thanh toán (ID=1)"));


        // 3. Tạo giao dịch hoàn tiền
        ThanhToan refund = new ThanhToan();
        refund.setIdHoaDon(hd);
        refund.setIdPttt(pttt);
        refund.setSoTien(req.getSoTien()); // Lưu số tiền cần hoàn
        refund.setLoaiGiaoDich(2);         // QUAN TRỌNG: 2 = Hoàn tiền
        refund.setTrangThai(1);            // 1 = Thành công
        refund.setThoiGianThanhToan(LocalDateTime.now());
        refund.setGhiChu(req.getGhiChu());
        refund.setMaGiaoDich("REFUND-" + System.currentTimeMillis());


        thanhToanRepo.save(refund);


        // Ghi log lịch sử hóa đơn
        ghiLichSu(hd, hd.getTrangThai(), "Hoàn tiền cho khách",
                "Hoàn " + req.getSoTien() + " - Lý do: " + req.getGhiChu());
    }


    // --- CÁC HÀM PHỤ TRỢ ---


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


    private BigDecimal tinhToanGiamGia(PhieuGiamGia voucher, BigDecimal tongTienHang) {
        if (voucher == null) return BigDecimal.ZERO;
        BigDecimal tienGiam;
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


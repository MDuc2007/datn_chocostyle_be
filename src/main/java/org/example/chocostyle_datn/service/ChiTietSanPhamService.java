package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.*;
import org.example.chocostyle_datn.model.Request.ChiTietSanPhamRequest;
import org.example.chocostyle_datn.model.Response.ChiTietSanPhamResponse;
import org.example.chocostyle_datn.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChiTietSanPhamService {

    private final ChiTietSanPhamRepository repository;
    private final HinhAnhSanPhamRepository hinhAnhSanPhamRepository;

    private final SanPhamRepository sanPhamRepository;
    private final KichCoRepository kichCoRepository;
    private final MauSacRepository mauSacRepository;
    private final LoaiAoRepository loaiAoRepository;
    private final PhongCachMacRepository phongCachMacRepository;
    private final KieuDangRepository kieuDangRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;

    private final ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /* ================= GET ================= */

    private void broadcastPublicUpdate() {
        try {
            messagingTemplate.convertAndSend("/topic/public-updates", "UPDATED");
            System.out.println("🚀 Đã bắn tín hiệu cập nhật giá/voucher cho tất cả client!");
        } catch (Exception e) {
            System.out.println("❌ Lỗi gửi WebSocket Update: " + e.getMessage());
        }
    }

    public List<ChiTietSanPhamResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ChiTietSanPhamResponse getById(Integer id) {
        ChiTietSanPham ctsp = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm"));
        return mapToResponse(ctsp);
    }

    public Page<ChiTietSanPhamResponse> getSaleProducts(Pageable pageable) {
        return chiTietDotGiamGiaRepository.findActiveSaleProducts(pageable)
                .map(this::mapToResponse);
    }

    /* ================= CREATE ================= */

    @Transactional
    public ChiTietSanPhamResponse create(ChiTietSanPhamRequest data) {

        ChiTietSanPham ctsp = new ChiTietSanPham();

        SanPham sp = sanPhamRepository.findById(data.getIdSanPham())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));
        ctsp.setIdSanPham(sp);

        ctsp.setSoLuongTon(data.getSoLuongTon());
        autoUpdateTrangThai(ctsp);
        ctsp.setGiaNhap(data.getGiaNhap());
        ctsp.setGiaBan(data.getGiaBan());

        ctsp.setNgayTao(LocalDate.now());
        ctsp.setNguoiTao(data.getNguoiCapNhat());

        repository.save(ctsp);

        if (data.getHinhAnh() != null) {
            for (String url : data.getHinhAnh()) {
                HinhAnhSanPham img = new HinhAnhSanPham();
                img.setChiTietSanPham(ctsp);
                img.setUrlAnh(url);
                hinhAnhSanPhamRepository.save(img);
            }
        }

        // Cập nhật trạng thái sản phẩm cha
        updateParentSanPhamStatus(sp.getId());

        return mapToResponse(ctsp);
    }

    /* ================= UPDATE ================= */

    @Transactional
    public ChiTietSanPhamResponse update(Integer id, ChiTietSanPhamRequest data) {

        ChiTietSanPham ctsp = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết sản phẩm"));

        ctsp.setSoLuongTon(data.getSoLuongTon());
        autoUpdateTrangThai(ctsp);
        ctsp.setGiaNhap(data.getGiaNhap());
        ctsp.setGiaBan(data.getGiaBan());

        ctsp.setNgayCapNhat(LocalDate.now());
        ctsp.setNguoiCapNhat(data.getNguoiCapNhat());

        repository.save(ctsp);

        if (data.getHinhAnh() != null) {
            hinhAnhSanPhamRepository.deleteByChiTietSanPham_Id(ctsp.getId());
            for (String url : data.getHinhAnh()) {
                HinhAnhSanPham img = new HinhAnhSanPham();
                img.setChiTietSanPham(ctsp);
                img.setUrlAnh(url);
                hinhAnhSanPhamRepository.save(img);
            }
        }

        // Cập nhật trạng thái sản phẩm cha
        updateParentSanPhamStatus(ctsp.getIdSanPham().getId());
        broadcastPublicUpdate();
        return mapToResponse(ctsp);
    }

    /* ================= DELETE ================= */

    public void delete(Integer id) {
        ChiTietSanPham ctsp = repository.findById(id).orElse(null);
        if (ctsp != null) {
            Integer spId = ctsp.getIdSanPham().getId();
            repository.deleteById(id);
            updateParentSanPhamStatus(spId);
        }
    }

    public Page<ChiTietSanPhamResponse> getAll(
            Integer productId,
            String keyword,
            Integer mauSacId,
            Integer kichCoId,
            Integer trangThai,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        return repository.filter(
                productId,
                keyword,
                mauSacId,
                kichCoId,
                trangThai,
                minPrice,
                maxPrice,
                pageable
        ).map(this::mapToResponse);
    }

    public void changeStatusChiTietSanPham(
            Integer sanPhamId,
            Integer trangThai,
            String nguoiCapNhat
    ) {

        ChiTietSanPham sp = chiTietSanPhamRepository.findById(sanPhamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // Tính null an toàn và so sánh <= 0
        int sl = sp.getSoLuongTon() == null ? 0 : sp.getSoLuongTon();
        if (sl <= 0 && trangThai == 1) {
            throw new IllegalArgumentException("Sản phẩm đã hết hàng, không thể thay đổi trạng thái sang đang bán");
        }

        if (trangThai != 1 && trangThai != 2 && trangThai != 0) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        }

        sp.setTrangThai(trangThai);
        sp.setNgayCapNhat(LocalDate.now());
        sp.setNguoiCapNhat(nguoiCapNhat);

        chiTietSanPhamRepository.save(sp);

        updateParentSanPhamStatus(sp.getIdSanPham().getId());
        broadcastPublicUpdate();
    }
    public List<ChiTietSanPham> getDataExport(List<Integer> ids, Integer productId) {

        if (ids != null && !ids.isEmpty()) {
            return repository.findAllById(ids);
        }

        if (productId != null) {
            return repository.findByIdSanPham_Id(productId);
        }

        return repository.findAll();
    }

    // 🔴 SỬA LỖI 1: Tự động đổi về Hết hàng (0) nếu tồn kho <= 0
    private void autoUpdateTrangThai(ChiTietSanPham ctsp) {
        if (ctsp.getSoLuongTon() == null || ctsp.getSoLuongTon() <= 0) {
            ctsp.setTrangThai(0);
        } else {
            // Không sửa nếu nó đang bị buộc ngưng bán
            if (ctsp.getTrangThai() != null && ctsp.getTrangThai() == 2) return;
            ctsp.setTrangThai(1);
        }
        broadcastPublicUpdate();

    }

    // 🔴 HÀM MỚI: Đồng bộ trạng thái Sản Phẩm cha khi sửa Chi Tiết
    private void updateParentSanPhamStatus(Integer sanPhamId) {
        SanPham sp = sanPhamRepository.findById(sanPhamId).orElse(null);
        if (sp == null) return;

        List<ChiTietSanPham> ctList = chiTietSanPhamRepository.findByIdSanPham_Id(sanPhamId);
        int totalQuantity = ctList.stream()
                .mapToInt(ct -> ct.getSoLuongTon() == null ? 0 : ct.getSoLuongTon())
                .sum();

        if (totalQuantity <= 0) {
            sp.setTrangThai(0); // Hết hàng
        } else if (sp.getTrangThai() != null && sp.getTrangThai() != 2) {
            sp.setTrangThai(1); // Đang bán
        }
        sanPhamRepository.save(sp);
        broadcastPublicUpdate();
    }


    /* ================= MAP RESPONSE ================= */

    private ChiTietSanPhamResponse mapToResponse(ChiTietSanPham ctsp) {

        ChiTietSanPhamResponse res = new ChiTietSanPhamResponse();

        res.setId(ctsp.getId());
        res.setMaChiTietSanPham(ctsp.getMaChiTietSanPham());

        int sl = ctsp.getSoLuongTon() == null ? 0 : ctsp.getSoLuongTon();
        res.setSoLuongTon(sl);

        // Cập nhật trạng thái tự động để trả về FE
        Integer trangThai = ctsp.getTrangThai();
        if (trangThai != null && trangThai != 2) {
            trangThai = sl <= 0 ? 0 : 1;
        }
        res.setTrangThai(trangThai);

        res.setGiaNhap(ctsp.getGiaNhap());
        res.setGiaBan(ctsp.getGiaBan());

        res.setTenMauSac(ctsp.getIdMauSac().getTenMauSac());
        res.setTenKichCo(ctsp.getIdKichCo().getTenKichCo());
        res.setTenLoaiAo(ctsp.getIdLoaiAo().getTenLoai());
        res.setTenPhongCachMac(ctsp.getIdPhongCachMac().getTenPhongCach());
        res.setTenKieuDang(ctsp.getIdKieuDang().getTenKieuDang());
        res.setTenSanPham(ctsp.getIdSanPham().getTenSp());
        res.setMaSanPham(ctsp.getIdSanPham().getMaSp());
        res.setQrCode(ctsp.getQrCode());
        res.setQrImage(ctsp.getQrImage());

        res.setNguoiTao(ctsp.getNguoiTao());
        res.setNgayTao(ctsp.getNgayTao());
        res.setNgayCapNhat(ctsp.getNgayCapNhat());
        res.setNguoiCapNhat(ctsp.getNguoiCapNhat());

        res.setHinhAnh(
                hinhAnhSanPhamRepository.findByChiTietSanPham_Id(ctsp.getId())
                        .stream()
                        .map(HinhAnhSanPham::getUrlAnh)
                        .toList()
        );

        BigDecimal giaGoc = ctsp.getGiaBan();
        BigDecimal giaSauGiam = giaGoc;
        Integer phanTramGiam = 0;

        if (ctsp.getId() != null) {
            List<ChiTietDotGiamGia> discounts =
                    chiTietDotGiamGiaRepository.findActiveDiscountBySpctId(ctsp.getId());

            if (!discounts.isEmpty()) {
                ChiTietDotGiamGia activeDiscount = discounts.get(0);
                DotGiamGia dgg = activeDiscount.getIdDotGiamGia();

                if (dgg != null && dgg.getGiaTriGiam() != null) {
                    phanTramGiam = dgg.getGiaTriGiam().intValue();

                    BigDecimal tienGiam = giaGoc.multiply(BigDecimal.valueOf(phanTramGiam))
                            .divide(BigDecimal.valueOf(100));
                    giaSauGiam = giaGoc.subtract(tienGiam);
                }
            }
        }

        res.setGiaGoc(giaGoc);
        res.setGiaSauGiam(giaSauGiam);
        res.setPhanTramGiam(phanTramGiam);

        return res;
    }
}
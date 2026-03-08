package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.*;
import org.example.chocostyle_datn.model.Request.ChiTietSanPhamRequest;
import org.example.chocostyle_datn.model.Response.ChiTietSanPhamResponse;
import org.example.chocostyle_datn.model.Response.SanPhamResponse;
import org.example.chocostyle_datn.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    // Đã thêm ChiTietDotGiamGiaRepository
    private final ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository;

    /* ================= GET ================= */

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

    // 👉 THÊM HÀM MỚI: LẤY CÁC SẢN PHẨM ĐANG CÓ ĐỢT GIẢM GIÁ (SALE)
    public Page<ChiTietSanPhamResponse> getSaleProducts(Pageable pageable) {
        return chiTietDotGiamGiaRepository.findActiveSaleProducts(pageable)
                .map(this::mapToResponse);
    }

    /* ================= CREATE ================= */

    @Transactional
    public ChiTietSanPhamResponse create(ChiTietSanPhamRequest data) {

        ChiTietSanPham ctsp = new ChiTietSanPham();

        ctsp.setIdSanPham(
                sanPhamRepository.findById(data.getIdSanPham())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"))
        );

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

        return mapToResponse(ctsp);
    }

    /* ================= DELETE ================= */

    public void delete(Integer id) {
        repository.deleteById(id);
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

        if (sp.getSoLuongTon() == 0) {
            throw new IllegalArgumentException("Sản phẩm đã hết hàng, không thể thay đổi trạng thái");
        }

        if (trangThai != 1 && trangThai != 2) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        }

        sp.setTrangThai(trangThai);
        sp.setNgayCapNhat(LocalDate.now());
        sp.setNguoiCapNhat(nguoiCapNhat);

        chiTietSanPhamRepository.save(sp);
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


    private void autoUpdateTrangThai(ChiTietSanPham ctsp) {
        if (ctsp.getSoLuongTon() == null || ctsp.getSoLuongTon() <= 0) {
            ctsp.setTrangThai(0);
        } else {
            ctsp.setTrangThai(1);
        }
    }


    /* ================= MAP RESPONSE ================= */

    private ChiTietSanPhamResponse mapToResponse(ChiTietSanPham ctsp) {

        ChiTietSanPhamResponse res = new ChiTietSanPhamResponse();

        res.setId(ctsp.getId());
        res.setMaChiTietSanPham(ctsp.getMaChiTietSanPham());
        res.setSoLuongTon(ctsp.getSoLuongTon());
        res.setGiaNhap(ctsp.getGiaNhap());
        res.setGiaBan(ctsp.getGiaBan());
        res.setTrangThai(ctsp.getTrangThai());

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

        // 👉 TÍNH TOÁN GIẢM GIÁ TỰ ĐỘNG NẾU CÓ KHUYẾN MÃI
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

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

    /* ================= CREATE ================= */

    @Transactional
    public ChiTietSanPhamResponse create(ChiTietSanPhamRequest data) {

        ChiTietSanPham ctsp = new ChiTietSanPham();

        ctsp.setIdSanPham(
                sanPhamRepository.findById(data.getIdSanPham())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"))
        );
//        ctsp.setIdKichCo(
//                kichCoRepository.findById(data.getIdKichCo())
//                        .orElseThrow(() -> new RuntimeException("Kích cỡ không tồn tại"))
//        );
//        ctsp.setIdMauSac(
//                mauSacRepository.findById(data.getIdMauSac())
//                        .orElseThrow(() -> new RuntimeException("Màu sắc không tồn tại"))
//        );
//        ctsp.setIdLoaiAo(
//                loaiAoRepository.findById(data.getIdLoaiAo())
//                        .orElseThrow(() -> new RuntimeException("Loại áo không tồn tại"))
//        );
//        ctsp.setIdPhongCachMac(
//                phongCachMacRepository.findById(data.getIdPhongCachMac())
//                        .orElseThrow(() -> new RuntimeException("Phong cách mặc không tồn tại"))
//        );
//        ctsp.setIdKieuDang(
//                kieuDangRepository.findById(data.getIdKieuDang())
//                        .orElseThrow(() -> new RuntimeException("Kiểu dáng không tồn tại"))
//        );

        ctsp.setSoLuongTon(data.getSoLuongTon());
        ctsp.setGiaNhap(data.getGiaNhap());
        ctsp.setGiaBan(data.getGiaBan());
        ctsp.setTrangThai(1);

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
//        ctsp.setIdKichCo(
//                kichCoRepository.findById(data.getIdKichCo())
//                        .orElseThrow(() -> new RuntimeException("Kích cỡ không tồn tại"))
//        );
//        ctsp.setIdMauSac(
//                mauSacRepository.findById(data.getIdMauSac())
//                        .orElseThrow(() -> new RuntimeException("Màu sắc không tồn tại"))
//        );
//        ctsp.setIdLoaiAo(
//                loaiAoRepository.findById(data.getIdLoaiAo())
//                        .orElseThrow(() -> new RuntimeException("Loại áo không tồn tại"))
//        );
//        ctsp.setIdPhongCachMac(
//                phongCachMacRepository.findById(data.getIdPhongCachMac())
//                        .orElseThrow(() -> new RuntimeException("Phong cách mặc không tồn tại"))
//        );
//        ctsp.setIdKieuDang(
//                kieuDangRepository.findById(data.getIdKieuDang())
//                        .orElseThrow(() -> new RuntimeException("Kiểu dáng không tồn tại"))
//        );

        ctsp.setSoLuongTon(data.getSoLuongTon());
        ctsp.setGiaNhap(data.getGiaNhap());
        ctsp.setGiaBan(data.getGiaBan());
        ctsp.setTrangThai(data.getTrangThai());

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

        if (trangThai != 1 && trangThai != 2) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        }

        ChiTietSanPham sp = chiTietSanPhamRepository.findById(sanPhamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        sp.setTrangThai(trangThai);
        sp.setNgayCapNhat(LocalDate.now());
        sp.setNguoiCapNhat(nguoiCapNhat);
        chiTietSanPhamRepository.save(sp);
    }

    public List<ChiTietSanPham> getDataExport(List<Integer> ids, Integer productId) {

        // 1️⃣ Ưu tiên checkbox
        if (ids != null && !ids.isEmpty()) {
            return repository.findAllById(ids);
        }

        // 2️⃣ Không tick checkbox nhưng đang xem theo sản phẩm
        if (productId != null) {
            return repository.findByIdSanPham_Id(productId);
        }

        // 3️⃣ Cuối cùng mới export ALL
        return repository.findAll();
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

        return res;
    }
}

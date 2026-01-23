package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.entity.HinhAnhSanPham;
import org.example.chocostyle_datn.entity.SanPham;
import org.example.chocostyle_datn.model.Request.BienTheRequest;
import org.example.chocostyle_datn.model.Request.KichCoRequest;
import org.example.chocostyle_datn.model.Request.SanPhamRequest;
import org.example.chocostyle_datn.model.Response.BienTheResponse;
import org.example.chocostyle_datn.model.Response.MauSacResponse;
import org.example.chocostyle_datn.model.Response.SanPhamResponse;
import org.example.chocostyle_datn.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SanPhamService {

    private final SanPhamRepository sanPhamRepo;
    private final ChatLieuRepository chatLieuRepo;
    private final XuatXuRepository xuatXuRepo;
    private final HinhAnhSanPhamRepository hinhAnhRepo;
    private final ChiTietSanPhamRepository chiTietRepo;
    private final KichCoRepository kichCoRepo;
    private final MauSacRepository mauSacRepo;
    private final LoaiAoRepository loaiAoRepo;
    private final PhongCachMacRepository phongCachMacRepo;
    private final KieuDangRepository kieuDangRepo;

    public List<SanPhamResponse> getAll() {
        return sanPhamRepo.findAll().stream().map(this::toResponse).toList();
    }

    public SanPhamResponse getById(Integer id) {
        return toResponse(
                sanPhamRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"))
        );
    }

    public SanPhamResponse create(SanPhamRequest request) {

        SanPham sp = new SanPham();

        sp.setMaSp(
                genMa("SP", sanPhamRepo.findMaxMa())
        );
        sp.setTenSp(request.getTenSp());
        sp.setMoTa(request.getMoTa());
        sp.setTrangThai(1);
        sp.setNgayTao(LocalDate.now());
        sp.setNguoiTao(request.getNguoiTao());
        sp.setHinhAnh(request.getHinhAnh());

        sp.setIdChatLieu(chatLieuRepo.findById(request.getIdChatLieu()).orElseThrow());
        sp.setIdXuatXu(xuatXuRepo.findById(request.getIdXuatXu()).orElseThrow());

        sanPhamRepo.save(sp);

        String maxMa = chiTietRepo.findMaxMa();
        saveBienThe(sp, request, maxMa);
        return toResponse(sp);
    }


    public SanPhamResponse update(Integer id, SanPhamRequest request) {

        SanPham sp = sanPhamRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        sp.setIdChatLieu(
                chatLieuRepo.findById(request.getIdChatLieu())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy chất liệu"))
        );
        sp.setIdXuatXu(
                xuatXuRepo.findById(request.getIdXuatXu())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy xuất xứ"))
        );


        sp.setTenSp(request.getTenSp());
        sp.setMoTa(request.getMoTa());
        sp.setNgayCapNhat(LocalDate.now());
        sp.setNguoiCapNhat(request.getNguoiCapNhat());
        sp.setHinhAnh(request.getHinhAnh());


        List<ChiTietSanPham> oldCt = chiTietRepo.findByIdSanPham(sp);

        for (ChiTietSanPham ct : oldCt) {
            hinhAnhRepo.deleteByChiTietSanPham_Id(ct.getId());
        }

        chiTietRepo.deleteByIdSanPham(sp);

        String maxMa = chiTietRepo.findMaxMa();

        chiTietRepo.deleteByIdSanPham(sp);

        sanPhamRepo.save(sp);
        saveBienThe(sp, request, maxMa);
        return toResponse(sp);

    }

    public void delete(Integer id) {
        sanPhamRepo.deleteById(id);
    }

    private void saveBienThe(SanPham sp, SanPhamRequest request, String maxMa) {

        int index = 1;
        if (maxMa != null) {
            index = Integer.parseInt(maxMa.replace("CTSP", "")) + 1;
        }

        for (BienTheRequest mauReq : request.getBienTheList()) {
            for (KichCoRequest sizeReq : mauReq.getSizeList()) {

                ChiTietSanPham ct = new ChiTietSanPham();
                ct.setIdSanPham(sp);
                ct.setIdMauSac(mauSacRepo.findById(mauReq.getMauSacId()).orElseThrow());
                ct.setIdKichCo(kichCoRepo.findById(sizeReq.getIdKichCo()).orElseThrow());
                ct.setIdLoaiAo(loaiAoRepo.findById(request.getIdLoaiAo()).orElseThrow());
                ct.setIdPhongCachMac(phongCachMacRepo.findById(request.getIdPhongCachMac()).orElseThrow());
                ct.setIdKieuDang(kieuDangRepo.findById(request.getIdKieuDang()).orElseThrow());

                // ✅ CTSP TĂNG LIÊN TỤC TOÀN HỆ THỐNG
                ct.setMaChiTietSanPham("CTSP" + String.format("%03d", index++));

                ct.setSoLuongTon(sizeReq.getSoLuongTon());
                ct.setGiaNhap(sizeReq.getGiaNhap());
                ct.setGiaBan(sizeReq.getGiaBan());
                ct.setTrangThai(sizeReq.getSoLuongTon() > 0 ? 1 : 0);

                ct.setNgayTao(LocalDate.now());
                ct.setNguoiTao(
                        request.getNguoiTao() != null
                                ? request.getNguoiTao()
                                : request.getNguoiCapNhat()
                );

                ChiTietSanPham savedCt = chiTietRepo.save(ct);
                saveImages(savedCt, mauReq.getHinhAnhUrls());
            }
        }
    }


    private void saveImages(ChiTietSanPham ctsp, List<String> urls) {
        if (urls == null) return;
        for (String url : urls) {
            HinhAnhSanPham img = new HinhAnhSanPham();
            img.setChiTietSanPham(ctsp);
            img.setUrlAnh(url);
            hinhAnhRepo.save(img);
        }
    }

    private SanPhamResponse toResponse(SanPham sp) {

        SanPhamResponse dto = new SanPhamResponse();
        dto.setId(sp.getId());
        dto.setMaSp(sp.getMaSp());
        dto.setTenSp(sp.getTenSp());
        dto.setMoTa(sp.getMoTa());
        dto.setTrangThai(sp.getTrangThai());
        dto.setNgayTao(sp.getNgayTao());
        dto.setNguoiTao(sp.getNguoiTao());
        dto.setNgayCapNhat(sp.getNgayCapNhat());
        dto.setNguoiCapNhat(sp.getNguoiCapNhat());
        dto.setHinhAnh(sp.getHinhAnh());

        dto.setTenChatLieu(sp.getIdChatLieu().getTenChatLieu());
        dto.setTenXuatXu(sp.getIdXuatXu().getTenXuatXu());

        List<ChiTietSanPham> ctList = chiTietRepo.findByIdSanPham(sp);

        // 🔥 GROUP BY MA CTSP
        Map<String, List<ChiTietSanPham>> grouped =
                ctList.stream()
                        .collect(Collectors.groupingBy(ChiTietSanPham::getMaChiTietSanPham));

        List<BienTheResponse> bienTheResponses = grouped.values().stream()
                .map(list -> {
                    ChiTietSanPham first = list.get(0);

                    BienTheResponse res = new BienTheResponse();
                    res.setId(String.valueOf(first.getId()));
                    res.setMaChiTietSanPham(first.getMaChiTietSanPham());
                    res.setGiaBan(first.getGiaBan());
                    res.setGiaNhap(first.getGiaNhap());
                    res.setTrangThai(first.getTrangThai());

                    // ✅ TỔNG SỐ LƯỢNG
                    res.setSoLuongTon(
                            list.stream()
                                    .mapToInt(ChiTietSanPham::getSoLuongTon)
                                    .sum()
                    );

                    // ✅ MÀU
                    res.setMauSacList(
                            list.stream()
                                    .map(ct -> new MauSacResponse(
                                            ct.getIdMauSac().getTenMauSac(),
                                            ct.getIdMauSac().getRgb()   // 🔥 lấy rgb
                                    ))
                                    .distinct()
                                    .toList()
                    );


                    // ✅ SIZE
                    res.setKichCoList(
                            list.stream()
                                    .map(ct -> ct.getIdKichCo().getTenKichCo())
                                    .distinct()
                                    .toList()
                    );

                    // ✅ ẢNH
                    List<String> images = hinhAnhRepo
                            .findByChiTietSanPham_Id(first.getId())
                            .stream()
                            .map(HinhAnhSanPham::getUrlAnh)
                            .toList();

                    res.setHinhAnhUrls(images);

                    return res;
                })
                .toList();

        dto.setBienTheList(bienTheResponses);

        // 🔹 THÔNG TIN CHUNG
        if (!ctList.isEmpty()) {
            ChiTietSanPham ct = ctList.get(0);
            dto.setTenLoaiAo(ct.getIdLoaiAo().getTenLoai());
            dto.setTenKieuDang(ct.getIdKieuDang().getTenKieuDang());
            dto.setTenPhongCachMac(ct.getIdPhongCachMac().getTenPhongCach());
        }

        return dto;
    }


    private String genMa(String prefix, String maxMa) {
        if (maxMa == null) {
            return prefix + "01";
        }

        String numberPart = maxMa.replaceAll("\\D+", "");
        int nextNumber = Integer.parseInt(numberPart) + 1;

        return prefix + String.format("%02d", nextNumber);
    }

    public Page<SanPhamResponse> getSanPham(
            String keyword,
            Integer status,
            Integer idChatLieu,
            Integer idXuatXu,
            Pageable pageable
    ) {
        Page<SanPham> page = sanPhamRepo.searchSanPham(
                keyword, status, idChatLieu, idXuatXu, pageable
        );
        return page.map(this::toResponse);
    }

    public void changeStatusSanPham(
            Integer sanPhamId,
            Integer trangThai,
            String nguoiCapNhat
    ) {

        if (trangThai != 1 && trangThai != 2) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ");
        }

        SanPham sp = sanPhamRepo.findById(sanPhamId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // 1️⃣ Cập nhật trạng thái sản phẩm
        sp.setTrangThai(trangThai);
        sp.setNgayCapNhat(LocalDate.now());
        sp.setNguoiCapNhat(nguoiCapNhat);
        sanPhamRepo.save(sp);

        // 2️⃣ Cập nhật biến thể
        List<ChiTietSanPham> ctList = chiTietRepo.findByIdSanPham(sp);

        for (ChiTietSanPham ct : ctList) {

            if (trangThai == 2) {
                // 🔴 Ngừng bán → tất cả biến thể = 2
                ct.setTrangThai(2);
            } else {
                // 🟢 Mở bán → dựa vào tồn kho
                if (ct.getSoLuongTon() > 0) {
                    ct.setTrangThai(1);
                } else {
                    ct.setTrangThai(0);
                }
            }

            ct.setNgayCapNhat(LocalDate.now());
            ct.setNguoiCapNhat(nguoiCapNhat);
        }

        chiTietRepo.saveAll(ctList);
    }


    public void updateSoLuongBienThe(Integer chiTietId, int soLuongMoi, String nguoiCapNhat) {

        ChiTietSanPham ct = chiTietRepo.findById(chiTietId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể"));

        ct.setSoLuongTon(soLuongMoi);

        // ⚠️ CHỈ đổi trạng thái nếu KHÔNG phải ngừng bán
        if (ct.getTrangThai() != 2) {
            if (soLuongMoi > 0) {
                ct.setTrangThai(1); // đang bán
            } else {
                ct.setTrangThai(0); // hết hàng
            }
        }

        ct.setNgayCapNhat(LocalDate.now());
        ct.setNguoiCapNhat(nguoiCapNhat);

        chiTietRepo.save(ct);
    }


}



package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.entity.HinhAnhSanPham;
import org.example.chocostyle_datn.entity.SanPham;
import org.example.chocostyle_datn.model.request.BienTheRequest;
import org.example.chocostyle_datn.model.request.KichCoRequest;
import org.example.chocostyle_datn.model.request.SanPhamRequest;
import org.example.chocostyle_datn.model.response.BienTheResponse;
import org.example.chocostyle_datn.model.response.MauSacResponse;
import org.example.chocostyle_datn.model.response.SanPhamResponse;
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

        saveBienThe(sp, request);
        return toResponse(sp);
    }


    public SanPhamResponse update(Integer id, SanPhamRequest request) {

        SanPham sp = sanPhamRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        sp.setTenSp(request.getTenSp());
        sp.setMoTa(request.getMoTa());
        sp.setNgayCapNhat(LocalDate.now());
        sp.setNguoiCapNhat(request.getNguoiTao());
        sp.setHinhAnh(request.getHinhAnh());

        List<ChiTietSanPham> oldCt = chiTietRepo.findByIdSanPham(sp);

        for (ChiTietSanPham ct : oldCt) {
            hinhAnhRepo.deleteByChiTietSanPham_Id(ct.getId());
        }

        chiTietRepo.deleteByIdSanPham(sp);
        chiTietRepo.deleteByIdSanPham(sp);

        sanPhamRepo.save(sp);
        saveBienThe(sp, request);
        return toResponse(sp);

    }

    public void delete(Integer id) {
        sanPhamRepo.deleteById(id);
    }

    private void saveBienThe(SanPham sp, SanPhamRequest request) {

        String maxMa = chiTietRepo.findMaxMa();
        int index = (maxMa == null)
                ? 1
                : Integer.parseInt(maxMa.replaceAll("\\D+", "")) + 1;

        for (BienTheRequest mauReq : request.getBienTheList()) {
            for (KichCoRequest sizeReq : mauReq.getSizeList()) {

                ChiTietSanPham ct = new ChiTietSanPham();
                ct.setIdSanPham(sp);
                ct.setIdMauSac(mauSacRepo.findById(mauReq.getMauSacId()).orElseThrow());
                ct.setIdKichCo(kichCoRepo.findById(sizeReq.getIdKichCo()).orElseThrow());
                ct.setIdLoaiAo(loaiAoRepo.findById(request.getIdLoaiAo()).orElseThrow());
                ct.setIdPhongCachMac(phongCachMacRepo.findById(request.getIdPhongCachMac()).orElseThrow());
                ct.setIdKieuDang(kieuDangRepo.findById(request.getIdKieuDang()).orElseThrow());

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

                // ✅ PHẢI SAVE TRƯỚC
                ChiTietSanPham savedCt = chiTietRepo.save(ct);

                // ✅ SAU ĐÓ MỚI LƯU ẢNH
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

}



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
import org.example.chocostyle_datn.model.Response.SanPhamResponse;
import org.example.chocostyle_datn.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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

        sp.setIdChatLieu(chatLieuRepo.findById(request.getIdChatLieu()).orElseThrow());
        sp.setIdXuatXu(xuatXuRepo.findById(request.getIdXuatXu()).orElseThrow());

        sanPhamRepo.save(sp);

        saveBienThe(sp, request);
        saveImages(sp, request.getHinhAnhUrls());

        return toResponse(sp);
    }


    public SanPhamResponse update(Integer id, SanPhamRequest request) {

        SanPham sp = sanPhamRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        sp.setTenSp(request.getTenSp());
        sp.setMoTa(request.getMoTa());
        sp.setNgayCapNhat(LocalDate.now());
        sp.setNguoiCapNhat(request.getNguoiTao());

        hinhAnhRepo.deleteByIdSanPham(sp);
        chiTietRepo.deleteByIdSanPham(sp);

        saveBienThe(sp, request);
        saveImages(sp, request.getHinhAnhUrls());

        return toResponse(sp);
    }

    public void delete(Integer id) {
        sanPhamRepo.deleteById(id);
    }

    private void saveBienThe(SanPham sp, SanPhamRequest request) {

        for (BienTheRequest mauReq : request.getBienTheList()) {

            for (KichCoRequest sizeReq : mauReq.getSizeList()) {

                ChiTietSanPham ct = new ChiTietSanPham();
                ct.setIdSanPham(sp);
                ct.setIdMauSac(mauSacRepo.findById(mauReq.getMauSacId()).orElseThrow());
                ct.setIdKichCo(kichCoRepo.findById(sizeReq.getIdKichCo()).orElseThrow());
                ct.setIdLoaiAo(loaiAoRepo.findById(request.getIdLoaiAo()).orElseThrow());
                ct.setIdPhongCachMac(phongCachMacRepo.findById(request.getIdPhongCachMac()).orElseThrow());
                ct.setIdKieuDang(kieuDangRepo.findById(request.getIdKieuDang()).orElseThrow());

                ct.setMaChiTietSanPham(
                        genMa("CTSP", chiTietRepo.findMaxMa())
                );
                ct.setSoLuongTon(sizeReq.getSoLuongTon());
                ct.setGiaNhap(sizeReq.getGiaNhap());
                ct.setGiaBan(sizeReq.getGiaBan());

                ct.setTrangThai(
                        sizeReq.getSoLuongTon() > 0 ? 1 : 0
                );

                ct.setNgayTao(LocalDate.now());
                ct.setNguoiTao(request.getNguoiTao());

                chiTietRepo.save(ct);
            }
        }
    }


    private void saveImages(SanPham sp, List<String> urls) {
        if (urls == null) return;
        for (String url : urls) {
            HinhAnhSanPham img = new HinhAnhSanPham();
            img.setIdSanPham(sp);
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

        dto.setTenChatLieu(sp.getIdChatLieu().getTenChatLieu());
        dto.setTenXuatXu(sp.getIdXuatXu().getTenXuatXu());

        dto.setHinhAnhUrls(
                hinhAnhRepo.findByIdSanPham(sp)
                        .stream()
                        .map(HinhAnhSanPham::getUrlAnh)
                        .toList()
        );

        List<ChiTietSanPham> ctList = chiTietRepo.findByIdSanPham(sp);

        dto.setBienTheList(
                ctList.stream()
                        .map(ct -> new BienTheResponse(
                                ct.getMaChiTietSanPham(),
                                ct.getIdKichCo().getTenKichCo(),
                                ct.getIdMauSac().getTenMauSac(),
                                ct.getSoLuongTon(),
                                ct.getGiaBan(),
                                ct.getGiaNhap(),
                                ct.getTrangThai()
                        ))
                        .toList()
        );

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

}



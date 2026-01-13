package org.example.chocostyle_datn.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.HinhAnhSanPham;
import org.example.chocostyle_datn.entity.SanPham;
import org.example.chocostyle_datn.model.Request.SanPhamRequest;
import org.example.chocostyle_datn.model.Response.SanPhamResponse;
import org.example.chocostyle_datn.repository.ChatLieuRepository;
import org.example.chocostyle_datn.repository.HinhAnhSanPhamRepository;
import org.example.chocostyle_datn.repository.SanPhamRepository;
import org.example.chocostyle_datn.repository.XuatXuRepository;
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

    public List<SanPhamResponse> getAll() {
        return sanPhamRepo.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public SanPhamResponse getById(Integer id) {
        return toResponse(
                sanPhamRepo.findById(id)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"))
        );
    }

    public SanPhamResponse create(SanPhamRequest request) {

        SanPham sp = new SanPham();
        sp.setMaSp(request.getMaSp());
        sp.setTenSp(request.getTenSp());
        sp.setMoTa(request.getMoTa());
        sp.setTrangThai(request.getTrangThai());
        sp.setNgayTao(LocalDate.now());
        sp.setNguoiTao(request.getNguoiTao());

        sp.setIdChatLieu(chatLieuRepo.findById(request.getIdChatLieu()).orElseThrow());
        sp.setIdXuatXu(xuatXuRepo.findById(request.getIdXuatXu()).orElseThrow());

        sanPhamRepo.save(sp);

        saveImages(sp, request.getHinhAnhUrls());

        return toResponse(sp);
    }

    public SanPhamResponse update(Integer id, SanPhamRequest request) {

        SanPham sp = sanPhamRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        sp.setTenSp(request.getTenSp());
        sp.setMoTa(request.getMoTa());
        sp.setTrangThai(request.getTrangThai());
        sp.setNgayCapNhat(LocalDate.now());
        sp.setNguoiCapNhat(request.getNguoiTao());

        hinhAnhRepo.deleteByIdSanPham(sp);
        saveImages(sp, request.getHinhAnhUrls());

        return toResponse(sp);
    }

    public void delete(Integer id) {
        sanPhamRepo.deleteById(id);
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

        return dto;
    }
}


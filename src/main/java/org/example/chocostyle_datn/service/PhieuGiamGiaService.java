package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.PhieuGiamGia;
import org.example.chocostyle_datn.model.request.PhieuGiamGiaRequest;
import org.example.chocostyle_datn.model.response.PhieuGiamGiaResponse;
import org.example.chocostyle_datn.repository.PhieuGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PhieuGiamGiaService {
    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;

    public List<PhieuGiamGiaResponse> getAllPGG() {
        return phieuGiamGiaRepository.findAllOrderByIdDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public String generateMaPgg() {
        List<String> list = phieuGiamGiaRepository.findLastMaPgg();

        if (list.isEmpty()) {
            return "PGG001";
        }

        String lastMa = list.get(0);
        int number = Integer.parseInt(lastMa.substring(3)) + 1;

        return String.format("PGG%03d", number);
    }

    private void validateRequest(PhieuGiamGiaRequest req) {
        if (req.getTenPgg() == null || req.getTenPgg().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phiếu giảm giá không được để trống");
        }
        req.setTenPgg(req.getTenPgg().trim());

        if (!req.getTenPgg().matches("^[A-Za-zÀ-ỹ0-9\\s]+$")) {
            throw new IllegalArgumentException("Tên phiếu giảm giá không được chứa ký tự đặc biệt");
        }

        if (!List.of("PERCENT", "MONEY").contains(req.getLoaiGiam())) {
            throw new IllegalArgumentException("Loại giảm giá không hợp lệ");
        }

        if (req.getGiaTri().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá trị giảm phải lớn hơn 0");
        }

        if (req.getLoaiGiam().equals("PERCENT")
                && req.getGiaTri().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Giảm theo % không được vượt quá 100%");
        }

        if (req.getNgayBatDau().isAfter(req.getNgayKetThuc())) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc");
        }

        if (req.getSoLuong() <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
    }

    private Integer tinhTrangThai(LocalDate batDau, LocalDate ketThuc) {
        LocalDate today = LocalDate.now();

        if (today.isBefore(batDau)) {
            return 2;
        }

        if (!today.isAfter(ketThuc)) {
            return 1;
        }

        return 0;
    }

    public PhieuGiamGiaResponse createPGG(PhieuGiamGiaRequest req) {
        PhieuGiamGia pgg = new PhieuGiamGia();

        pgg.setMaPgg(generateMaPgg());
        pgg.setTenPgg(req.getTenPgg());
        pgg.setKieuApDung(req.getKieuApDung());
        pgg.setLoaiGiam(req.getLoaiGiam());
        pgg.setGiaTri(req.getGiaTri());
        pgg.setGiaTriToiDa(req.getGiaTriToiDa());
        pgg.setDieuKienDonHang(req.getDieuKienDonHang());
        pgg.setNgayBatDau(req.getNgayBatDau());
        pgg.setNgayKetThuc(req.getNgayKetThuc());
        pgg.setSoLuong(req.getSoLuong());
        pgg.setSoLuongDaDung(0);
        pgg.setTrangThai(tinhTrangThai(
                req.getNgayBatDau(),
                req.getNgayKetThuc()
        ));

        return toResponse(phieuGiamGiaRepository.save(pgg));
    }

    public PhieuGiamGiaResponse updatePGG(Integer id, PhieuGiamGiaRequest req) {
        PhieuGiamGia pgg = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu giảm giá"));

        pgg.setTenPgg(req.getTenPgg());
        pgg.setKieuApDung(req.getKieuApDung());
        pgg.setLoaiGiam(req.getLoaiGiam());
        pgg.setGiaTri(req.getGiaTri());
        pgg.setGiaTriToiDa(req.getGiaTriToiDa());
        pgg.setDieuKienDonHang(req.getDieuKienDonHang());
        pgg.setNgayBatDau(req.getNgayBatDau());
        pgg.setNgayKetThuc(req.getNgayKetThuc());
        pgg.setSoLuong(req.getSoLuong());
        pgg.setTrangThai(tinhTrangThai(
                req.getNgayBatDau(),
                req.getNgayKetThuc()
        ));

        return toResponse(phieuGiamGiaRepository.save(pgg));
    }

    public Boolean deletePGG(Integer id) {
        PhieuGiamGia pgg = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu giảm giá"));

        pgg.setTrangThai(0);
        phieuGiamGiaRepository.save(pgg);
        return true;
    }

    public List<PhieuGiamGiaResponse> filterPGG(
            String loaiGiam,
            Integer trangThai,
            String fromDate,
            String toDate
    ) {
        return phieuGiamGiaRepository.findByTrangThaiNot(0)
                .stream()
                .filter(pgg -> loaiGiam == null || loaiGiam.isEmpty()
                        || pgg.getLoaiGiam().equals(loaiGiam))
                .filter(pgg -> trangThai == null
                        || pgg.getTrangThai().equals(trangThai))
                .filter(pgg -> fromDate == null || fromDate.isEmpty()
                        || !pgg.getNgayBatDau().isBefore(LocalDate.parse(fromDate)))
                .filter(pgg -> toDate == null || toDate.isEmpty()
                        || !pgg.getNgayKetThuc().isAfter(LocalDate.parse(toDate)))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private PhieuGiamGiaResponse toResponse(PhieuGiamGia pgg) {
        return new PhieuGiamGiaResponse(
                pgg.getId(),
                pgg.getMaPgg(),
                pgg.getTenPgg(),
                pgg.getKieuApDung(),
                pgg.getLoaiGiam(),
                pgg.getGiaTri(),
                pgg.getGiaTriToiDa(),
                pgg.getDieuKienDonHang(),
                pgg.getNgayBatDau(),
                pgg.getNgayKetThuc(),
                pgg.getSoLuong(),
                pgg.getSoLuongDaDung(),
                pgg.getTrangThai()
        );
    }
    public PhieuGiamGiaResponse getPGGById(Integer id) {
        PhieuGiamGia pgg = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu giảm giá"));

        return toResponse(pgg);
    }

}

package org.example.chocostyle_datn.service;


import org.example.chocostyle_datn.entity.KhachHang;
import org.example.chocostyle_datn.entity.PhieuGiamGia;
import org.example.chocostyle_datn.entity.PhieuGiamGiaKhachHang;
import org.example.chocostyle_datn.model.Request.PhieuGiamGiaRequest;
import org.example.chocostyle_datn.model.Response.PhieuGiamGiaResponse;
import org.example.chocostyle_datn.repository.KhachHangRepository;
import org.example.chocostyle_datn.repository.PhieuGiamGiaKhachHangRepository;
import org.example.chocostyle_datn.repository.PhieuGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;


@Service
@EnableScheduling
public class PhieuGiamGiaService {
    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;


    @Autowired
    private KhachHangRepository khachHangRepository;


    @Autowired
    private PhieuGiamGiaKhachHangRepository pggKhRepository;


    @Autowired
    private EmailService emailService;




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


//    @Scheduled(cron = "0 0 0 * * ?")
//    public void autoUpdateTrangThai() {
//        LocalDate today = LocalDate.now();
//
//
//        List<PhieuGiamGia> list = phieuGiamGiaRepository.findAll();
//
//
//        for (PhieuGiamGia pgg : list) {
//
//
//            if (today.isAfter(pgg.getNgayKetThuc())) {
//                if (pgg.getTrangThai() != 0) {
//                    pgg.setTrangThai(0);
//                    phieuGiamGiaRepository.save(pgg);
//                }
//            }
//        }
//    }

    public PhieuGiamGiaResponse toggleTrangThai(Integer id) {
        PhieuGiamGia pgg = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu giảm giá"));


        LocalDate today = LocalDate.now();


        // ❌ Không cho bật phiếu đã hết hạn
        if (today.isAfter(pgg.getNgayKetThuc())) {
            throw new IllegalArgumentException("Phiếu giảm giá đã hết hạn, không thể kích hoạt");
        }


        // ✅ Chỉ toggle cho phép / không cho phép
        pgg.setTrangThai(pgg.getTrangThai() == 1 ? 0 : 1);


        return toResponse(phieuGiamGiaRepository.save(pgg));
    }


    public Boolean checkTenTrung(String tenPgg) {
        return phieuGiamGiaRepository.existsByTenPggIgnoreCase(tenPgg.trim());
    }


    public PhieuGiamGiaResponse createPGG(PhieuGiamGiaRequest req) {
        validateRequest(req);


        if (phieuGiamGiaRepository.existsByTenPggIgnoreCase(req.getTenPgg())) {
            throw new IllegalArgumentException("Tên phiếu giảm giá đã tồn tại");
        }


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


        // ✅ CHỈ CÓ 1 Ý NGHĨA: cho phép hoạt động
        pgg.setTrangThai(1);


        pgg = phieuGiamGiaRepository.save(pgg);


        if ("PERSONAL".equals(req.getKieuApDung())) {
            for (Integer khId : req.getKhachHangIds()) {
                KhachHang kh = khachHangRepository.findById(khId).orElseThrow();


                PhieuGiamGiaKhachHang pggKh = new PhieuGiamGiaKhachHang();
                pggKh.setKhachHang(kh);
                pggKh.setPhieuGiamGia(pgg);
                pggKh.setMaPhieuGiamGiaKh(pgg.getMaPgg() + "-" + kh.getMaKh());
                pggKh.setNgayNhan(java.time.LocalDateTime.now());
                pggKh.setDaSuDung(false);


                pggKhRepository.save(pggKh);
                emailService.sendVoucherCreatedEmail(kh, pgg);
            }
        }

        if ("ALL".equals(req.getKieuApDung())) {
            List<KhachHang> customers = khachHangRepository.findByTrangThai(1);
            for (KhachHang cus : customers) {
                emailService.sendVoucherCreatedEmail(cus, pgg);
            }
        }

        return toResponse(pgg);
    }


    public Boolean checkTenTrungKhiUpdate(Integer id, String tenPgg) {
        return phieuGiamGiaRepository
                .existsByTenPggIgnoreCaseAndIdNot(tenPgg.trim(), id);
    }

    public PhieuGiamGiaResponse updatePGG(Integer id, PhieuGiamGiaRequest req) {
        validateRequest(req);


        if (phieuGiamGiaRepository
                .existsByTenPggIgnoreCaseAndIdNot(req.getTenPgg(), id)) {
            throw new IllegalArgumentException("Tên phiếu giảm giá đã tồn tại");
        }


        PhieuGiamGia pgg = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu giảm giá"));


        if (LocalDate.now().isAfter(pgg.getNgayKetThuc())) {
            throw new IllegalArgumentException("Phiếu giảm giá đã hết hạn, không thể cập nhật");
        }


        if (req.getSoLuong() < pgg.getSoLuongDaDung()) {
            throw new IllegalArgumentException("Số lượng không được nhỏ hơn số đã sử dụng");
        }


        BigDecimal oldGiaTri = pgg.getGiaTri();
        BigDecimal oldGiaTriToiDa = pgg.getGiaTriToiDa();
        BigDecimal oldDieuKien = pgg.getDieuKienDonHang();
        LocalDate oldNgayBatDau = pgg.getNgayBatDau();
        LocalDate oldNgayKetThuc = pgg.getNgayKetThuc();
        String oldLoaiGiam = pgg.getLoaiGiam();


        pgg.setTenPgg(req.getTenPgg());
        pgg.setKieuApDung(req.getKieuApDung());
        pgg.setLoaiGiam(req.getLoaiGiam());
        pgg.setGiaTri(req.getGiaTri());
        pgg.setGiaTriToiDa(req.getGiaTriToiDa());
        pgg.setDieuKienDonHang(req.getDieuKienDonHang());
        pgg.setNgayBatDau(req.getNgayBatDau());
        pgg.setNgayKetThuc(req.getNgayKetThuc());
        pgg.setSoLuong(req.getSoLuong());


        boolean contentChanged =
                oldGiaTri.compareTo(req.getGiaTri()) != 0
                        || !equalsNullable(oldGiaTriToiDa, req.getGiaTriToiDa())
                        || oldDieuKien.compareTo(req.getDieuKienDonHang()) != 0
                        || !oldNgayBatDau.equals(req.getNgayBatDau())
                        || !oldNgayKetThuc.equals(req.getNgayKetThuc())
                        || !oldLoaiGiam.equals(req.getLoaiGiam());


        if ("PERSONAL".equals(req.getKieuApDung())) {


            if (req.getKhachHangIds() == null || req.getKhachHangIds().isEmpty()) {
                throw new IllegalArgumentException("Voucher cá nhân phải chọn khách hàng");
            }


            if (req.getKhachHangIds().size() > req.getSoLuong()) {
                throw new IllegalArgumentException("Số khách hàng không được vượt quá số lượng voucher");
            }


            List<PhieuGiamGiaKhachHang> existedList =
                    pggKhRepository.findByPhieuGiamGiaId(id);


            List<Integer> existedKhIds = existedList.stream()
                    .map(x -> x.getKhachHang().getId())
                    .toList();


            List<Integer> daSuDungIds = existedList.stream()
                    .filter(PhieuGiamGiaKhachHang::getDaSuDung)
                    .map(x -> x.getKhachHang().getId())
                    .toList();


            if (!req.getKhachHangIds().containsAll(daSuDungIds)) {
                throw new IllegalArgumentException("Không thể bỏ khách hàng đã sử dụng voucher");
            }


            List<Integer> newKhIds = req.getKhachHangIds().stream()
                    .filter(khId -> !existedKhIds.contains(khId))
                    .toList();


            for (Integer khId : newKhIds) {
                KhachHang kh = khachHangRepository.findById(khId)
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy khách hàng"));


                PhieuGiamGiaKhachHang pggKh = new PhieuGiamGiaKhachHang();
                pggKh.setKhachHang(kh);
                pggKh.setPhieuGiamGia(pgg);
                pggKh.setMaPhieuGiamGiaKh(pgg.getMaPgg() + "-" + kh.getMaKh());
                pggKh.setNgayNhan(java.time.LocalDateTime.now());
                pggKh.setDaSuDung(false);


                pggKhRepository.save(pggKh);
                emailService.sendVoucherCreatedEmail(kh, pgg);
            }


            if (contentChanged) {
                for (PhieuGiamGiaKhachHang item : existedList) {
                    Integer khId = item.getKhachHang().getId();
                    if (!item.getDaSuDung() && !newKhIds.contains(khId)) {
                        emailService.sendVoucherUpdatedEmail(item.getKhachHang(), pgg);
                    }
                }
            }


        }

        if ("ALL".equals(req.getKieuApDung()) && contentChanged) {
            List<KhachHang> customers =
                    khachHangRepository.findByTrangThai(1);

            for (KhachHang kh : customers) {
                emailService.sendVoucherUpdatedEmail(kh, pgg);
            }
        }


        return toResponse(phieuGiamGiaRepository.save(pgg));
    }


    private boolean equalsNullable(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
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
            String kieuApDung,
            Integer trangThai,
            String fromDate,
            String toDate
    ) {
        LocalDate today = LocalDate.now();


        return phieuGiamGiaRepository.findAll()
                .stream()
                .filter(pgg -> {
                    if (trangThai == null) return true;


                    // ĐANG DIỄN RA
                    if (trangThai == 1) {
                        return pgg.getTrangThai() == 1
                                && !today.isBefore(pgg.getNgayBatDau())
                                && !today.isAfter(pgg.getNgayKetThuc());
                    }


                    // SẮP DIỄN RA
                    if (trangThai == 2) {
                        return pgg.getTrangThai() == 1
                                && today.isBefore(pgg.getNgayBatDau());
                    }


                    // ĐÃ KẾT THÚC / NGỪNG
                    if (trangThai == 0) {
                        return today.isAfter(pgg.getNgayKetThuc())
                                || pgg.getTrangThai() == 0;
                    }


                    return true;
                })
                .filter(pgg ->
                        fromDate == null || fromDate.isEmpty()
                                || !pgg.getNgayBatDau().isBefore(LocalDate.parse(fromDate))
                )
                .filter(pgg ->
                        toDate == null || toDate.isEmpty()
                                || !pgg.getNgayKetThuc().isAfter(LocalDate.parse(toDate))
                )
                .map(this::toResponse)
                .toList();
    }


    private PhieuGiamGiaResponse toResponse(PhieuGiamGia pgg) {

        LocalDate today = LocalDate.now();
        int trangThaiHienThi;

        if (pgg.getTrangThai() == 0) {
            trangThaiHienThi = 0; // Ngừng
        } else if (today.isBefore(pgg.getNgayBatDau())) {
            trangThaiHienThi = 2; // Sắp diễn ra
        } else if (today.isAfter(pgg.getNgayKetThuc())) {
            trangThaiHienThi = 3; // Đã kết thúc
        } else {
            trangThaiHienThi = 1; // Đang hoạt động
        }

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
                trangThaiHienThi
        );
    }


    public PhieuGiamGiaResponse getPGGById(Integer id) {
        PhieuGiamGia pgg = phieuGiamGiaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu giảm giá"));


        return toResponse(pgg);
    }

    public List<PhieuGiamGiaResponse> getVoucherForCustomer(Integer idKhachHang) {

        LocalDate today = LocalDate.now();

        return phieuGiamGiaRepository.findAll()
                .stream()
                .filter(pgg -> {

                    // Chỉ lấy voucher đang hoạt động
                    if (pgg.getTrangThai() != 1) return false;
                    if (today.isBefore(pgg.getNgayBatDau())) return false;
                    if (today.isAfter(pgg.getNgayKetThuc())) return false;

                    // Nếu là ALL -> luôn cho phép
                    if ("ALL".equals(pgg.getKieuApDung())) {
                        return true;
                    }

                    // Nếu là PERSONAL
                    if ("PERSONAL".equals(pgg.getKieuApDung())) {

                        // Chưa chọn khách -> không cho dùng
                        if (idKhachHang == null) return false;

                        // Phải tồn tại bản ghi và chưa sử dụng
                        return pggKhRepository
                                .existsByPhieuGiamGiaIdAndKhachHangIdAndDaSuDungFalse(
                                        pgg.getId(),
                                        idKhachHang
                                );
                    }

                    return false;
                })
                .map(this::toResponse)
                .toList();
    }

}




    package org.example.chocostyle_datn.service;

    import jakarta.transaction.Transactional;
    import org.example.chocostyle_datn.entity.ChiTietDotGiamGia;
    import org.example.chocostyle_datn.entity.ChiTietDotGiamGiaId;
    import org.example.chocostyle_datn.entity.ChiTietSanPham;
    import org.example.chocostyle_datn.entity.DotGiamGia;
    import org.example.chocostyle_datn.model.Request.DotGiamGiaRequest;
    import org.example.chocostyle_datn.model.Response.DotGiamGiaResponse;
    import org.example.chocostyle_datn.repository.ChiTietDotGiamGiaRepository;
    import org.example.chocostyle_datn.repository.ChiTietSanPhamRepository;
    import org.example.chocostyle_datn.repository.DotGiamGiaRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;

    import java.time.LocalDate;
    import java.util.List;
    import java.util.stream.Collectors;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.data.domain.Pageable;

    @Service
    public class DotGiamGiaService {
        @Autowired
        private ChiTietSanPhamRepository chiTietSanPhamRepository;

        @Autowired
        private ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository;

        @Autowired
        private DotGiamGiaRepository dotGiamGiaRepository;
        public List<DotGiamGiaResponse> getAllDGG() {
            return dotGiamGiaRepository.findAll()
                    .stream()
                    .map(this::toResponse)
                    .collect(Collectors.toList());
        }


        public DotGiamGiaResponse getById(Integer id) {
            DotGiamGia dgg = dotGiamGiaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("DotGiamGia not found"));

            return toResponse(dgg);
        }


        @Transactional
        public DotGiamGia createDGG(DotGiamGiaRequest request) {

            validateNgay(request.getNgayBatDau(), request.getNgayKetThuc());
            if (dotGiamGiaRepository
                    .existsByTenDotGiamGiaIgnoreCase(request.getTenDotGiamGia())) {
                throw new IllegalArgumentException("Tên đợt giảm giá đã tồn tại");
            }


            DotGiamGia dgg = new DotGiamGia();
            dgg.setMaDotGiamGia(generateNextMaDGG());
            dgg.setTenDotGiamGia(request.getTenDotGiamGia());
            dgg.setGiaTriGiam(request.getGiaTriGiam());
            dgg.setNgayBatDau(request.getNgayBatDau());
            dgg.setNgayKetThuc(request.getNgayKetThuc());
            dgg.setTrangThai(tinhTrangThai(
                    request.getNgayBatDau(),
                    request.getNgayKetThuc()
            ));

            dgg = dotGiamGiaRepository.save(dgg);

            if (request.getChiTietSanPhamIds() != null) {
                for (Integer ctspId : request.getChiTietSanPhamIds()) {

                    ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(ctspId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Không tìm thấy CTSP: " + ctspId
                                    )
                            );

                    ChiTietDotGiamGia ct = new ChiTietDotGiamGia();

                    ChiTietDotGiamGiaId id = new ChiTietDotGiamGiaId();
                    id.setIdDotGiamGia(dgg.getId());
                    id.setIdSpct(ctspId);

                    ct.setId(id);
                    ct.setIdDotGiamGia(dgg);
                    ct.setIdSpct(ctsp);

                    ct.setSoLuongTonKhoKhuyenMai(0);
                    ct.setTrangThai(1);
                    ct.setNgayTao(LocalDate.now());
                    ct.setNguoiTao("admin");

                    chiTietDotGiamGiaRepository.save(ct);
                }
            }

            return dgg;
        }

        @Transactional
        public DotGiamGia updateDGG(Integer id, DotGiamGiaRequest request) {

            validateNgay(request.getNgayBatDau(), request.getNgayKetThuc());

            DotGiamGia dgg = dotGiamGiaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("DotGiamGia not found"));
            if (dotGiamGiaRepository.existsTenIgnoreCaseForUpdate(
                    request.getTenDotGiamGia(), id)) {
                throw new IllegalArgumentException("Tên đợt giảm giá đã tồn tại");
            }
            dgg.setTenDotGiamGia(request.getTenDotGiamGia());
            dgg.setGiaTriGiam(request.getGiaTriGiam());
            dgg.setNgayBatDau(request.getNgayBatDau());
            dgg.setNgayKetThuc(request.getNgayKetThuc());
            dgg.setTrangThai(
                    tinhTrangThai(request.getNgayBatDau(), request.getNgayKetThuc())
            );

            dotGiamGiaRepository.save(dgg);


            chiTietDotGiamGiaRepository.deleteById_IdDotGiamGia(id);
            if (request.getChiTietSanPhamIds() != null) {
                for (Integer ctspId : request.getChiTietSanPhamIds()) {

                    ChiTietSanPham ctsp = chiTietSanPhamRepository.findById(ctspId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Không tìm thấy CTSP: " + ctspId
                                    )
                            );

                    ChiTietDotGiamGia ct = new ChiTietDotGiamGia();

                    ChiTietDotGiamGiaId ctId = new ChiTietDotGiamGiaId();
                    ctId.setIdDotGiamGia(dgg.getId());
                    ctId.setIdSpct(ctspId);

                    ct.setId(ctId);
                    ct.setIdDotGiamGia(dgg);
                    ct.setIdSpct(ctsp);

                    ct.setSoLuongTonKhoKhuyenMai(0);
                    ct.setTrangThai(1);
                    ct.setNgayTao(LocalDate.now());
                    ct.setNguoiTao("admin");

                    chiTietDotGiamGiaRepository.save(ct);
                }
            }

            return dgg;
        }

        public Boolean deleteDGG(Integer id) {
            if (!dotGiamGiaRepository.existsById(id)) {
                throw new IllegalArgumentException("DotGiamGia not found");
            }
            dotGiamGiaRepository.deleteById(id);
            return true;
        }
        private String generateNextMaDGG() {
            DotGiamGia last = dotGiamGiaRepository.findTopByOrderByIdDesc();

            int nextNumber = 1;

            if (last != null && last.getMaDotGiamGia() != null) {
                // VD: DGG007 → lấy 007
                String numberPart = last.getMaDotGiamGia().substring(3);
                nextNumber = Integer.parseInt(numberPart) + 1;
            }

            return String.format("DGG%03d", nextNumber);
        }
        public Page<DotGiamGiaResponse> filterPage(
                String keyword,
                Integer trangThai,
                LocalDate start,
                LocalDate end,
                int page,
                int size
        ) {
            Pageable pageable = PageRequest.of(page, size);

            return dotGiamGiaRepository
                    .filter(keyword, trangThai, start, end, pageable)
                    .map(this::toResponse);
        }

        private Integer tinhTrangThai(LocalDate ngayBatDau, LocalDate ngayKetThuc) {
            LocalDate today = LocalDate.now();
            if (today.isBefore(ngayBatDau)) {
                return 2;
            }
            if ((today.isEqual(ngayBatDau) || today.isAfter(ngayBatDau))
                    && (today.isEqual(ngayKetThuc) || today.isBefore(ngayKetThuc))) {
                return 1;
            }

            return 0;
        }
        private void validateNgay(LocalDate ngayBatDau, LocalDate ngayKetThuc) {
            if (ngayBatDau == null || ngayKetThuc == null) {
                throw new IllegalArgumentException("Ngày bắt đầu và ngày kết thúc không được để trống");
            }

            if (ngayKetThuc.isBefore(ngayBatDau)) {
                throw new IllegalArgumentException(
                        "Ngày kết thúc không được trước ngày bắt đầu"
                );
            }
        }
        public DotGiamGiaResponse toggleTrangThai(Integer id) {
            DotGiamGia dgg = dotGiamGiaRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("DotGiamGia not found"));


            dgg.setTrangThai(dgg.getTrangThai() == 0 ? 1 : 0);

            dotGiamGiaRepository.save(dgg);

            return toResponse(dgg);
        }

        private Integer tinhTrangThaiThuc(DotGiamGia dgg) {
            if (dgg.getTrangThai() != null && dgg.getTrangThai() == 0) {
                return 0;
            }
            LocalDate today = LocalDate.now();
            if (today.isBefore(dgg.getNgayBatDau())) {
                return 2;
            }
            if (today.isAfter(dgg.getNgayKetThuc())) {
                return 0;
            }
            return 1;
        }
        private DotGiamGiaResponse toResponse(DotGiamGia dgg) {
            DotGiamGiaResponse res = new DotGiamGiaResponse();

            res.setId(dgg.getId());
            res.setMaDotGiamGia(dgg.getMaDotGiamGia());
            res.setTenDotGiamGia(dgg.getTenDotGiamGia());
            res.setGiaTriGiam(dgg.getGiaTriGiam());
            res.setNgayBatDau(dgg.getNgayBatDau());
            res.setNgayKetThuc(dgg.getNgayKetThuc());
            res.setTrangThai(tinhTrangThaiThuc(dgg));
            List<Integer> ctspIds =
                    chiTietDotGiamGiaRepository
                            .findById_IdDotGiamGia(dgg.getId())
                            .stream()
                            .map(ct -> ct.getId().getIdSpct())
                            .toList();

            res.setChiTietSanPhamIds(ctspIds);

            return res;
        }


    }


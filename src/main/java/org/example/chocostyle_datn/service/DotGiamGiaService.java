package org.example.chocostyle_datn.service;


import jakarta.transaction.Transactional;
import org.example.chocostyle_datn.entity.ChiTietDotGiamGia;
import org.example.chocostyle_datn.entity.ChiTietDotGiamGiaId;
import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.entity.DotGiamGia;
import org.example.chocostyle_datn.model.Request.DotGiamGiaRequest;
import org.example.chocostyle_datn.model.Response.DotGiamGiaResponse;
import org.example.chocostyle_datn.model.Response.DotGiamGiaSanPhamResponse;
import org.example.chocostyle_datn.repository.ChiTietDotGiamGiaRepository;
import org.example.chocostyle_datn.repository.ChiTietSanPhamRepository;
import org.example.chocostyle_datn.repository.DotGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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


        // Làm sạch keyword để SQL không bị lỗi nếu truyền chuỗi rỗng
        String cleanKeyword = (keyword == null || keyword.trim().isEmpty()) ? null : keyword.trim();


        return dotGiamGiaRepository
                .filter(cleanKeyword, trangThai, start, end, pageable)
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


        LocalDate today = LocalDate.now();


        // Nếu đang BẬT (1 hoặc 2) → TẮT
        if (dgg.getTrangThai() == 1 || dgg.getTrangThai() == 2) {
            dgg.setTrangThai(0);
        }
        // Nếu đang TẮT → BẬT LẠI (tự tính theo ngày)
        else {
            Integer newStatus = tinhTrangThai(
                    dgg.getNgayBatDau(),
                    dgg.getNgayKetThuc()
            );


            // Nếu đã hết hạn → không bật, vẫn là 0
            if (newStatus == 0) {
                dgg.setTrangThai(0);
            } else {
                dgg.setTrangThai(newStatus); // 1 hoặc 2
            }
        }


        dotGiamGiaRepository.save(dgg);
        return toResponse(dgg);
    }


    private Integer tinhTrangThaiThuc(DotGiamGia dgg) {
        // Không tính toán theo LocalDate.now() ở đây nữa
        // Chỉ cần trả về đúng những gì đang có trong DB
        return dgg.getTrangThai();
    }


    private DotGiamGiaResponse toResponse(DotGiamGia dgg) {
        DotGiamGiaResponse res = new DotGiamGiaResponse();


        res.setId(dgg.getId());
        res.setMaDotGiamGia(dgg.getMaDotGiamGia());
        res.setTenDotGiamGia(dgg.getTenDotGiamGia());
        res.setGiaTriGiam(dgg.getGiaTriGiam());
        res.setNgayBatDau(dgg.getNgayBatDau());
        res.setNgayKetThuc(dgg.getNgayKetThuc());
        res.setTrangThai(dgg.getTrangThai());


        List<ChiTietDotGiamGia> list =
                chiTietDotGiamGiaRepository.findById_IdDotGiamGia(dgg.getId());


        // CTSP IDs (giữ lại cho FE nếu cần)
        res.setChiTietSanPhamIds(
                list.stream()
                        .map(ct -> ct.getId().getIdSpct())
                        .toList()
        );


        // Gom theo sản phẩm
        Map<Integer, List<ChiTietDotGiamGia>> groupBySp =
                list.stream()
                        .collect(Collectors.groupingBy(
                                ct -> ct.getIdSpct().getIdSanPham().getId()
                        ));


        List<DotGiamGiaSanPhamResponse> sanPhamApDung =
                groupBySp.values().stream()
                        .map(ctList -> {
                            ChiTietSanPham ctsp = ctList.get(0).getIdSpct();


                            DotGiamGiaSanPhamResponse spRes =
                                    new DotGiamGiaSanPhamResponse();


                            spRes.setIdSp(ctsp.getIdSanPham().getId());
                            spRes.setMaSp(ctsp.getIdSanPham().getMaSp());
                            spRes.setTenSp(ctsp.getIdSanPham().getTenSp());


                            spRes.setChiTietSanPhamIds(
                                    ctList.stream()
                                            .map(ct -> ct.getId().getIdSpct())
                                            .toList()
                            );


                            return spRes;
                        })
                        .toList();


        res.setSanPhamApDung(sanPhamApDung);


        return res;
    }

    @Scheduled(cron = "1 0 0 * * *")
    @Transactional
    public void tuDongCapNhatTrangThai() {
        List<DotGiamGia> all = dotGiamGiaRepository.findAll();
        LocalDate today = LocalDate.now();


        for (DotGiamGia dgg : all) {
            // Lấy trạng thái đúng theo ngày
            Integer statusTheoNgay = tinhTrangThai(dgg.getNgayBatDau(), dgg.getNgayKetThuc());


       /* LOGIC MỚI:
          - Nếu đợt giảm giá đã hết hạn (statusTheoNgay == 0), ép về 0 bất kể Admin đang để gì.
          - Nếu vẫn còn hạn (statusTheoNgay là 1 hoặc 2), nhưng Admin ĐÃ CHỦ ĐỘNG DỪNG (trangThai == 0),
            thì KHÔNG tự ý bật lại.
       */
            if (statusTheoNgay == 0) {
                dgg.setTrangThai(0);
            } else {
                // Chỉ cập nhật nếu Admin chưa chủ động dừng đợt này
                if (dgg.getTrangThai() != 0) {
                    dgg.setTrangThai(statusTheoNgay);
                }
            }
        }
        dotGiamGiaRepository.saveAll(all);
    }


}




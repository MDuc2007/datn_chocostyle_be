package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.CaLamViec;
import org.example.chocostyle_datn.entity.LichLamViec;
import org.example.chocostyle_datn.entity.NhanVien;
import org.example.chocostyle_datn.model.Request.LichLamViecRequest;
import org.example.chocostyle_datn.repository.CaLamViecRepository;
import org.example.chocostyle_datn.repository.LichLamViecRepository;
import org.example.chocostyle_datn.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LichLamViecService {

    @Autowired
    private LichLamViecRepository lichRepo;
    @Autowired
    private NhanVienRepository nvRepo;
    @Autowired
    private CaLamViecRepository caRepo;

    // 1. GET
    public List<LichLamViec> getSchedules(LocalDate from, LocalDate to) {
        if (from == null || to == null) return lichRepo.findAllByOrderByNgayLamViecDesc();
        return lichRepo.findByNgayLamViecBetweenOrderByNgayLamViecDesc(from, to);
    }

    // 2. CREATE
    public LichLamViec createSchedule(LichLamViecRequest req) {
        Object[] entities = validateCommon(null, req);
        NhanVien nv = (NhanVien) entities[0];
        CaLamViec ca = (CaLamViec) entities[1];

        LichLamViec lich = new LichLamViec();
        lich.setNhanVien(nv);
        lich.setCaLamViec(ca);
        lich.setNgayLamViec(req.getNgayLamViec());
        lich.setGhiChu(req.getGhiChu());
        lich.setTrangThai(req.getTrangThai() != null ? req.getTrangThai() : 2);

        return lichRepo.save(lich);
    }

    // 3. UPDATE
    public LichLamViec updateSchedule(Integer id, LichLamViecRequest req) {
        LichLamViec lich = lichRepo.findById(id).orElseThrow(() -> new RuntimeException("Lịch không tồn tại"));

        if (lich.getNgayLamViec().isBefore(LocalDate.now())) {
            throw new RuntimeException("Lịch làm việc trong quá khứ không thể sửa đổi!");
        }

        Object[] entities = validateCommon(id, req);
        NhanVien nv = (NhanVien) entities[0];
        CaLamViec ca = (CaLamViec) entities[1];

        lich.setNhanVien(nv);
        lich.setCaLamViec(ca);
        lich.setNgayLamViec(req.getNgayLamViec());
        lich.setGhiChu(req.getGhiChu());
        lich.setTrangThai(req.getTrangThai());

        return lichRepo.save(lich);
    }

    // 4. DELETE
    public void deleteSchedule(Integer id) {
        LichLamViec lich = lichRepo.findById(id).orElseThrow(() -> new RuntimeException("Lịch không tồn tại"));
        if (lich.getNgayLamViec().isBefore(LocalDate.now())) {
            throw new RuntimeException("Không thể xóa lịch sử làm việc trong quá khứ!");
        }
        lichRepo.delete(lich);
    }

    // 5. BATCH CREATE
    @Transactional(rollbackFor = Exception.class)
    public List<LichLamViec> createBatchSchedule(List<LichLamViecRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new RuntimeException("Danh sách lịch làm việc trống!");
        }

        // 👉 ĐOẠN THÊM MỚI: TÌM NGÀY NHỎ NHẤT VÀ LỚN NHẤT TRONG DANH SÁCH LẶP
        LocalDate minDate = requests.get(0).getNgayLamViec();
        LocalDate maxDate = requests.get(0).getNgayLamViec();

        for (LichLamViecRequest req : requests) {
            if (req.getNgayLamViec().isBefore(minDate)) minDate = req.getNgayLamViec();
            if (req.getNgayLamViec().isAfter(maxDate)) maxDate = req.getNgayLamViec();
        }

        // Kiểm tra khoảng cách: Nếu vượt quá 365 ngày thì chặn luôn
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(minDate, maxDate);
        if (daysBetween > 365) {
            throw new RuntimeException("Vượt quá giới hạn! Hệ thống chỉ cho phép lặp lịch tối đa trong vòng 1 năm (365 ngày).");
        }

        List<LichLamViec> result = new ArrayList<>();
        String batchId = UUID.randomUUID().toString();

        for (LichLamViecRequest req : requests) {
            try {
                Object[] entities = validateCommon(null, req);
                NhanVien nv = (NhanVien) entities[0];
                CaLamViec ca = (CaLamViec) entities[1];

                LichLamViec lich = new LichLamViec();
                lich.setNhanVien(nv);
                lich.setCaLamViec(ca);
                lich.setNgayLamViec(req.getNgayLamViec());
                lich.setGhiChu(req.getGhiChu());
                lich.setTrangThai(req.getTrangThai() != null ? req.getTrangThai() : 2); // Trạng thái 2: Chờ làm
                lich.setMaLapLai(batchId);
                result.add(lichRepo.save(lich));
            } catch (RuntimeException e) {
                throw new RuntimeException("Lỗi xếp lịch ngày " + req.getNgayLamViec() + ": " + e.getMessage());
            }
        }
        return result;
    }

    // 6. DELETE SERIES
    @Transactional
    public void deleteScheduleSeries(String maLapLai) {
        List<LichLamViec> list = lichRepo.findByMaLapLaiAndNgayLamViecGreaterThanEqual(maLapLai, LocalDate.now());
        if (list.isEmpty()) throw new RuntimeException("Không có chuỗi lịch hợp lệ hoặc các lịch đã diễn ra trong quá khứ!");
        lichRepo.deleteAll(list);
    }

    // 7. UPDATE SERIES
    @Transactional
    public void updateScheduleSeries(String maLapLai, LichLamViecRequest req) {
        List<LichLamViec> list = lichRepo.findByMaLapLaiAndNgayLamViecGreaterThanEqual(maLapLai, LocalDate.now());
        if (list.isEmpty()) throw new RuntimeException("Không có chuỗi lịch hợp lệ để cập nhật!");

        for (LichLamViec lich : list) {
            LichLamViecRequest tempReq = new LichLamViecRequest();
            tempReq.setIdNhanVien(req.getIdNhanVien());
            tempReq.setIdCa(req.getIdCa());
            tempReq.setNgayLamViec(lich.getNgayLamViec());

            Object[] entities = validateCommon(lich.getId(), tempReq);
            NhanVien nv = (NhanVien) entities[0];
            CaLamViec ca = (CaLamViec) entities[1];

            lich.setNhanVien(nv);
            lich.setCaLamViec(ca);
            lich.setGhiChu(req.getGhiChu());
            lich.setTrangThai(req.getTrangThai());
            lichRepo.save(lich);
        }
    }


    // ================= VALIDATION LOGIC =================
    private Object[] validateCommon(Integer currentId, LichLamViecRequest req) {
        if (req.getNgayLamViec().isBefore(LocalDate.now())) {
            throw new RuntimeException("Không thể xếp lịch hoặc thao tác với ngày trong quá khứ!");
        }

        NhanVien nv = nvRepo.findById(req.getIdNhanVien())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại!"));
        CaLamViec caMoi = caRepo.findById(req.getIdCa())
                .orElseThrow(() -> new RuntimeException("Ca làm việc không tồn tại!"));

        if (nv.getTrangThai() == null || nv.getTrangThai() == 0) {
            throw new RuntimeException("Không thể xếp lịch cho nhân viên đã nghỉ việc hoặc tài khoản bị khóa!");
        }
        if (caMoi.getTrangThai() == null || caMoi.getTrangThai() == 0) {
            throw new RuntimeException("Ca làm việc này hiện đang tạm ngưng hoạt động!");
        }

        // Check: 1 Nhân viên không làm 2 ca trùng giờ
        validateEmployeeConflict(currentId, nv.getId(), caMoi, req.getNgayLamViec());

        // 👉 ĐÃ TẮT CHECK NÀY: Giờ đây 1 ca có thể có nhiều nhân viên cùng làm
        // validateShiftOccupancy(currentId, caMoi.getIdCa(), req.getNgayLamViec());

        return new Object[]{nv, caMoi};
    }

    private void validateEmployeeConflict(Integer currentId, Integer idNv, CaLamViec caMoi, LocalDate ngay) {
        List<LichLamViec> existing = lichRepo.findByNhanVienAndNgay(idNv, ngay);

        for (LichLamViec old : existing) {
            // 1. Bỏ qua nếu đang sửa chính lịch đó
            if (currentId != null && old.getId().equals(currentId)) {
                continue;
            }

            CaLamViec caCu = old.getCaLamViec();

            // 2. CHẶN TUYỆT ĐỐI: Không bao giờ được phân trùng đúng cái ID Ca đó lần 2 (Dù đã đóng hay chưa)
            if (caCu.getIdCa().equals(caMoi.getIdCa())) {
                // Chỉ chặn nếu ca cũ không phải là ca đã Hủy (nếu sau này bạn có trạng thái Hủy)
                if (old.getTrangThai() != null && old.getTrangThai() != 0) {
                    throw new RuntimeException("Nhân viên này đã có lịch cho ca [" + caCu.getTenCa() + "] trong ngày này!");
                }
            }

            // 3. KIỂM TRA TRÙNG GIỜ (Overlap)
            // Nếu ca cũ ĐÃ ĐÓNG (1), chúng ta bỏ qua không check trùng giờ nữa (cho phép phân ca khác trùng giờ)
            if (old.getTrangThai() != null && old.getTrangThai() == 1) {
                continue;
            }

            // Nếu ca cũ đang ở trạng thái Đang chờ (2) hoặc Đang làm (3), tiến hành check trùng khung giờ
            boolean isOverlap = caMoi.getGioBatDau().isBefore(caCu.getGioKetThuc())
                    && caMoi.getGioKetThuc().isAfter(caCu.getGioBatDau());

            if (isOverlap) {
                throw new RuntimeException("Nhân viên này đang có một ca khác chưa kết thúc trùng khung giờ: ["
                        + caCu.getTenCa() + " từ " + caCu.getGioBatDau() + " - " + caCu.getGioKetThuc() + "] !");
            }
        }
    }

    // Hàm này giữ lại nhưng không gọi đến nữa (để dành nếu sau này muốn bật lại tính năng)
    private void validateShiftOccupancy(Integer currentId, Integer idCa, LocalDate ngay) {
        List<LichLamViec> occupiedList = lichRepo.findByCaAndNgay(idCa, ngay);

        for (LichLamViec item : occupiedList) {
            if (currentId != null && item.getId().equals(currentId)) continue;
            if (item.getTrangThai() == 0) continue;

            throw new RuntimeException("Ca làm việc này đã có nhân viên ("
                    + item.getNhanVien().getHoTen() + ") đăng ký trong ngày " + ngay + ".");
        }
    }

    // ================= CHỨC NĂNG TÌM KIẾM =================
    public Page<LichLamViec> searchLichLamViec(String keyword, LocalDate fromDate, LocalDate toDate, Integer trangThai, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayLamViec"));
        LocalDate start = (fromDate != null) ? fromDate : LocalDate.of(2000, 1, 1);
        LocalDate end = (toDate != null) ? toDate : LocalDate.of(2100, 12, 31);

        return lichRepo.searchLichLamViec(keyword, start, end, trangThai, pageable);
    }

    public List<LichLamViec> getMySchedules(Integer idNv) {
        return lichRepo.findByNhanVien_Id(idNv);
    }

    public Page<LichLamViec> searchMySchedules(Integer idNv, LocalDate fromDate, LocalDate toDate, Integer trangThai, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayLamViec"));
        LocalDate start = (fromDate != null) ? fromDate : LocalDate.of(2000, 1, 1);
        LocalDate end = (toDate != null) ? toDate : LocalDate.of(2100, 12, 31);
        return lichRepo.searchMySchedules(idNv, start, end, trangThai, pageable);
    }
}
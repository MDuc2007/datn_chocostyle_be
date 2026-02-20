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
        validateCommon(null, req); // Gọi hàm validate chung

        NhanVien nv = nvRepo.findById(req.getIdNhanVien()).orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));
        CaLamViec ca = caRepo.findById(req.getIdCa()).orElseThrow(() -> new RuntimeException("Ca làm việc không tồn tại"));

        LichLamViec lich = new LichLamViec();
        lich.setNhanVien(nv);
        lich.setCaLamViec(ca);
        lich.setNgayLamViec(req.getNgayLamViec());
        lich.setGhiChu(req.getGhiChu());
        lich.setTrangThai(req.getTrangThai() != null ? req.getTrangThai() : 1);

        return lichRepo.save(lich);
    }

    // 3. UPDATE
    public LichLamViec updateSchedule(Integer id, LichLamViecRequest req) {
        LichLamViec lich = lichRepo.findById(id).orElseThrow(() -> new RuntimeException("Lịch không tồn tại"));

        // Check quá khứ
        if (lich.getNgayLamViec().isBefore(LocalDate.now())) throw new RuntimeException("Lịch quá khứ không thể sửa!");

        validateCommon(id, req); // Gọi hàm validate chung

        NhanVien nv = nvRepo.findById(req.getIdNhanVien()).orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));
        CaLamViec ca = caRepo.findById(req.getIdCa()).orElseThrow(() -> new RuntimeException("Ca làm việc không tồn tại"));

        lich.setNhanVien(nv);
        lich.setCaLamViec(ca);
        lich.setNgayLamViec(req.getNgayLamViec()); // Cập nhật ngày mới
        lich.setGhiChu(req.getGhiChu());
        lich.setTrangThai(req.getTrangThai());

        return lichRepo.save(lich);
    }

    // 4. DELETE
    public void deleteSchedule(Integer id) {
        LichLamViec lich = lichRepo.findById(id).orElseThrow(() -> new RuntimeException("Lịch không tồn tại"));
        if (lich.getNgayLamViec().isBefore(LocalDate.now())) throw new RuntimeException("Không thể xóa lịch sử quá khứ!");
        lichRepo.delete(lich);
    }

    // 5. BATCH CREATE
    @Transactional(rollbackFor = Exception.class)
    public List<LichLamViec> createBatchSchedule(List<LichLamViecRequest> requests) {
        List<LichLamViec> result = new ArrayList<>();
        String batchId = UUID.randomUUID().toString();

        for (LichLamViecRequest req : requests) {
            try {
                validateCommon(null, req); // Validate từng cái

                NhanVien nv = nvRepo.findById(req.getIdNhanVien()).orElseThrow(() -> new RuntimeException("NV không tồn tại"));
                CaLamViec ca = caRepo.findById(req.getIdCa()).orElseThrow(() -> new RuntimeException("Ca không tồn tại"));

                LichLamViec lich = new LichLamViec();
                lich.setNhanVien(nv);
                lich.setCaLamViec(ca);
                lich.setNgayLamViec(req.getNgayLamViec());
                lich.setGhiChu(req.getGhiChu());
                lich.setTrangThai(req.getTrangThai() != null ? req.getTrangThai() : 1);
                lich.setMaLapLai(batchId);

                result.add(lichRepo.save(lich));
            } catch (RuntimeException e) {
                throw new RuntimeException("Lỗi ngày " + req.getNgayLamViec() + ": " + e.getMessage());
            }
        }
        return result;
    }

    // 6. DELETE SERIES
    @Transactional
    public void deleteScheduleSeries(String maLapLai) {
        List<LichLamViec> list = lichRepo.findByMaLapLaiAndNgayLamViecGreaterThanEqual(maLapLai, LocalDate.now());
        if (list.isEmpty()) throw new RuntimeException("Không có lịch hợp lệ để xóa!");
        lichRepo.deleteAll(list);
    }

    // 7. UPDATE SERIES
    @Transactional
    public void updateScheduleSeries(String maLapLai, LichLamViecRequest req) {
        List<LichLamViec> list = lichRepo.findByMaLapLaiAndNgayLamViecGreaterThanEqual(maLapLai, LocalDate.now());
        if (list.isEmpty()) throw new RuntimeException("Không có lịch hợp lệ để cập nhật!");

        NhanVien nv = nvRepo.findById(req.getIdNhanVien()).orElseThrow(() -> new RuntimeException("NV không tồn tại"));
        CaLamViec ca = caRepo.findById(req.getIdCa()).orElseThrow(() -> new RuntimeException("Ca không tồn tại"));

        for (LichLamViec lich : list) {
            // Validate từng lịch (Giữ nguyên ngày cũ của nó, chỉ đổi người/ca)
            LichLamViecRequest tempReq = new LichLamViecRequest();
            tempReq.setIdNhanVien(req.getIdNhanVien());
            tempReq.setIdCa(req.getIdCa());
            tempReq.setNgayLamViec(lich.getNgayLamViec()); // Giữ nguyên ngày

            validateCommon(lich.getId(), tempReq);

            lich.setNhanVien(nv);
            lich.setCaLamViec(ca);
            lich.setGhiChu(req.getGhiChu());
            lich.setTrangThai(req.getTrangThai());
            lichRepo.save(lich);
        }
    }

    // ================= VALIDATION LOGIC =================

    // Hàm gom nhóm validate
    private void validateCommon(Integer currentId, LichLamViecRequest req) {
        if (req.getNgayLamViec().isBefore(LocalDate.now())) {
            throw new RuntimeException("Không thể thao tác với ngày trong quá khứ!");
        }

        // 1. Check: 1 Nhân viên không làm 2 ca trùng giờ (Luật cũ)
        validateEmployeeConflict(currentId, req.getIdNhanVien(), req.getIdCa(), req.getNgayLamViec());

        // 2. Check: 1 Ca chỉ có 1 nhân viên (Luật MỚI)
        validateShiftOccupancy(currentId, req.getIdCa(), req.getNgayLamViec());
    }

    // Validate 1: Nhân viên trùng giờ (Employee-centric)
    private void validateEmployeeConflict(Integer currentId, Integer idNv, Integer idCa, LocalDate ngay) {
        CaLamViec caMoi = caRepo.findById(idCa).orElseThrow(() -> new RuntimeException("Ca k tồn tại"));
        List<LichLamViec> existing = lichRepo.findByNhanVienAndNgay(idNv, ngay);

        for (LichLamViec old : existing) {
            if (currentId != null && old.getId().equals(currentId)) continue;
            if (old.getTrangThai() == 0) continue;

            CaLamViec caCu = old.getCaLamViec();
            boolean isOverlap = caMoi.getGioBatDau().isBefore(caCu.getGioKetThuc())
                    && caMoi.getGioKetThuc().isAfter(caCu.getGioBatDau());
            if (isOverlap) {
                throw new RuntimeException("Nhân viên này đã có lịch trùng giờ (" + caCu.getTenCa() + ")!");
            }
        }
    }

    // Validate 2: Ca đã có người làm (Shift-centric) - LOGIC MỚI
    private void validateShiftOccupancy(Integer currentId, Integer idCa, LocalDate ngay) {
        // Tìm xem trong ngày đó, ca đó đã có ai làm chưa
        List<LichLamViec> occupiedList = lichRepo.findByCaAndNgay(idCa, ngay);

        for (LichLamViec item : occupiedList) {
            // Nếu là update, bỏ qua chính nó
            if (currentId != null && item.getId().equals(currentId)) continue;

            // Nếu lịch kia đang ẩn/hủy (status 0) thì coi như trống, cho phép ghi đè
            if (item.getTrangThai() == 0) continue;

            // Nếu đã có record active -> Báo lỗi
            throw new RuntimeException("Ca làm việc này đã có nhân viên ("
                    + item.getNhanVien().getHoTen() + ") đăng ký trong ngày " + ngay + ". Mỗi ca chỉ được 1 người làm!");
        }
    }
    // THÊM HÀM SEARCH NÀY
    public Page<LichLamViec> searchLichLamViec(String keyword, LocalDate fromDate, LocalDate toDate, Integer trangThai, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayLamViec"));

        // Mẹo fix lỗi SQL Server: Nếu không truyền ngày, lấy từ năm 2000 đến 2100
        LocalDate start = (fromDate != null) ? fromDate : LocalDate.of(2000, 1, 1);
        LocalDate end = (toDate != null) ? toDate : LocalDate.of(2100, 12, 31);

        return lichRepo.searchLichLamViec(keyword, start, end, trangThai, pageable);
    }
}
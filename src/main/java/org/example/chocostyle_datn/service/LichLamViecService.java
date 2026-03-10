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
        // Validate và lấy Entity ra luôn để khỏi query DB 2 lần
        Object[] entities = validateCommon(null, req);
        NhanVien nv = (NhanVien) entities[0];
        CaLamViec ca = (CaLamViec) entities[1];

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

        // Check khóa lịch quá khứ của cái lịch CŨ trước khi sửa
        if (lich.getNgayLamViec().isBefore(LocalDate.now())) {
            throw new RuntimeException("Lịch làm việc trong quá khứ không thể sửa đổi!");
        }

        // Validate cái req MỚI truyền vào
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

    // 5. BATCH CREATE (Sắp xếp nhiều lịch/chuỗi lịch)
    @Transactional(rollbackFor = Exception.class)
    public List<LichLamViec> createBatchSchedule(List<LichLamViecRequest> requests) {
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
                lich.setTrangThai(req.getTrangThai() != null ? req.getTrangThai() : 1);

                // Cần đảm bảo Entity LichLamViec của bạn đã có trường maLapLai
                // lich.setMaLapLai(batchId);

                result.add(lichRepo.save(lich));
            } catch (RuntimeException e) {
                // Sẽ ném ra lỗi để rollback toàn bộ nếu 1 ngày bị lỗi trùng ca
                throw new RuntimeException("Lỗi xếp lịch ngày " + req.getNgayLamViec() + ": " + e.getMessage());
            }
        }
        return result;
    }

    // 6. DELETE SERIES
    @Transactional
    public void deleteScheduleSeries(String maLapLai) {
        // Chỉ lấy các lịch từ hôm nay trở đi để xóa, bảo toàn lịch sử
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
            // Validate từng lịch con (Giữ nguyên ngày cũ của nó, chỉ đổi người/ca)
            LichLamViecRequest tempReq = new LichLamViecRequest();
            tempReq.setIdNhanVien(req.getIdNhanVien());
            tempReq.setIdCa(req.getIdCa());
            tempReq.setNgayLamViec(lich.getNgayLamViec()); // Giữ nguyên ngày của phần tử đó

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


    // ================= VALIDATION LOGIC GỘP =================

    // Trả về Object[] chứa NhanVien và CaLamViec để tiết kiệm số lần Query DB
    private Object[] validateCommon(Integer currentId, LichLamViecRequest req) {
        // 1. Kiểm tra ngày quá khứ
        if (req.getNgayLamViec().isBefore(LocalDate.now())) {
            throw new RuntimeException("Không thể xếp lịch hoặc thao tác với ngày trong quá khứ!");
        }

        // 2. Fetch Data (Chỉ fetch 1 lần)
        NhanVien nv = nvRepo.findById(req.getIdNhanVien())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại!"));
        CaLamViec caMoi = caRepo.findById(req.getIdCa())
                .orElseThrow(() -> new RuntimeException("Ca làm việc không tồn tại!"));

        // 3. Kiểm tra trạng thái làm việc (Đã lấy từ validateLogic cũ đưa vào đây)
        if (nv.getTrangThai() == null || nv.getTrangThai() == 0) {
            throw new RuntimeException("Không thể xếp lịch cho nhân viên đã nghỉ việc hoặc tài khoản bị khóa!");
        }
        if (caMoi.getTrangThai() == null || caMoi.getTrangThai() == 0) {
            throw new RuntimeException("Ca làm việc này hiện đang tạm ngưng hoạt động!");
        }

        // 4. Check: 1 Nhân viên không làm 2 ca trùng giờ
        validateEmployeeConflict(currentId, nv.getId(), caMoi, req.getNgayLamViec());

        // 5. Check: 1 Ca chỉ có 1 nhân viên
        validateShiftOccupancy(currentId, caMoi.getIdCa(), req.getNgayLamViec());

        // Trả về entities để Controller dùng luôn
        return new Object[]{nv, caMoi};
    }

    // Validate 1: Nhân viên trùng giờ (Truyền thẳng đối tượng CaMoi vào để khỏi tốn DB Query)
    private void validateEmployeeConflict(Integer currentId, Integer idNv, CaLamViec caMoi, LocalDate ngay) {
        List<LichLamViec> existing = lichRepo.findByNhanVienAndNgay(idNv, ngay); // Đảm bảo hàm này có trong Repo

        for (LichLamViec old : existing) {
            if (currentId != null && old.getId().equals(currentId)) continue;
            if (old.getTrangThai() == 0) continue; // Bỏ qua lịch đã hủy

            CaLamViec caCu = old.getCaLamViec();
            // Logic Overlap chuẩn: Bắt đầu mới < Kết thúc cũ AND Kết thúc mới > Bắt đầu cũ
            boolean isOverlap = caMoi.getGioBatDau().isBefore(caCu.getGioKetThuc())
                    && caMoi.getGioKetThuc().isAfter(caCu.getGioBatDau());

            if (isOverlap) {
                throw new RuntimeException("Nhân viên này đã có lịch trùng giờ: [" + caCu.getTenCa()
                        + " từ " + caCu.getGioBatDau() + " - " + caCu.getGioKetThuc() + "] !");
            }
        }
    }

    // Validate 2: Ca đã có người làm
    private void validateShiftOccupancy(Integer currentId, Integer idCa, LocalDate ngay) {
        List<LichLamViec> occupiedList = lichRepo.findByCaAndNgay(idCa, ngay); // Đảm bảo hàm này có trong Repo

        for (LichLamViec item : occupiedList) {
            if (currentId != null && item.getId().equals(currentId)) continue;
            if (item.getTrangThai() == 0) continue; // Lịch hủy coi như trống

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
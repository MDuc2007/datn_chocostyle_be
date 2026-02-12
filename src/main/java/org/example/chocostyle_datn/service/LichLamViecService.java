package org.example.chocostyle_datn.service;


import org.example.chocostyle_datn.entity.CaLamViec;
import org.example.chocostyle_datn.entity.LichLamViec;
import org.example.chocostyle_datn.entity.NhanVien;
import org.example.chocostyle_datn.model.Request.LichLamViecRequest;
import org.example.chocostyle_datn.repository.CaLamViecRepository;
import org.example.chocostyle_datn.repository.LichLamViecRepository;
import org.example.chocostyle_datn.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
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


    // 1. LẤY DANH SÁCH (Hỗ trợ lọc theo ngày)
    public List<LichLamViec> getSchedules(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            return lichRepo.findAllByOrderByNgayLamViecDesc();
        }
        return lichRepo.findByNgayLamViecBetweenOrderByNgayLamViecDesc(from, to);
    }


    // 2. TẠO MỚI LỊCH (CREATE ĐƠN)
    public LichLamViec createSchedule(LichLamViecRequest req) {
        if (req.getNgayLamViec().isBefore(LocalDate.now())) {
            throw new RuntimeException("Không thể phân lịch cho ngày trong quá khứ!");
        }
        validateConflict(null, req.getIdNhanVien(), req.getIdCa(), req.getNgayLamViec());


        NhanVien nv = nvRepo.findById(req.getIdNhanVien())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại (ID: " + req.getIdNhanVien() + ")"));
        CaLamViec ca = caRepo.findById(req.getIdCa())
                .orElseThrow(() -> new RuntimeException("Ca làm việc không tồn tại (ID: " + req.getIdCa() + ")"));


        LichLamViec lich = new LichLamViec();
        lich.setNhanVien(nv);
        lich.setCaLamViec(ca);
        lich.setNgayLamViec(req.getNgayLamViec());
        lich.setGhiChu(req.getGhiChu());
        lich.setTrangThai(req.getTrangThai() != null ? req.getTrangThai() : 1);


        return lichRepo.save(lich);
    }


    // 3. CẬP NHẬT LỊCH (UPDATE)
    public LichLamViec updateSchedule(Integer id, LichLamViecRequest req) {
        LichLamViec lich = lichRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch làm việc không tồn tại với ID: " + id));


        if (lich.getNgayLamViec().isBefore(LocalDate.now())) {
            throw new RuntimeException("Lịch này thuộc về quá khứ, không thể chỉnh sửa!");
        }
        if (req.getNgayLamViec().isBefore(LocalDate.now())) {
            throw new RuntimeException("Không thể dời lịch sang ngày trong quá khứ!");
        }


        validateConflict(id, req.getIdNhanVien(), req.getIdCa(), req.getNgayLamViec());


        NhanVien nv = nvRepo.findById(req.getIdNhanVien())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));
        CaLamViec ca = caRepo.findById(req.getIdCa())
                .orElseThrow(() -> new RuntimeException("Ca làm việc không tồn tại"));


        lich.setNhanVien(nv);
        lich.setCaLamViec(ca);
        lich.setNgayLamViec(req.getNgayLamViec());
        lich.setGhiChu(req.getGhiChu());
        lich.setTrangThai(req.getTrangThai());


        return lichRepo.save(lich);
    }


    // 4. XÓA LỊCH (DELETE ĐƠN)
    public void deleteSchedule(Integer id) {
        LichLamViec lich = lichRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch làm việc không tồn tại"));


        if (lich.getNgayLamViec().isBefore(LocalDate.now())) {
            throw new RuntimeException("Không thể xóa lịch sử làm việc đã qua (Dữ liệu này cần để tính lương)!");
        }


        lichRepo.delete(lich);
    }


    // 5. TẠO NHIỀU LỊCH (BATCH CREATE - ĐÃ SỬA CHUẨN)
    @Transactional(rollbackFor = Exception.class)
    public List<LichLamViec> createBatchSchedule(List<LichLamViecRequest> requests) {
        List<LichLamViec> result = new ArrayList<>();


        // Tạo 1 mã UUID chung cho cả đợt này
        String batchId = UUID.randomUUID().toString();


        for (LichLamViecRequest req : requests) {
            try {
                // --- Validation ---
                if (req.getNgayLamViec().isBefore(LocalDate.now())) {
                    throw new RuntimeException("Ngày " + req.getNgayLamViec() + " là quá khứ");
                }
                validateConflict(null, req.getIdNhanVien(), req.getIdCa(), req.getNgayLamViec());


                // --- Map Dữ Liệu ---
                NhanVien nv = nvRepo.findById(req.getIdNhanVien())
                        .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));
                CaLamViec ca = caRepo.findById(req.getIdCa())
                        .orElseThrow(() -> new RuntimeException("Ca làm việc không tồn tại"));


                LichLamViec lich = new LichLamViec();
                lich.setNhanVien(nv);
                lich.setCaLamViec(ca);
                lich.setNgayLamViec(req.getNgayLamViec());
                lich.setGhiChu(req.getGhiChu());
                lich.setTrangThai(req.getTrangThai() != null ? req.getTrangThai() : 1);


                // --- GÁN MÃ LẶP LẠI Ở ĐÂY ---
                lich.setMaLapLai(batchId);


                // --- LƯU ---
                result.add(lichRepo.save(lich));


            } catch (RuntimeException e) {
                // Nếu 1 ngày bị lỗi, rollback toàn bộ transaction
                throw new RuntimeException("Lỗi tại ngày " + req.getNgayLamViec() + ": " + e.getMessage());
            }
        }
        return result;
    }
    // 7. CẬP NHẬT CHUỖI SỰ KIỆN (UPDATE SERIES)
    @Transactional
    public void updateScheduleSeries(String maLapLai, LichLamViecRequest req) {
        // 1. Tìm các lịch TƯƠNG LAI hoặc HÔM NAY thuộc chuỗi này
        List<LichLamViec> listToUpdate = lichRepo.findByMaLapLaiAndNgayLamViecGreaterThanEqual(maLapLai, LocalDate.now());


        if (listToUpdate.isEmpty()) {
            throw new RuntimeException("Không tìm thấy lịch tương lai nào để cập nhật!");
        }


        // 2. Lấy thông tin tham chiếu mới
        NhanVien nvMoi = nvRepo.findById(req.getIdNhanVien())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));
        CaLamViec caMoi = caRepo.findById(req.getIdCa())
                .orElseThrow(() -> new RuntimeException("Ca làm việc không tồn tại"));


        // 3. Duyệt và cập nhật từng cái
        for (LichLamViec lich : listToUpdate) {
            // Validate trùng giờ cho từng ngày (Trừ chính nó ra nếu cần, nhưng ở đây logic check trùng sẽ bỏ qua nếu ID khớp)
            // Lưu ý: Validate conflict cần truyền ID của lịch đang xét để nó bỏ qua chính nó
            validateConflict(lich.getId(), req.getIdNhanVien(), req.getIdCa(), lich.getNgayLamViec());


            // Cập nhật thông tin (GIỮ NGUYÊN NGÀY LÀM VIỆC CŨ CỦA NÓ)
            lich.setNhanVien(nvMoi);
            lich.setCaLamViec(caMoi);
            lich.setGhiChu(req.getGhiChu());
            lich.setTrangThai(req.getTrangThai());


            // Lưu lại
            lichRepo.save(lich);
        }
    }
    // 6. XÓA CHUỖI SỰ KIỆN (DELETE SERIES)
    @Transactional
    public void deleteScheduleSeries(String maLapLai) {
        // Chỉ xóa những lịch TƯƠNG LAI hoặc HÔM NAY thuộc chuỗi này
        List<LichLamViec> listToDelete = lichRepo.findByMaLapLaiAndNgayLamViecGreaterThanEqual(maLapLai, LocalDate.now());


        if (listToDelete.isEmpty()) {
            throw new RuntimeException("Không tìm thấy lịch nào hợp lệ để xóa (Lịch quá khứ không được xóa)!");
        }


        lichRepo.deleteAll(listToDelete);
    }


    // ================= PRIVATE HELPER METHODS =================


    private void validateConflict(Integer currentId, Integer idNv, Integer idCa, LocalDate ngay) {
        CaLamViec caMoi = caRepo.findById(idCa)
                .orElseThrow(() -> new RuntimeException("Ca làm việc không tồn tại"));


        List<LichLamViec> existingSchedules = lichRepo.findByNhanVienAndNgay(idNv, ngay);


        for (LichLamViec oldSch : existingSchedules) {
            if (currentId != null && oldSch.getId().equals(currentId)) {
                continue;
            }
            if (oldSch.getTrangThai() == 0) continue;


            CaLamViec caCu = oldSch.getCaLamViec();
            boolean isOverlap = caMoi.getGioBatDau().isBefore(caCu.getGioKetThuc())
                    && caMoi.getGioKetThuc().isAfter(caCu.getGioBatDau());


            if (isOverlap) {
                throw new RuntimeException("Nhân viên này đã có lịch trùng giờ ("
                        + caCu.getTenCa() + ": " + caCu.getGioBatDau() + "-" + caCu.getGioKetThuc()
                        + ") trong ngày " + ngay);
            }
        }
    }
}




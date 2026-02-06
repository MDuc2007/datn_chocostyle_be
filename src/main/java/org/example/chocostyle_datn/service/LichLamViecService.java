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


import java.time.LocalDate;
import java.util.List;


@Service
public class LichLamViecService {


    @Autowired
    private LichLamViecRepository lichRepo;
    @Autowired
    private NhanVienRepository nvRepo;
    @Autowired
    private CaLamViecRepository caRepo;


    // 1. Lấy danh sách (Nếu không truyền ngày thì lấy tất cả)
    public List<LichLamViec> getSchedules(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            return lichRepo.findAllByOrderByNgayLamViecDesc();
        }
        return lichRepo.findByDateRange(from, to);
    }


    // 2. Tạo lịch mới
    public LichLamViec createSchedule(LichLamViecRequest req) {
        validateConflict(null, req.getIdNhanVien(), req.getIdCa(), req.getNgayLamViec());


        LichLamViec lich = new LichLamViec();
        return mapAndSave(lich, req);
    }


    // 3. Cập nhật lịch
    public LichLamViec updateSchedule(Integer id, LichLamViecRequest req) {
        LichLamViec lich = lichRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch làm việc không tồn tại"));


        // Check trùng (truyền id hiện tại để loại trừ chính nó)
        validateConflict(id, req.getIdNhanVien(), req.getIdCa(), req.getNgayLamViec());


        return mapAndSave(lich, req);
    }


    // 4. Xóa lịch
    public void deleteSchedule(Integer id) {
        if (!lichRepo.existsById(id)) {
            throw new RuntimeException("Lịch không tồn tại");
        }
        lichRepo.deleteById(id);
    }


    // --- Private Helper Methods ---


    private LichLamViec mapAndSave(LichLamViec lich, LichLamViecRequest req) {
        NhanVien nv = nvRepo.findById(req.getIdNhanVien())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));
        CaLamViec ca = caRepo.findById(req.getIdCa())
                .orElseThrow(() -> new RuntimeException("Ca làm việc không tồn tại"));


        lich.setNhanVien(nv);
        lich.setCaLamViec(ca);
        lich.setNgayLamViec(req.getNgayLamViec());
        lich.setGhiChu(req.getGhiChu());
        lich.setTrangThai(req.getTrangThai() != null ? req.getTrangThai() : 1);


        return lichRepo.save(lich);
    }


    // Logic Check trùng giờ làm
    private void validateConflict(Integer currentId, Integer idNv, Integer idCa, LocalDate ngay) {
        CaLamViec caMoi = caRepo.findById(idCa)
                .orElseThrow(() -> new RuntimeException("Ca làm việc không tồn tại"));


        List<LichLamViec> existingSchedules = lichRepo.findByNhanVienAndNgay(idNv, ngay);


        for (LichLamViec oldSch : existingSchedules) {
            // Nếu đang update, bỏ qua chính bản ghi đang sửa
            if (currentId != null && oldSch.getId().equals(currentId)) {
                continue;
            }


            CaLamViec caCu = oldSch.getCaLamViec();
            // Công thức giao nhau: (StartA < EndB) && (EndA > StartB)
            boolean isConflict = caMoi.getGioBatDau().isBefore(caCu.getGioKetThuc())
                    && caMoi.getGioKetThuc().isAfter(caCu.getGioBatDau());


            if (isConflict) {
                throw new RuntimeException("Trùng giờ với ca: " + caCu.getTenCa()
                        + " (" + caCu.getGioBatDau() + " - " + caCu.getGioKetThuc() + ")");
            }
        }
    }
}


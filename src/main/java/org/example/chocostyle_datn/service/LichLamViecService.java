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


    // 1. LẤY DANH SÁCH (Hỗ trợ lọc theo ngày)
    public List<LichLamViec> getSchedules(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            // Mặc định lấy tất cả, sắp xếp ngày mới nhất lên đầu
            return lichRepo.findAllByOrderByNgayLamViecDesc();
        }
        // Lọc theo khoảng thời gian
        return lichRepo.findByNgayLamViecBetweenOrderByNgayLamViecDesc(from, to);
    }


    // 2. TẠO MỚI LỊCH (CREATE)
    public LichLamViec createSchedule(LichLamViecRequest req) {
        // Validation 1: Không được tạo lịch cho quá khứ
        if (req.getNgayLamViec().isBefore(LocalDate.now())) {
            throw new RuntimeException("Không thể phân lịch cho ngày trong quá khứ!");
        }


        // Validation 2: Check trùng giờ (Truyền null vì là tạo mới chưa có ID)
        validateConflict(null, req.getIdNhanVien(), req.getIdCa(), req.getNgayLamViec());


        // Lấy thông tin reference
        NhanVien nv = nvRepo.findById(req.getIdNhanVien())
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại (ID: " + req.getIdNhanVien() + ")"));


        CaLamViec ca = caRepo.findById(req.getIdCa())
                .orElseThrow(() -> new RuntimeException("Ca làm việc không tồn tại (ID: " + req.getIdCa() + ")"));


        // Map dữ liệu
        LichLamViec lich = new LichLamViec();
        lich.setNhanVien(nv);
        lich.setCaLamViec(ca);
        lich.setNgayLamViec(req.getNgayLamViec());
        lich.setGhiChu(req.getGhiChu());
        lich.setTrangThai(req.getTrangThai() != null ? req.getTrangThai() : 1); // 1: Công khai


        return lichRepo.save(lich);
    }


    // 3. CẬP NHẬT LỊCH (UPDATE)
    public LichLamViec updateSchedule(Integer id, LichLamViecRequest req) {
        LichLamViec lich = lichRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch làm việc không tồn tại với ID: " + id));


        // Validation 1: Chặn sửa lịch quá khứ (Bảo toàn dữ liệu chấm công)
        if (lich.getNgayLamViec().isBefore(LocalDate.now())) {
            throw new RuntimeException("Lịch này thuộc về quá khứ, không thể chỉnh sửa!");
        }
        // Cũng không được đổi ngày sang quá khứ
        if (req.getNgayLamViec().isBefore(LocalDate.now())) {
            throw new RuntimeException("Không thể dời lịch sang ngày trong quá khứ!");
        }


        // Validation 2: Check trùng giờ (Truyền ID hiện tại để bỏ qua chính nó khi check)
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


    // 4. XÓA LỊCH (DELETE)
    public void deleteSchedule(Integer id) {
        LichLamViec lich = lichRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch làm việc không tồn tại"));


        // Validation: Chặn xóa lịch quá khứ
        if (lich.getNgayLamViec().isBefore(LocalDate.now())) {
            throw new RuntimeException("Không thể xóa lịch sử làm việc đã qua (Dữ liệu này cần để tính lương)!");
        }


        lichRepo.delete(lich);
    }


    // ================= PRIVATE HELPER METHODS =================


    /**
     * Hàm check trùng giờ làm việc (Overlap Logic)
     * @param currentId: ID của lịch đang sửa (null nếu là tạo mới). Dùng để bỏ qua chính nó.
     * @param idNv: ID Nhân viên
     * @param idCa: ID Ca làm việc MỚI muốn gán
     * @param ngay: Ngày làm việc
     */
    private void validateConflict(Integer currentId, Integer idNv, Integer idCa, LocalDate ngay) {
        // 1. Lấy thông tin Ca Mới
        CaLamViec caMoi = caRepo.findById(idCa)
                .orElseThrow(() -> new RuntimeException("Ca làm việc không tồn tại"));


        // 2. Lấy tất cả lịch đã có của nhân viên trong ngày đó
        List<LichLamViec> existingSchedules = lichRepo.findByNhanVienAndNgay(idNv, ngay);


        for (LichLamViec oldSch : existingSchedules) {
            // Nếu là Update -> Bỏ qua chính bản ghi đang sửa
            if (currentId != null && oldSch.getId().equals(currentId)) {
                continue;
            }


            // Nếu lịch cũ đã bị ẩn/hủy (Trạng thái 0) thì có thể bỏ qua (Tùy nghiệp vụ, ở đây tôi đang check cả)
            if (oldSch.getTrangThai() == 0) continue;


            CaLamViec caCu = oldSch.getCaLamViec();


            // 3. Công thức kiểm tra trùng giờ (Overlap): (StartA < EndB) && (EndA > StartB)
            // Ví dụ: Ca cũ (08:00 - 12:00). Ca mới (10:00 - 14:00)
            // 10:00 < 12:00 (True) AND 14:00 > 08:00 (True) -> TRÙNG
            boolean isOverlap = caMoi.getGioBatDau().isBefore(caCu.getGioKetThuc())
                    && caMoi.getGioKetThuc().isAfter(caCu.getGioBatDau());


            if (isOverlap) {
                // Ném lỗi có từ khóa "trùng" để Frontend bắt được và hiện đỏ
                throw new RuntimeException("Nhân viên này đã có lịch trùng giờ ("
                        + caCu.getTenCa() + ": " + caCu.getGioBatDau() + "-" + caCu.getGioKetThuc()
                        + ") trong ngày " + ngay);
            }
        }
    }
}


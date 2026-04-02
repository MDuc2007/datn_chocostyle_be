package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.CaLamViec;
import org.example.chocostyle_datn.model.Request.CaLamViecRequest;
import org.example.chocostyle_datn.repository.CaLamViecRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class CaLamViecService {

    @Autowired
    private CaLamViecRepository repo;

    // 1. Lấy danh sách ca làm việc
    public List<CaLamViec> getAll() {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "idCa"));
    }

    // 2. Lấy chi tiết ca
    public CaLamViec getById(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca làm việc với ID: " + id));
    }

    // === HÀM BẢO VỆ DỮ LIỆU & LOGIC NGHIỆP VỤ ===
    private void validateLogic(CaLamViecRequest req, Integer idCa) {
        // 1. Cắt khoảng trắng thừa
        String tenCaClean = req.getTenCa().trim();
        req.setTenCa(tenCaClean);

        // 2. CHẶN TUYỆT ĐỐI: Giờ bắt đầu phải diễn ra trước giờ kết thúc
        if (!req.getGioBatDau().isBefore(req.getGioKetThuc())) {
            throw new RuntimeException("Giờ bắt đầu phải diễn ra trước giờ kết thúc!");
        }

        // 3. Thời gian ca làm việc tối thiểu phải là 30 phút
        long minutes = java.time.Duration.between(req.getGioBatDau(), req.getGioKetThuc()).toMinutes();
        if (minutes < 30) {
            throw new RuntimeException("Thời gian ca làm việc không hợp lệ (Tối thiểu phải là 30 phút)!");
        }

        // 4. Kiểm tra trùng lặp Tên Ca
        if (idCa == null) { // Nếu là Thêm mới
            if (repo.existsByTenCa(tenCaClean)) {
                throw new RuntimeException("Tên ca làm việc '" + tenCaClean + "' đã tồn tại trong hệ thống!");
            }
        } else { // Nếu là Cập nhật
            if (repo.existsByTenCaAndIdCaNot(tenCaClean, idCa)) {
                throw new RuntimeException("Tên ca làm việc '" + tenCaClean + "' đã tồn tại trong hệ thống!");
            }
        }

        // 5. Kiểm tra trùng lặp KHUNG GIỜ
        List<CaLamViec> allShifts = repo.findAll();
        for (CaLamViec ca : allShifts) {
            if (idCa != null && ca.getIdCa().equals(idCa)) continue;

            if (ca.getGioBatDau().equals(req.getGioBatDau()) && ca.getGioKetThuc().equals(req.getGioKetThuc())) {
                throw new RuntimeException("Khung giờ này đã bị trùng y hệt với ca: " + ca.getTenCa());
            }
        }
    }
    // 3. Tạo mới ca làm việc (Đổi tham số sang Request)
    public CaLamViec create(CaLamViecRequest request) {
        // Kiểm tra toàn bộ logic trước khi lưu
        validateLogic(request, null);

        CaLamViec ca = new CaLamViec();
        ca.setMaCa(generateNextMaCa());
        ca.setTenCa(request.getTenCa());
        ca.setGioBatDau(request.getGioBatDau());
        ca.setGioKetThuc(request.getGioKetThuc());
        ca.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : 1); // Mặc định hoạt động
        ca.setNgayTao(LocalDateTime.now());

        return repo.save(ca);
    }

    // 4. Cập nhật ca làm việc (Đổi tham số sang Request)
    public CaLamViec update(Integer id, CaLamViecRequest request) {
        // Kiểm tra toàn bộ logic trước khi cập nhật
        validateLogic(request, id);

        CaLamViec ca = getById(id);
        ca.setTenCa(request.getTenCa());
        ca.setGioBatDau(request.getGioBatDau());
        ca.setGioKetThuc(request.getGioKetThuc());
        if (request.getTrangThai() != null) {
            ca.setTrangThai(request.getTrangThai());
        }
        ca.setNgayCapNhat(LocalDateTime.now());

        return repo.save(ca);
    }

    // 5. Thay đổi trạng thái (Xóa mềm)
    public void delete(Integer id) {
        CaLamViec ca = getById(id);
        ca.setTrangThai(0); // Chuyển sang ngưng hoạt động
        repo.save(ca);
    }

    // Hàm tự động tạo mã ca: CA001, CA002...
    private String generateNextMaCa() {
        long count = repo.count();
        return String.format("CA%03d", count + 1);
    }

    // BỔ SUNG THAM SỐ KEYWORD
    public Page<CaLamViec> searchCaLamViec(String keyword, Integer trangThai, LocalTime gioBatDau, LocalTime gioKetThuc, int page, int size) {
        // Lưu ý: Dùng Native Query nên phải sort theo đúng tên cột trong DB là "id_ca"
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id_ca"));

        // Đổi LocalTime sang String ("HH:mm:ss") để lừa JDBC Driver
        String startStr = (gioBatDau != null) ? gioBatDau.toString() : null;
        String endStr = (gioKetThuc != null) ? gioKetThuc.toString() : null;

        // Truyền thêm keyword xuống repo
        return repo.searchCaLamViec(keyword, trangThai, startStr, endStr, pageable);
    }
}
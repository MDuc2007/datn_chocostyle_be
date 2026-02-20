package org.example.chocostyle_datn.service;
import org.example.chocostyle_datn.entity.CaLamViec;
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


    // 3. Tạo mới ca làm việc
    public CaLamViec create(CaLamViec ca) {
        // Tự động gen mã ca: CA + số lượng hiện tại + 1
        ca.setMaCa(generateNextMaCa());
        ca.setTrangThai(1); // Mặc định hoạt động
        ca.setNgayTao(LocalDateTime.now());
        // Check trùng tên
        if (repo.existsByTenCa(ca.getTenCa())) {
            throw new RuntimeException("Tên ca làm việc '" + ca.getTenCa() + "' đã tồn tại trong hệ thống!");
        }
        // Kiểm tra logic giờ giấc
        if (ca.getGioBatDau().isAfter(ca.getGioKetThuc())) {
            throw new RuntimeException("Giờ bắt đầu không thể sau giờ kết thúc");
        }


        return repo.save(ca);
    }


    // 4. Cập nhật ca làm việc
    public CaLamViec update(Integer id, CaLamViec caDetails) {
        CaLamViec ca = getById(id);


        ca.setTenCa(caDetails.getTenCa());
        ca.setGioBatDau(caDetails.getGioBatDau());
        ca.setGioKetThuc(caDetails.getGioKetThuc());
        ca.setTrangThai(caDetails.getTrangThai());
        ca.setNgayCapNhat(LocalDateTime.now());
        // Check trùng tên
        if (repo.existsByTenCaAndIdCaNot(caDetails.getTenCa(), id)) {
            throw new RuntimeException("Tên ca làm việc '" + ca.getTenCa() + "' đã tồn tại trong hệ thống!");
        }
        if (ca.getGioBatDau().isAfter(ca.getGioKetThuc())) {
            throw new RuntimeException("Giờ bắt đầu không thể sau giờ kết thúc");
        }


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
    // BỔ SUNG XỬ LÝ NULL ĐỂ FIX LỖI ÉP KIỂU CỦA SQL SERVER
    public Page<CaLamViec> searchCaLamViec(Integer trangThai, LocalTime gioBatDau, LocalTime gioKetThuc, int page, int size) {
        // Lưu ý: Dùng Native Query nên phải sort theo đúng tên cột trong DB là "id_ca"
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id_ca"));

        // Đổi LocalTime sang String ("HH:mm:ss") để lừa JDBC Driver
        String startStr = (gioBatDau != null) ? gioBatDau.toString() : null;
        String endStr = (gioKetThuc != null) ? gioKetThuc.toString() : null;

        return repo.searchCaLamViec(trangThai, startStr, endStr, pageable);
    }
}


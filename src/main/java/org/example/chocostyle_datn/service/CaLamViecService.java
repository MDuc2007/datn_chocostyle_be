package org.example.chocostyle_datn.service;
import org.example.chocostyle_datn.entity.CaLamViec;
import org.example.chocostyle_datn.repository.CaLamViecRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
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
}


package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.ChamCong;
import org.example.chocostyle_datn.entity.LichLamViec;
import org.example.chocostyle_datn.entity.NhanVien;
import org.example.chocostyle_datn.repository.ChamCongRepository;
import org.example.chocostyle_datn.repository.LichLamViecRepository;
import org.example.chocostyle_datn.repository.NhanVienRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ChamCongService {

    private final ChamCongRepository chamCongRepository;
    private final LichLamViecRepository lichLamViecRepository;
    private final NhanVienRepository nhanVienRepository;

    public ChamCongService(ChamCongRepository chamCongRepository,
                           LichLamViecRepository lichLamViecRepository,
                           NhanVienRepository nhanVienRepository) {
        this.chamCongRepository = chamCongRepository;
        this.lichLamViecRepository = lichLamViecRepository;
        this.nhanVienRepository = nhanVienRepository;
    }

    // 🔎 Kiểm tra đã check-in chưa
    public boolean daCheckIn(Integer idNv, LocalDate ngay) {
        return chamCongRepository
                .findByNhanVien_IdAndNgay(idNv, ngay)
                .isPresent();
    }

    // 🚀 CHECK-IN
    public ChamCong checkIn(Integer idNv) {

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 1️⃣ Kiểm tra có ca hôm nay không
        List<LichLamViec> lich =
                lichLamViecRepository.checkCaHomNay(idNv, today);

        if (lich.isEmpty()) {
            throw new RuntimeException("Hôm nay bạn không có ca làm!");
        }

        // 2️⃣ Kiểm tra đã check-in chưa
        if (daCheckIn(idNv, today)) {
            throw new RuntimeException("Bạn đã check-in rồi!");
        }

        // 3️⃣ Lấy nhân viên
        NhanVien nv = nhanVienRepository.findById(idNv)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        // 4️⃣ Tạo bản ghi chấm công
        ChamCong chamCong = new ChamCong();
        chamCong.setNhanVien(nv);
        chamCong.setNgay(today);
        chamCong.setGioCheckIn(now);
        chamCong.setTrangThai(1);

        return chamCongRepository.save(chamCong);
    }

    public ChamCong checkOut(Integer idNv) {

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 1️⃣ Tìm bản ghi hôm nay
        ChamCong chamCong = chamCongRepository
                .findByNhanVien_IdAndNgay(idNv, today)
                .orElseThrow(() -> new RuntimeException("Bạn chưa check-in hôm nay!"));

        // 2️⃣ Kiểm tra đã check-out chưa
        if (chamCong.getGioCheckOut() != null) {
            throw new RuntimeException("Bạn đã check-out rồi!");
        }

        // 3️⃣ Cập nhật giờ ra
        chamCong.setGioCheckOut(now);
        chamCong.setTrangThai(3);

        return chamCongRepository.save(chamCong);
    }
}
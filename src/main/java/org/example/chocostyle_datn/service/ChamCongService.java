package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.ChamCong;
import org.example.chocostyle_datn.entity.LichLamViec;
import org.example.chocostyle_datn.entity.NhanVien;
import org.example.chocostyle_datn.model.Response.GiaoCaResponse;
import org.example.chocostyle_datn.repository.ChamCongRepository;
import org.example.chocostyle_datn.repository.LichLamViecRepository;
import org.example.chocostyle_datn.repository.NhanVienRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public ChamCong checkIn(Integer idNv,Double tienMatDauCa, Double tienCkDauCa) {

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 1️⃣ Kiểm tra có ca hôm nay không
        List<LichLamViec> lich =
                lichLamViecRepository.checkCaHomNay(idNv, today);

        if (lich.isEmpty()) {
            throw new RuntimeException("Hôm nay bạn không có ca làm!");
        }

        LichLamViec ca = lich.get(0);

        LocalTime gioBatDau = ca.getCaLamViec().getGioBatDau();
        LocalTime gioKetThuc = ca.getCaLamViec().getGioKetThuc();
        LocalTime gioMoCaSom = gioBatDau.minusMinutes(30); // Cho phép check-in sớm 30p

        // 2️⃣ VÀ 3️⃣: KIỂM TRA GIỜ CHECK-IN CHO CẢ CA NGÀY VÀ ĐÊM
        boolean isThoiGianHopLe = false;

        if (gioBatDau.isBefore(gioKetThuc)) {
            // Ca ban ngày
            isThoiGianHopLe = now.isAfter(gioMoCaSom) && now.isBefore(gioKetThuc);
        } else {
            // Ca qua đêm
            isThoiGianHopLe = now.isAfter(gioMoCaSom) || now.isBefore(gioKetThuc);
        }
        if (!isThoiGianHopLe) {
            throw new RuntimeException("Hiện tại không nằm trong thời gian cho phép vào ca!");
        }
        // 4️⃣ Kiểm tra đã check-in chưa
        if (daCheckIn(idNv, today)) {
            throw new RuntimeException("Bạn đã check-in rồi!");
        }

        // 5️⃣ Lấy nhân viên
        NhanVien nv = nhanVienRepository.findById(idNv)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        // 6️⃣ Tạo bản ghi chấm công
        ChamCong chamCong = new ChamCong();
        chamCong.setNhanVien(nv);
        chamCong.setNgay(today);
        chamCong.setGioCheckIn(now);
        chamCong.setTrangThai(3);
        chamCong.setTienMatDauCa(tienMatDauCa);
        chamCong.setTienChuyenKhoanDauCa(tienCkDauCa);
        // Lưu chấm công
        ChamCong savedChamCong = chamCongRepository.save(chamCong);

        // 👉 ĐỒNG BỘ LỊCH LÀM VIỆC (Đổi trạng thái lịch thành Đang làm = 3)
        LichLamViec caHienTai = lich.get(0);
        caHienTai.setTrangThai(3);
        lichLamViecRepository.save(caHienTai);

        return savedChamCong;
    }

    // Sửa lại hàm checkOut để nhận thêm tiền
    public ChamCong checkOut(Integer idNv, Double tienMat, Double tienChuyenKhoan, String ghiChu) {

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

        // 3️⃣ Lấy ca làm hôm nay (Sử dụng hàm mới để lấy lịch bỏ qua trạng thái)
        List<LichLamViec> tatCaLichHomNay = lichLamViecRepository.findByNhanVien_IdAndNgayLamViec(idNv, today);
        LichLamViec ca = null;

        // Tìm xem trong ngày hôm nay có cái lịch nào đang ở trạng thái 3 (Đang làm) không
        for (LichLamViec l : tatCaLichHomNay) {
            if (l.getTrangThai() == 3) {
                ca = l;
                break;
            }
        }

        if (ca == null) {
            throw new RuntimeException("Không tìm thấy ca làm đang mở của bạn hôm nay!");
        }

        // 4️⃣ CHẶN CHECK-OUT SỚM
        if (now.isBefore(ca.getCaLamViec().getGioKetThuc())) {
            throw new RuntimeException("Chưa đến giờ kết thúc ca!");
        }

        if (chamCong.getTrangThai() != 3) {
            throw new RuntimeException("Ca làm việc này chưa được mở hoặc đã kết thúc!");
        }

        // 5️⃣ Cập nhật giờ ra và số tiền kết toán
        chamCong.setGioCheckOut(now);
        chamCong.setTrangThai(1);
        chamCong.setTienMatCuoiCa(tienMat);
        chamCong.setTienChuyenKhoanCuoiCa(tienChuyenKhoan);
        chamCong.setGhiChu(ghiChu);
        // 2. TÍNH DOANH THU TÁCH BIỆT (Tiền mặt & Chuyển khoản)
        java.time.LocalDateTime startDateTime = java.time.LocalDateTime.of(chamCong.getNgay(), chamCong.getGioCheckIn());
        java.time.LocalDateTime endDateTime = java.time.LocalDateTime.now();

        Double dtTienMat = chamCongRepository.calculateDoanhThuTienMat(idNv, startDateTime, endDateTime);
        Double dtChuyenKhoan = chamCongRepository.calculateDoanhThuChuyenKhoan(idNv, startDateTime, endDateTime);

        chamCong.setDoanhThuTienMat(dtTienMat);
        chamCong.setDoanhThuCk(dtChuyenKhoan);
        chamCong.setTongDoanhThu(dtTienMat + dtChuyenKhoan); // Tổng doanh thu bằng 2 túi cộng lại

        // 3. TÍNH CHÊNH LỆCH CHO TỪNG TÚI TIỀN
        Double dauCaMat = chamCong.getTienMatDauCa() != null ? chamCong.getTienMatDauCa() : 0.0;
        Double dauCaCk = chamCong.getTienChuyenKhoanDauCa() != null ? chamCong.getTienChuyenKhoanDauCa() : 0.0;

        // Công thức: Chênh lệch = Thực tế nhập vào - (Đầu ca + Doanh thu)
        Double chenhLechMat = tienMat - (dauCaMat + dtTienMat);
        Double chenhLechCk = tienChuyenKhoan - (dauCaCk + dtChuyenKhoan);

        chamCong.setChenhLechTienMat(chenhLechMat);
        chamCong.setChenhLechCk(chenhLechCk);
        chamCong.setTienChenhLech(chenhLechMat + chenhLechCk); // Vẫn lưu tổng chênh lệch để dễ nhìn lướt
        // Lưu chấm công
        ChamCong savedChamCong = chamCongRepository.save(chamCong);

        // 👉 ĐỒNG BỘ LỊCH LÀM VIỆC (Đóng lịch lại thành 1 = Đã hoàn thành)
        ca.setTrangThai(1);
        lichLamViecRepository.save(ca);

        return savedChamCong;
    }
    // HÀM LẤY DANH SÁCH GIAO CA (Đã fix lỗi chuỗi rỗng)
    public List<GiaoCaResponse> getDanhSachGiaoCa(String keyword, String fromDate, String toDate) {

        // 1. Ép các chuỗi rỗng ("") thành null để SQL Server không bị lỗi ép kiểu
        String kw = (keyword != null && !keyword.trim().isEmpty()) ? keyword : null;
        String fd = (fromDate != null && !fromDate.trim().isEmpty()) ? fromDate : null;
        String td = (toDate != null && !toDate.trim().isEmpty()) ? toDate : null;

        // 2. Truyền các biến đã xử lý xuống Repository
        List<Map<String, Object>> results = chamCongRepository.getDanhSachGiaoCa(kw, fd, td);

        List<GiaoCaResponse> responses = new ArrayList<>();

        for (Map<String, Object> row : results) {
            GiaoCaResponse dto = new GiaoCaResponse();
            dto.setId((Integer) row.get("id"));
            dto.setNhanVien((String) row.get("nhanVien"));
            dto.setCa((String) row.get("ca"));

            String ngay = (String) row.get("ngayStr");
            String timeIn = (String) row.get("gioCheckInStr");
            String timeOut = (String) row.get("gioCheckOutStr");

            dto.setThoiGianMo(timeIn + " " + ngay);
            dto.setThoiGianDong(timeOut != null ? (timeOut + " " + ngay) : "-");

            dto.setTrangThai((Integer) row.get("trangThai"));

            Double tienMat = Double.valueOf(row.get("tienMat").toString());
            Double tienCk = Double.valueOf(row.get("tienChuyenKhoan").toString());
            Double doanhThu = Double.valueOf(row.get("tongDoanhThu").toString());

            dto.setTienMat(tienMat);
            dto.setTienChuyenKhoan(tienCk);
//            dto.setTongDoanhThu(doanhThu);
            dto.setTienMatDauCa(Double.valueOf(row.get("tienMatDauCa").toString()));
            dto.setTienChuyenKhoanDauCa(Double.valueOf(row.get("tienChuyenKhoanDauCa").toString()));
            dto.setTongDoanhThu(Double.valueOf(row.get("tongDoanhThu").toString()));
            dto.setTienChenhLech(Double.valueOf(row.get("tienChenhLech").toString()));
            dto.setGhiChu((String) row.get("ghiChu"));
            if (dto.getTrangThai() == 3) {
                dto.setTienChenh((tienMat + tienCk) - doanhThu);
            } else {
                dto.setTienChenh(0.0);
            }

            responses.add(dto);
        }
        return responses;
    }
}
package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.ChamCong;
import org.example.chocostyle_datn.entity.LichLamViec;
import org.example.chocostyle_datn.entity.NhanVien;
import org.example.chocostyle_datn.model.Response.GiaoCaResponse;
import org.example.chocostyle_datn.repository.ChamCongRepository;
import org.example.chocostyle_datn.repository.LichLamViecRepository;
import org.example.chocostyle_datn.repository.NhanVienRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    // 🔎 Hàm Lấy ca thông minh (Đã chuẩn hóa theo: 1=Đóng, 2=Chờ, 3=Đang làm)
    public ChamCong getChamCongHomNay(Integer idNv) {
        LocalDate today = LocalDate.now();
        List<ChamCong> list = chamCongRepository.findDanhSachChamCongHomNay(idNv, today);

        // 1. NẾU CÓ PHIẾU ĐANG LÀM DỞ: Ưu tiên trả về để nhân viên tiếp tục bán hàng
        if (list != null && !list.isEmpty()) {
            for (ChamCong cc : list) {
                if (cc.getGioCheckOut() == null) {
                    return cc; // Trả về ca có trạng thái 3 (Đang làm)
                }
            }
        }

        // 2. KIỂM TRA LỊCH LÀM VIỆC: Xem Quản lý có phân ca mới không?
        List<LichLamViec> lichs = lichLamViecRepository.checkCaHomNay(idNv, today);
        if (!lichs.isEmpty()) {
            LichLamViec caHienTai = lichs.get(0);

            // 👉 ĐÂY LÀ CHÌA KHÓA: Nếu lịch đang ở trạng thái 2 (ĐANG MỞ / CHỜ LÀM)
            if (caHienTai.getTrangThai() == 2) {
                // Có ca mới tinh chưa Check-in! Trả về rỗng để Frontend mở form Nhập tiền
                return null;
            }
        }

        // 3. NẾU KHÔNG CÓ CA MỚI CHỜ LÀM: Trả về phiếu chấm công vừa đóng gần nhất
        // (Để hiển thị giao diện Tổng kết Ca đã đóng và 2 nút Chỉ xem / Đăng xuất)
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }

        return null;
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
        // 4️⃣ Kiểm tra xem có ca nào đang làm dở chưa đóng không
        ChamCong caDangMo = getChamCongHomNay(idNv);
        if (caDangMo != null) {
            throw new RuntimeException("Bạn đang có một ca chưa kết thúc. Vui lòng đóng ca cũ trước khi vào ca mới!");
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

        // 1️⃣ & 2️⃣ Lấy đúng cái ca ĐANG MỞ của ngày hôm nay ra để đóng
        ChamCong chamCong = getChamCongHomNay(idNv);
        if (chamCong == null) {
            throw new RuntimeException("Bạn chưa check-in hoặc không có ca nào đang mở!");
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

        /* * 4️⃣ ĐÃ ẨN KIỂM TRA CHẶN CHECK-OUT SỚM THEO YÊU CẦU
         * Cho phép nhân viên chốt ca bất cứ lúc nào, dù chưa đến giờ kết thúc ca.
         */
        // if (now.isBefore(ca.getCaLamViec().getGioKetThuc())) {
        //     throw new RuntimeException("Chưa đến giờ kết thúc ca!");
        // }

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

        Integer soHd = chamCongRepository.countHoaDonTrongCa(idNv, startDateTime, endDateTime);
        chamCong.setSoLuongHoaDon(soHd);


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
            dto.setDoanhThuTienMat(Double.valueOf(row.get("doanhThuTienMat").toString()));
            dto.setDoanhThuCk(Double.valueOf(row.get("doanhThuCk").toString()));
            dto.setChenhLechTienMat(Double.valueOf(row.get("chenhLechTienMat").toString()));
            dto.setChenhLechCk(Double.valueOf(row.get("chenhLechCk").toString()));
            dto.setSoLuongHoaDon(row.get("soLuongHoaDon") != null ? Integer.valueOf(row.get("soLuongHoaDon").toString()) : 0);
            if (dto.getTrangThai() == 3) {
                dto.setTienChenh((tienMat + tienCk) - doanhThu);
            } else {
                dto.setTienChenh(0.0);
            }

            responses.add(dto);
        }
        return responses;
    }
    public Map<String, Double> laySoDuCaTruoc() {
        ChamCong caTruoc = chamCongRepository.layCaDongGanNhat();
        Double tienMat = (caTruoc != null && caTruoc.getTienMatCuoiCa() != null) ? caTruoc.getTienMatCuoiCa() : 0.0;
        Double tienCk = (caTruoc != null && caTruoc.getTienChuyenKhoanCuoiCa() != null) ? caTruoc.getTienChuyenKhoanCuoiCa() : 0.0;
        return Map.of("tienMat", tienMat, "tienCk", tienCk);
    }
    // 🤖 HÀM CHẠY NGẦM: TỰ ĐỘNG ĐÓNG CA QUÁ HẠN 30 PHÚT
    @Scheduled(fixedRate = 60000) // Chạy lặp lại mỗi 60 giây
    public void tuDongDongCaQuaHan() {
        // 1. Lấy tất cả các ca chưa check-out
        List<ChamCong> cacCaDangMo = chamCongRepository.findTatCaCaDangMo();
        LocalDateTime now = LocalDateTime.now();

        for (ChamCong cc : cacCaDangMo) {
            // 2. Tìm lịch làm việc tương ứng với ca này
            List<LichLamViec> lichs = lichLamViecRepository.findByNhanVien_IdAndNgayLamViec(cc.getNhanVien().getId(), cc.getNgay());
            LichLamViec caHienTai = null;

            for (LichLamViec l : lichs) {
                if (l.getTrangThai() == 3) { // 3 = Đang làm
                    caHienTai = l;
                    break;
                }
            }

            if (caHienTai != null) {
                LocalTime gioKetThuc = caHienTai.getCaLamViec().getGioKetThuc();
                LocalDateTime thoiGianKetThucCa = LocalDateTime.of(cc.getNgay(), gioKetThuc);

                // Xử lý logic nếu là ca đêm (qua 12h đêm)
                if (gioKetThuc.isBefore(caHienTai.getCaLamViec().getGioBatDau())) {
                    thoiGianKetThucCa = thoiGianKetThucCa.plusDays(1);
                }

                // 3. NẾU HIỆN TẠI ĐÃ VƯỢT QUÁ GIỜ KẾT THÚC 30 PHÚT
                if (now.isAfter(thoiGianKetThucCa.plusMinutes(30))) {

                    // Tự động set giờ ra và trạng thái
                    cc.setGioCheckOut(now.toLocalTime());
                    cc.setTrangThai(1); // 1 = Đã đóng

                    // Vì nhân viên quên chốt, hệ thống tự điền két thực tế = 0
                    cc.setTienMatCuoiCa(0.0);
                    cc.setTienChuyenKhoanCuoiCa(0.0);
                    cc.setGhiChu("Hệ thống tự động đóng ca do nhân viên quên Check-out quá 30 phút.");

                    // Tính toán doanh thu như bình thường
                    LocalDateTime startDateTime = LocalDateTime.of(cc.getNgay(), cc.getGioCheckIn());
                    Double dtTienMat = chamCongRepository.calculateDoanhThuTienMat(cc.getNhanVien().getId(), startDateTime, now);
                    Double dtChuyenKhoan = chamCongRepository.calculateDoanhThuChuyenKhoan(cc.getNhanVien().getId(), startDateTime, now);

                    Integer soHd = chamCongRepository.countHoaDonTrongCa(cc.getNhanVien().getId(), startDateTime, now);
                    cc.setSoLuongHoaDon(soHd);

                    cc.setDoanhThuTienMat(dtTienMat);
                    cc.setDoanhThuCk(dtChuyenKhoan);
                    cc.setTongDoanhThu(dtTienMat + dtChuyenKhoan);

                    // Tính chênh lệch (Vì két thực tế = 0 nên độ lệch sẽ bị ÂM, Quản lý nhìn vào sẽ biết ngay)
                    Double dauCaMat = cc.getTienMatDauCa() != null ? cc.getTienMatDauCa() : 0.0;
                    Double dauCaCk = cc.getTienChuyenKhoanDauCa() != null ? cc.getTienChuyenKhoanDauCa() : 0.0;

                    cc.setChenhLechTienMat(0.0 - (dauCaMat + dtTienMat));
                    cc.setChenhLechCk(0.0 - (dauCaCk + dtChuyenKhoan));
                    cc.setTienChenhLech(cc.getChenhLechTienMat() + cc.getChenhLechCk());

                    // Lưu phiếu chấm công
                    chamCongRepository.save(cc);

                    // Cập nhật lại lịch làm việc
                    caHienTai.setTrangThai(1);
                    lichLamViecRepository.save(caHienTai);

                    System.out.println("⚠️ Đã tự động đóng ca cho nhân viên ID: " + cc.getNhanVien().getId());
                }
            }
        }
    }
}
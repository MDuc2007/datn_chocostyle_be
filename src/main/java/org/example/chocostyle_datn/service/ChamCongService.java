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

    // 🔎 Hàm Lấy ca thông minh (Đã nâng cấp: Dùng chung két tiền)
    public ChamCong getChamCongHomNay(Integer idNv) {
        LocalDate today = LocalDate.now();

        // 1. KIỂM TRA TOÀN BỘ CỬA HÀNG ĐÃ CÓ AI MỞ CA CHƯA?
        List<ChamCong> caDangMoList = chamCongRepository.findCaDangMoCuaCuaHang(today);
        if (!caDangMoList.isEmpty()) {
            // 👉 NẾU CÓ NGƯỜI MỞ RỒI (Vd NV 1 mở) -> TRẢ VỀ LUÔN PHIẾU ĐÓ CHO NV 2
            // Để Frontend của NV 2 thấy là đã mở ca và ẩn form nhập tiền đi
            return caDangMoList.get(0);
        }

        // 2. NẾU CHƯA AI MỞ CA: Kiểm tra xem NV này có được phân ca hôm nay không?
        List<LichLamViec> lichs = lichLamViecRepository.checkCaHomNay(idNv, today);
        if (!lichs.isEmpty()) {
            LichLamViec caHienTai = lichs.get(0);
            // 👉 Nếu lịch đang ở trạng thái 2 (Chờ làm) -> Bật form nhập tiền mở ca
            if (caHienTai.getTrangThai() == 2) {
                return null;
            }
        }

        // 3. Nếu không có ca chờ mở, trả về ca đóng gần nhất (để xem sao kê)
        List<ChamCong> list = chamCongRepository.findDanhSachChamCongHomNay(idNv, today);
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }

        return null;
    }

    // 🚀 CHECK-IN (MỞ CA)
    public ChamCong checkIn(Integer idNv, Double tienMatDauCa, Double tienCkDauCa) {

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 1️⃣ Kiểm tra có ca hôm nay không
        List<LichLamViec> lich = lichLamViecRepository.checkCaHomNay(idNv, today);

        if (lich.isEmpty()) {
            throw new RuntimeException("Hôm nay bạn không có ca làm!");
        }

        LichLamViec ca = lich.get(0);

        LocalTime gioBatDau = ca.getCaLamViec().getGioBatDau();
        LocalTime gioKetThuc = ca.getCaLamViec().getGioKetThuc();
        LocalTime gioMoCaSom = gioBatDau.minusMinutes(30); // Cho phép check-in sớm 30p

        // 2️⃣ Kiểm tra giờ
        boolean isThoiGianHopLe = false;
        if (gioBatDau.isBefore(gioKetThuc)) {
            isThoiGianHopLe = now.isAfter(gioMoCaSom) && now.isBefore(gioKetThuc);
        } else {
            isThoiGianHopLe = now.isAfter(gioMoCaSom) || now.isBefore(gioKetThuc);
        }
        if (!isThoiGianHopLe) {
            throw new RuntimeException("Hiện tại không nằm trong thời gian cho phép vào ca!");
        }

        // 3️⃣ Kiểm tra xem có ca nào đang mở không (chống mở đúp)
        List<ChamCong> caDangMoList = chamCongRepository.findCaDangMoCuaCuaHang(today);
        if (!caDangMoList.isEmpty()) {
            throw new RuntimeException("Cửa hàng đã có người mở ca rồi, bạn không cần mở lại!");
        }

        NhanVien nv = nhanVienRepository.findById(idNv)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        // 4️⃣ Tạo bản ghi chấm công
        ChamCong chamCong = new ChamCong();
        chamCong.setNhanVien(nv);
        chamCong.setNgay(today);
        chamCong.setGioCheckIn(now);
        chamCong.setTrangThai(3);
        chamCong.setTienMatDauCa(tienMatDauCa);
        chamCong.setTienChuyenKhoanDauCa(tienCkDauCa);
        chamCong.setTenNguoiMoCa(nv.getHoTen());

        ChamCong savedChamCong = chamCongRepository.save(chamCong);

        // Đổi trạng thái lịch thành Đang làm = 3
        LichLamViec caHienTai = lich.get(0);
        caHienTai.setTrangThai(3);
        lichLamViecRepository.save(caHienTai);

        return savedChamCong;
    }

    // 🚀 CHECK-OUT (ĐÓNG CA & GỘP TIỀN TOÀN CỬA HÀNG)
    public ChamCong checkOut(Integer idNv, Double tienMat, Double tienChuyenKhoan, String ghiChu) {

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 1️⃣ Lấy đúng ca ĐANG MỞ của cửa hàng ra để đóng
        ChamCong chamCong = getChamCongHomNay(idNv);
        if (chamCong == null || chamCong.getTrangThai() != 3) {
            throw new RuntimeException("Cửa hàng hiện không có ca nào đang mở để kết thúc!");
        }

        NhanVien nvDongCa = nhanVienRepository.findById(idNv)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        // 2️⃣ Cập nhật giờ ra và số tiền kết toán
        chamCong.setGioCheckOut(now);
        chamCong.setTrangThai(1);
        chamCong.setTienMatCuoiCa(tienMat);
        chamCong.setTienChuyenKhoanCuoiCa(tienChuyenKhoan);
        chamCong.setGhiChu(ghiChu);
        chamCong.setTenNguoiDongCa(nvDongCa.getHoTen()); // Lưu người chốt ca

        // 3️⃣ TÍNH DOANH THU CHUNG CỦA TOÀN BỘ NHÂN VIÊN TRONG CA
        java.time.LocalDateTime startDateTime = java.time.LocalDateTime.of(chamCong.getNgay(), chamCong.getGioCheckIn());
        java.time.LocalDateTime endDateTime = java.time.LocalDateTime.now();

        Double dtTienMat = chamCongRepository.calculateDoanhThuTienMatChung(startDateTime, endDateTime);
        Double dtChuyenKhoan = chamCongRepository.calculateDoanhThuChuyenKhoanChung(startDateTime, endDateTime);
        Integer soHd = chamCongRepository.countHoaDonTrongCaChung(startDateTime, endDateTime);

        chamCong.setSoLuongHoaDon(soHd);
        chamCong.setDoanhThuTienMat(dtTienMat);
        chamCong.setDoanhThuCk(dtChuyenKhoan);
        chamCong.setTongDoanhThu(dtTienMat + dtChuyenKhoan);

        // 4️⃣ TÍNH CHÊNH LỆCH KÉT
        Double dauCaMat = chamCong.getTienMatDauCa() != null ? chamCong.getTienMatDauCa() : 0.0;
        Double dauCaCk = chamCong.getTienChuyenKhoanDauCa() != null ? chamCong.getTienChuyenKhoanDauCa() : 0.0;

        Double chenhLechMat = tienMat - (dauCaMat + dtTienMat);
        Double chenhLechCk = tienChuyenKhoan - (dauCaCk + dtChuyenKhoan);

        chamCong.setChenhLechTienMat(chenhLechMat);
        chamCong.setChenhLechCk(chenhLechCk);
        chamCong.setTienChenhLech(chenhLechMat + chenhLechCk);

        ChamCong savedChamCong = chamCongRepository.save(chamCong);

        // 5️⃣ ĐÓNG LỊCH LÀM VIỆC CỦA NGƯỜI MỞ CA
        List<LichLamViec> lichNguoiMo = lichLamViecRepository.findByNhanVien_IdAndNgayLamViec(chamCong.getNhanVien().getId(), today);
        for (LichLamViec l : lichNguoiMo) {
            if (l.getTrangThai() == 3 || l.getTrangThai() == 2) {
                l.setTrangThai(1); // 1 = Đã hoàn thành
                lichLamViecRepository.save(l);
            }
        }

        // 6️⃣ ĐÓNG LUÔN LỊCH LÀM VIỆC CỦA NGƯỜI ĐÓNG CA (Nếu người đóng khác người mở)
        if (!chamCong.getNhanVien().getId().equals(idNv)) {
            List<LichLamViec> lichNguoiDong = lichLamViecRepository.findByNhanVien_IdAndNgayLamViec(idNv, today);
            for (LichLamViec l : lichNguoiDong) {
                if (l.getTrangThai() == 3 || l.getTrangThai() == 2) {
                    l.setTrangThai(1);
                    lichLamViecRepository.save(l);
                }
            }
        }

        return savedChamCong;
    }

    // HÀM LẤY DANH SÁCH GIAO CA
    public List<GiaoCaResponse> getDanhSachGiaoCa(String keyword, String fromDate, String toDate) {

        // 👉 MẸO FIX LỖI 400 TỪ SQL SERVER: Không dùng null nữa!
        String kw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : "";
        String fd = (fromDate != null && !fromDate.trim().isEmpty()) ? fromDate.trim() : "1900-01-01";
        String td = (toDate != null && !toDate.trim().isEmpty()) ? toDate.trim() : "2100-01-01";

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
            dto.setTienMat(Double.valueOf(row.get("tienMat").toString()));
            dto.setTienChuyenKhoan(Double.valueOf(row.get("tienChuyenKhoan").toString()));
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

            // DỮ LIỆU TÊN NGƯỜI ĐÓNG MỞ
            dto.setNguoiMoCa((String) row.get("tenNguoiMoCa"));
            dto.setNguoiDongCa((String) row.get("tenNguoiDongCa"));

            Double doanhThu = Double.valueOf(row.get("tongDoanhThu").toString());
            Double tienMat = dto.getTienMat();
            Double tienCk = dto.getTienChuyenKhoan();

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

    // 🤖 HÀM CHẠY NGẦM: TỰ ĐỘNG ĐÓNG CA QUÁ HẠN
    @Scheduled(fixedRate = 60000)
    public void tuDongDongCaQuaHan() {
        List<ChamCong> cacCaDangMo = chamCongRepository.findTatCaCaDangMo();
        LocalDateTime now = LocalDateTime.now();

        for (ChamCong cc : cacCaDangMo) {
            List<LichLamViec> lichs = lichLamViecRepository.findByNhanVien_IdAndNgayLamViec(cc.getNhanVien().getId(), cc.getNgay());
            LichLamViec caHienTai = null;

            for (LichLamViec l : lichs) {
                if (l.getTrangThai() == 3) {
                    caHienTai = l;
                    break;
                }
            }

            if (caHienTai != null) {
                LocalTime gioKetThuc = caHienTai.getCaLamViec().getGioKetThuc();
                LocalDateTime thoiGianKetThucCa = LocalDateTime.of(cc.getNgay(), gioKetThuc);

                if (gioKetThuc.isBefore(caHienTai.getCaLamViec().getGioBatDau())) {
                    thoiGianKetThucCa = thoiGianKetThucCa.plusDays(1);
                }

                if (now.isAfter(thoiGianKetThucCa.plusMinutes(30))) {
                    cc.setGioCheckOut(now.toLocalTime());
                    cc.setTrangThai(1);
                    cc.setTienMatCuoiCa(0.0);
                    cc.setTienChuyenKhoanCuoiCa(0.0);
                    cc.setGhiChu("Hệ thống tự động đóng ca do nhân viên quên Check-out quá 30 phút.");
                    cc.setTenNguoiDongCa("Hệ thống tự động");

                    LocalDateTime startDateTime = LocalDateTime.of(cc.getNgay(), cc.getGioCheckIn());

                    // 👉 TỰ ĐỘNG ĐÓNG CA CŨNG GỘP DOANH THU CHUNG
                    Double dtTienMat = chamCongRepository.calculateDoanhThuTienMatChung(startDateTime, now);
                    Double dtChuyenKhoan = chamCongRepository.calculateDoanhThuChuyenKhoanChung(startDateTime, now);
                    Integer soHd = chamCongRepository.countHoaDonTrongCaChung(startDateTime, now);

                    cc.setSoLuongHoaDon(soHd);
                    cc.setDoanhThuTienMat(dtTienMat);
                    cc.setDoanhThuCk(dtChuyenKhoan);
                    cc.setTongDoanhThu(dtTienMat + dtChuyenKhoan);

                    Double dauCaMat = cc.getTienMatDauCa() != null ? cc.getTienMatDauCa() : 0.0;
                    Double dauCaCk = cc.getTienChuyenKhoanDauCa() != null ? cc.getTienChuyenKhoanDauCa() : 0.0;

                    cc.setChenhLechTienMat(0.0 - (dauCaMat + dtTienMat));
                    cc.setChenhLechCk(0.0 - (dauCaCk + dtChuyenKhoan));
                    cc.setTienChenhLech(cc.getChenhLechTienMat() + cc.getChenhLechCk());

                    chamCongRepository.save(cc);

                    caHienTai.setTrangThai(1);
                    lichLamViecRepository.save(caHienTai);
                }
            }
        }
    }
}
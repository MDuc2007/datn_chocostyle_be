package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.ChamCong;
import org.example.chocostyle_datn.entity.LichLamViec;
import org.example.chocostyle_datn.entity.NhanVien;
import org.example.chocostyle_datn.model.Response.GiaoCaResponse;
import org.example.chocostyle_datn.repository.ChamCongRepository;
import org.example.chocostyle_datn.repository.LichLamViecRepository;
import org.example.chocostyle_datn.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChamCongService {
    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

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
    // 🔎 Hàm Lấy ca thông minh (Đã nâng cấp: Dùng chung két tiền + Hỗ trợ nhiều ca 1 ngày)
    public ChamCong getChamCongHomNay(Integer idNv) {
        LocalDate today = LocalDate.now();

        // 1. KIỂM TRA TOÀN BỘ CỬA HÀNG ĐÃ CÓ AI MỞ CA CHƯA?
        List<ChamCong> caDangMoList = chamCongRepository.findCaDangMoCuaCuaHang(today);
        if (!caDangMoList.isEmpty()) {
            return caDangMoList.get(0);
        }

        // 2. NẾU CHƯA AI MỞ CA: Kiểm tra xem NV này có được phân ca hôm nay không?
        List<LichLamViec> lichs = lichLamViecRepository.checkCaHomNay(idNv, today);
        if (!lichs.isEmpty()) {
            LichLamViec caHienTai = null;
            // 👉 LỌC CA THÔNG MINH: Ưu tiên tìm ca Đang làm (3) hoặc Chờ làm (2)
            for (LichLamViec l : lichs) {
                if (l.getTrangThai() == 3 || l.getTrangThai() == 2) {
                    caHienTai = l;
                    break;
                }
            }
            // Nếu không tìm thấy, lấy ca cuối cùng trong ngày
            if (caHienTai == null) caHienTai = lichs.get(lichs.size() - 1);

            // Nếu lịch đang ở trạng thái 2 (Chờ làm) -> Bật form nhập tiền mở ca
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
    // 🚀 CHECK-IN (MỞ CA)
    public ChamCong checkIn(Integer idNv, Double tienMatDauCa, Double tienCkDauCa) {

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 1️⃣ Kiểm tra có ca hôm nay không
        List<LichLamViec> lich = lichLamViecRepository.checkCaHomNay(idNv, today);

        if (lich.isEmpty()) {
            throw new RuntimeException("Hôm nay bạn không có ca làm!");
        }

        // 👉 LỌC CA THÔNG MINH V2: Ưu tiên bốc đúng ca đang trong khung giờ
        LichLamViec caHienTai = null;

        // Ưu tiên 1: Tìm xem có ca nào ĐANG NẰM TRONG GIỜ được phép check-in không (trước 30p)
        for (LichLamViec l : lich) {
            if (l.getTrangThai() == 2) {
                LocalTime start = l.getCaLamViec().getGioBatDau().minusMinutes(30);
                LocalTime end = l.getCaLamViec().getGioKetThuc();

                // Nếu giờ hiện tại lọt vào khoảng (Bắt đầu - 30p) đến (Kết thúc)
                if (!now.isBefore(start) && !now.isAfter(end)) {
                    caHienTai = l;
                    break; // Tìm thấy ca chuẩn thì dừng vòng lặp luôn
                }
            }
        }

        // Ưu tiên 2: Nếu không có ca nào khớp giờ hiện tại, lấy đại ca "Chờ làm" tiếp theo để ném ra thông báo lỗi
        if (caHienTai == null) {
            for (LichLamViec l : lich) {
                if (l.getTrangThai() == 2) {
                    caHienTai = l;
                    break;
                }
            }
        }

        if (caHienTai == null) {
            throw new RuntimeException("Bạn không có ca nào đang chờ mở lúc này!");
        }

        LocalTime gioBatDau = caHienTai.getCaLamViec().getGioBatDau();
        LocalTime gioKetThuc = caHienTai.getCaLamViec().getGioKetThuc();

        // (Phần check isAfter, isBefore bên dưới của bạn giữ nguyên không cần đổi)

        // 2️⃣ KIỂM TRA GIỜ (Đã bỏ logic ca đêm, chia thông báo rõ ràng)
        // 2.1 - Nếu thời gian hiện tại đã vượt qua giờ kết thúc ca
        if (now.isAfter(gioKetThuc)) {
            throw new RuntimeException("Ca làm việc đã kết thúc vào lúc " + gioKetThuc + ". Không thể mở ca!");
        }

        // 2.2 - Nếu vào quá sớm (chưa tới ngưỡng 30 phút trước khi bắt đầu)
        LocalTime gioMoCaSom = gioBatDau.minusMinutes(30); // Cho phép check-in sớm 30p
        if (now.isBefore(gioMoCaSom)) {
            throw new RuntimeException("Chưa đến ca! Bạn chỉ được mở ca trước giờ bắt đầu (" + gioBatDau + ") tối đa 30 phút.");
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
        chamCong.setTenNguoiMoCa("[" + nv.getMaNv() + "] " + nv.getHoTen());

        ChamCong savedChamCong = chamCongRepository.save(chamCong);

        // Đổi trạng thái lịch thành Đang làm = 3
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
        chamCong.setTenNguoiDongCa("[" + nvDongCa.getMaNv() + "] " + nvDongCa.getHoTen());

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
    public Map<String, Object> getDanhSachGiaoCa(String keyword, String fromDate, String toDate, int page, int size) {

        // 👉 MẸO FIX LỖI 400 TỪ SQL SERVER: Không dùng null nữa!
        String kw = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : "";
        String fd = (fromDate != null && !fromDate.trim().isEmpty()) ? fromDate.trim() : "1900-01-01";
        String td = (toDate != null && !toDate.trim().isEmpty()) ? toDate.trim() : "2100-01-01";

        // Khởi tạo đối tượng phân trang
        Pageable pageable = PageRequest.of(page, size);

        // Gọi hàm từ repository đã được sửa thành trả về Page
        Page<Map<String, Object>> pageResult = chamCongRepository.getDanhSachGiaoCa(kw, fd, td, pageable);
        List<GiaoCaResponse> responses = new ArrayList<>();

        // Thay results bằng pageResult.getContent() để lấy data của trang hiện tại
        for (Map<String, Object> row : pageResult.getContent()) {
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

        // Đóng gói danh sách GiaoCaResponse và các thông số phân trang vào Map để trả về Controller
        Map<String, Object> response = new HashMap<>();
        response.put("content", responses);
        response.put("currentPage", pageResult.getNumber());
        response.put("totalItems", pageResult.getTotalElements());
        response.put("totalPages", pageResult.getTotalPages());

        return response;
    }

    public Map<String, Double> laySoDuCaTruoc() {
        ChamCong caTruoc = chamCongRepository.layCaDongGanNhat();
        Double tienMat = (caTruoc != null && caTruoc.getTienMatCuoiCa() != null) ? caTruoc.getTienMatCuoiCa() : 0.0;
        Double tienCk = (caTruoc != null && caTruoc.getTienChuyenKhoanCuoiCa() != null) ? caTruoc.getTienChuyenKhoanCuoiCa() : 0.0;
        return Map.of("tienMat", tienMat, "tienCk", tienCk);
    }

    // 🤖 HÀM CHẠY NGẦM: TỰ ĐỘNG ĐÓNG CA QUÁ HẠN
    @Scheduled(fixedRate = 10000)
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
    // ⚡ HÀM CẬP NHẬT DOANH THU REAL-TIME TRONG CA
    // 🚀 HÀM CẬP NHẬT DOANH THU REAL-TIME (DÙNG CHUNG CHO TOÀN CỬA HÀNG)
    @Transactional
    public void capNhatDoanhThuCaHienTai(Integer idNv) {
        LocalDate today = LocalDate.now();

        // 1. Lấy ra Ca đang mở của TẤT CẢ mọi người (Vì cửa hàng dùng chung 1 ca)
        List<ChamCong> caDangMoList = chamCongRepository.findCaDangMoCuaCuaHang(today);
        if (caDangMoList.isEmpty()) {
            return; // Nếu không có ca nào đang mở thì không tính toán gì cả
        }

        ChamCong cc = caDangMoList.get(0); // Lấy ca đang mở ra

        if (cc.getGioCheckIn() != null && cc.getGioCheckOut() == null) {
            LocalDateTime startDateTime = LocalDateTime.of(cc.getNgay(), cc.getGioCheckIn());
            LocalDateTime now = LocalDateTime.now();

            // 👉 2. SỬA QUAN TRỌNG: Gọi hàm TÍNH CHUNG (Chung chữ "Chung" ở cuối hàm, KHÔNG truyền idNv vào)
            Double dtTienMat = chamCongRepository.calculateDoanhThuTienMatChung(startDateTime, now);
            Double dtChuyenKhoan = chamCongRepository.calculateDoanhThuChuyenKhoanChung(startDateTime, now);
            Integer soHd = chamCongRepository.countHoaDonTrongCaChung(startDateTime, now);

            // Cập nhật doanh thu tổng vào DB
            cc.setDoanhThuTienMat(dtTienMat);
            cc.setDoanhThuCk(dtChuyenKhoan);
            cc.setTongDoanhThu(dtTienMat + dtChuyenKhoan);
            cc.setSoLuongHoaDon(soHd);

            // Tính chênh lệch tạm thời (Đầu ca + Bán được - Thực tế)
            // Lúc này chưa đóng ca (thực tế = 0) nên nó sẽ tạm âm
            Double dauCaMat = cc.getTienMatDauCa() != null ? cc.getTienMatDauCa() : 0.0;
            Double dauCaCk = cc.getTienChuyenKhoanDauCa() != null ? cc.getTienChuyenKhoanDauCa() : 0.0;

            cc.setChenhLechTienMat(0.0 - (dauCaMat + dtTienMat));
            cc.setChenhLechCk(0.0 - (dauCaCk + dtChuyenKhoan));
            cc.setTienChenhLech(cc.getChenhLechTienMat() + cc.getChenhLechCk());

            chamCongRepository.save(cc);

            // Phát sóng Websocket để cập nhật màn hình giao ca
            try {
                messagingTemplate.convertAndSend("/topic/shift-updates", "UPDATED");
            } catch (Exception e) {
                System.out.println("Lỗi Websocket Giao ca: " + e.getMessage());
            }
        }
    }
}
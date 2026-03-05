package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.ChiTietSanPham;
import org.example.chocostyle_datn.entity.HoaDon;
import org.example.chocostyle_datn.entity.PhieuGiamGia;
import org.example.chocostyle_datn.repository.ChiTietSanPhamRepository;
import org.example.chocostyle_datn.repository.HoaDonRepository;
import org.example.chocostyle_datn.repository.PhieuGiamGiaRepository;
import org.example.chocostyle_datn.repository.SanPhamRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;

@Service
public class ChatAIService {

    private final ChiTietSanPhamRepository chiTietRepo;
    private final PhieuGiamGiaRepository voucherRepo;
    private final HoaDonRepository hoaDonRepo;

    public ChatAIService(
            ChiTietSanPhamRepository chiTietRepo,
            PhieuGiamGiaRepository voucherRepo,
            HoaDonRepository hoaDonRepo
    ) {
        this.chiTietRepo = chiTietRepo;
        this.voucherRepo = voucherRepo;
        this.hoaDonRepo = hoaDonRepo;
    }

    private final NumberFormat vnFormat =
            NumberFormat.getInstance(new Locale("vi", "VN"));

    private final Random random = new Random();

    private boolean containsAny(String msg, String... keywords) {
        for (String k : keywords) {
            if (msg.contains(k)) {
                return true;
            }
        }
        return false;
    }

    private String randomIntro() {
        String[] list = {
                "Shop xin thông tin đến bạn:",
                "Dạ mình tham khảo thông tin sau nhé:",
                "Hiện tại bên shop có thông tin như sau:",
                "Em gửi anh/chị thông tin ạ:"
        };
        return list[random.nextInt(list.length)];
    }

    public String chat(String message) {

        String msg = message.toLowerCase().trim();

        String size = extractSize(msg);
        Long maxPrice = extractMaxPrice(msg);

        if (size != null || maxPrice != null) {
            return handleDynamicFilter(size, maxPrice);
        }

        if (msg.contains("dưới") && maxPrice != null) {
            return handlePriceRange(0L, maxPrice);
        }

        if (msg.contains("trên") && maxPrice != null) {
            return handlePriceRange(maxPrice, Long.MAX_VALUE);
        }

        // ===== GIÁ =====
        if (containsAny(msg,
                "giá", "bao nhiêu tiền", "bao nhiêu", "giá tiền", "price", "tiền")) {
            return handlePrice();
        }

        // ===== TỒN KHO / SIZE =====
        if (containsAny(msg,
                "còn hàng", "còn không", "hết hàng", "size", "còn size",
                "còn sản phẩm", "còn áo", "tồn kho")) {
            return handleStock();
        }

        // ===== VOUCHER =====
        if (containsAny(msg,
                "voucher", "giảm giá", "khuyến mãi", "ưu đãi",
                "sale", "mã giảm giá", "chương trình")) {
            return handleVoucher();
        }

        // ===== ĐƠN HÀNG =====
        if (containsAny(msg,
                "đơn hàng", "hóa đơn", "hd", "mã đơn", "kiểm tra đơn",
                "trạng thái đơn")) {
            return handleOrder(msg);
        }

        // ===== SẢN PHẨM =====
        if (containsAny(msg,
                "áo", "sản phẩm", "mặt hàng", "shop có gì",
                "bán gì", "có gì", "xem hàng", "xem sản phẩm",
                "loại áo", "mẫu áo")) {
            return handleProduct();
        }

        if (msg.contains("top") || msg.contains("gợi ý") || msg.contains("bán chạy")) {
            return handleTopProducts();
        }

        return "Shop chuyên áo khoác nam. Bạn có thể hỏi về sản phẩm, giá bán, voucher hoặc đơn hàng nhé.";
    }

    private String handleDynamicFilter(String size, Long maxPrice) {

        BigDecimal min = BigDecimal.ZERO;
        BigDecimal max = maxPrice != null ?
                BigDecimal.valueOf(maxPrice) :
                null;

        Integer kichCoId = null;

        if (size != null) {
            // bạn cần query tìm id kích cỡ theo tên
            // hoặc hardcode nếu biết id
        }

        Page<ChiTietSanPham> page = chiTietRepo.filter(
                null,
                null,
                null,
                kichCoId,
                1,
                min,
                max,
                PageRequest.of(0, 3)
        );

        List<ChiTietSanPham> list = page.getContent();

        if (list.isEmpty()) {
            return "Không tìm thấy sản phẩm phù hợp yêu cầu ạ.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Sản phẩm phù hợp yêu cầu của bạn:\n\n");

        for (ChiTietSanPham sp : list) {
            sb.append("• ")
                    .append(sp.getIdSanPham().getTenSp())
                    .append(" - Size ")
                    .append(sp.getIdKichCo().getTenKichCo())
                    .append(" - ")
                    .append(vnFormat.format(sp.getGiaBan()))
                    .append("đ\n");
        }

        return sb.toString();
    }

    private String extractSize(String msg) {
        msg = msg.toUpperCase();

        if (msg.contains(" SIZE S") || msg.endsWith("S") || msg.contains(" S ")) {
            return "S";
        }
        if (msg.contains(" SIZE M") || msg.endsWith("M") || msg.contains(" M ")) {
            return "M";
        }
        if (msg.contains(" SIZE L") || msg.endsWith("L") || msg.contains(" L ")) {
            return "L";
        }

        return null;
    }

    private String handleSize(String size) {

        List<ChiTietSanPham> list = chiTietRepo.getAllActive();

        List<ChiTietSanPham> filtered = list.stream()
                .filter(sp ->
                        sp.getIdKichCo().getTenKichCo()
                                .equalsIgnoreCase(size)
                )
                .toList();

        if (filtered.isEmpty()) {
            return "Hiện tại shop chưa có size " + size + " ạ.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("👕 Các mẫu size ").append(size).append(":\n\n");

        for (ChiTietSanPham sp : filtered.stream().limit(3).toList()) {
            sb.append("• ")
                    .append(sp.getIdSanPham().getTenSp())
                    .append(" - ")
                    .append(vnFormat.format(sp.getGiaBan()))
                    .append("đ (Còn ")
                    .append(sp.getSoLuongTon())
                    .append(")\n");
        }

        return sb.toString();
    }

    private Long extractMaxPrice(String msg) {

        msg = msg.replace("k", "000");

        String[] words = msg.split(" ");

        for (String w : words) {
            if (w.matches("\\d+")) {
                return Long.parseLong(w);
            }
        }
        return null;
    }

    private String handlePriceRange(Long min, Long max) {

        Page<ChiTietSanPham> page = chiTietRepo.filter(
                null,
                null,
                null,
                null,
                1,
                BigDecimal.valueOf(min),
                BigDecimal.valueOf(max),
                PageRequest.of(0, 3)
        );

        List<ChiTietSanPham> filtered = page.getContent();

        if (filtered.isEmpty()) {
            return "Không có sản phẩm trong khoảng giá này ạ.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(" Sản phẩm trong tầm giá:\n\n");

        for (ChiTietSanPham sp : filtered) {
            sb.append("• ")
                    .append(sp.getIdSanPham().getTenSp())
                    .append(" - Size ")
                    .append(sp.getIdKichCo().getTenKichCo())
                    .append(" - ")
                    .append(vnFormat.format(sp.getGiaBan()))
                    .append("đ\n");
        }

        return sb.toString();
    }

    private String handlePrice() {

        List<ChiTietSanPham> list = chiTietRepo.getAllActive();

        if (list.isEmpty()) {
            return "Hiện tại shop chưa có sản phẩm đang kinh doanh ạ.";
        }

        BigDecimal min = list.stream()
                .map(ChiTietSanPham::getGiaBan)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal max = list.stream()
                .map(ChiTietSanPham::getGiaBan)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        return randomIntro()
                + "\nGiá áo khoác hiện dao động từ "
                + vnFormat.format(min) + "đ đến "
                + vnFormat.format(max) + "đ.";
    }

    private String handleStock() {

        List<ChiTietSanPham> list = chiTietRepo.getAllActive();

        if (list.isEmpty()) {
            return "Hiện tại sản phẩm đang tạm hết hàng ạ.";
        }

        ChiTietSanPham sp = list.get(random.nextInt(list.size()));

        if (sp.getSoLuongTon() <= 0) {
            return "Sản phẩm hiện đã hết hàng ạ.";
        }

        return "Dạ mẫu "
                + sp.getIdSanPham().getTenSp()
                + " - Size " + sp.getIdKichCo().getTenKichCo()
                + " hiện còn "
                + sp.getSoLuongTon()
                + " sản phẩm trong kho.";
    }

    private String handleVoucher() {

        List<PhieuGiamGia> list = voucherRepo.findActiveVoucher();

        if (list.isEmpty()) {
            return "Hiện tại shop chưa có chương trình khuyến mãi nào ạ.";
        }

        PhieuGiamGia p = list.get(random.nextInt(list.size()));

        return randomIntro()
                + "\n Chương trình: " + p.getTenPgg()
                + "\n Giá trị giảm: " + p.getGiaTri()
                + "\n\nAnh/chị cần em hướng dẫn cách áp dụng không ạ?";
    }

    private String handleOrder(String msg) {

        String[] words = msg.split(" ");

        for (String word : words) {
            if (word.toUpperCase().startsWith("HD")) {

                Optional<HoaDon> hoaDon =
                        hoaDonRepo.findByMaHoaDon(word.toUpperCase());

                if (hoaDon.isPresent()) {

                    return " Đơn hàng "
                            + word.toUpperCase()
                            + " hiện đang ở trạng thái: "
                            + hoaDon.get().getTrangThai()
                            + ".\n"
                            + "Anh/chị cần hỗ trợ thêm thông tin gì không ạ?";
                }
            }
        }

        return "Anh/chị vui lòng cung cấp mã đơn hàng (VD: HD01) để em kiểm tra giúp ạ.";
    }

    private String handleProduct() {

        List<ChiTietSanPham> list = chiTietRepo.getAllActive();

        if (list.isEmpty()) {
            return "Hiện tại shop đang hết hàng ạ.";
        }

        ChiTietSanPham sp = list.get(random.nextInt(list.size()));

        return randomIntro()
                + "\n " + sp.getIdSanPham().getTenSp()
                + "\n Màu: " + sp.getIdMauSac().getTenMauSac()
                + "\n Size: " + sp.getIdKichCo().getTenKichCo()
                + "\n Giá: " + vnFormat.format(sp.getGiaBan()) + "đ"
                + "\n Còn: " + sp.getSoLuongTon();
    }

    private String handleTopProducts() {

        List<ChiTietSanPham> list = chiTietRepo.getAllActive();

        if (list.isEmpty()) {
            return "Hiện tại chưa có sản phẩm ạ.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("⭐ Một số mẫu nổi bật:\n\n");

        for (ChiTietSanPham sp : list.stream().limit(3).toList()) {
            sb.append("• ")
                    .append(sp.getIdSanPham().getTenSp())
                    .append(" - Size ")
                    .append(sp.getIdKichCo().getTenKichCo())
                    .append(" - ")
                    .append(vnFormat.format(sp.getGiaBan()))
                    .append("đ\n");
        }

        return sb.toString();
    }
}
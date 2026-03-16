//package org.example.chocostyle_datn.service;
//
//import org.example.chocostyle_datn.entity.ChiTietSanPham;
//import org.example.chocostyle_datn.entity.HoaDon;
//import org.example.chocostyle_datn.entity.PhieuGiamGia;
//import org.example.chocostyle_datn.model.Response.SanPhamBanChayResponse;
//import org.example.chocostyle_datn.repository.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Optional;
//import java.text.NumberFormat;
//import java.util.Locale;
//import java.util.Random;
//
//@Service
//public class ChatAIService {
//    @Autowired
//    private ThongKeRepository thongKeRepository;
//    @Autowired
//    private ChiTietDotGiamGiaRepository chiTietDggRepo;
//    private final ChiTietSanPhamRepository chiTietRepo;
//    private final PhieuGiamGiaRepository voucherRepo;
//    private final HoaDonRepository hoaDonRepo;
//    private Map<Integer, Double> heightMap = new ConcurrentHashMap<>();
//    private Map<Integer, Integer> weightMap = new ConcurrentHashMap<>();
//
//    public ChatAIService(ChiTietSanPhamRepository chiTietRepo, PhieuGiamGiaRepository voucherRepo, HoaDonRepository hoaDonRepo) {
//        this.chiTietRepo = chiTietRepo;
//        this.voucherRepo = voucherRepo;
//        this.hoaDonRepo = hoaDonRepo;
//    }
//
//    private final NumberFormat vnFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
//
//    private final Random random = new Random();
//
//    private boolean containsAny(String msg, String... keywords) {
//        for (String k : keywords) {
//            if (msg.contains(k)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private String randomIntro() {
//        String[] list = {"Shop xin thông tin đến bạn:", "Dạ mình tham khảo thông tin sau nhé:", "Hiện tại bên shop có thông tin như sau:", "Em gửi anh/chị thông tin ạ:"};
//        return list[random.nextInt(list.length)];
//    }
//
//    public String chat(String message, Integer conversationId) {
//
//        String msg = normalize(message);
//
//        // ===== Lấy dữ liệu từ tin nhắn =====
//        Double height = extractHeight(msg);
//        Integer weight = extractWeight(msg);
//
//        if (height != null) {
//            heightMap.put(conversationId, height);
//        }
//
//        if (weight != null) {
//            weightMap.put(conversationId, weight);
//        }
//
//        Double savedHeight = heightMap.get(conversationId);
//        Integer savedWeight = weightMap.get(conversationId);
//
//        // ===== Hỏi thêm cân nặng =====
//        if (savedHeight != null && savedWeight == null) {
//            return "Anh/chị nặng khoảng bao nhiêu kg để shop tư vấn size chuẩn hơn ạ?";
//        }
//
//        // ===== Hỏi thêm chiều cao =====
//        if (savedHeight == null && savedWeight != null) {
//            return "Anh/chị cao khoảng bao nhiêu mét để shop tư vấn size chuẩn hơn ạ?";
//        }
//
//        // ===== Nếu đủ dữ liệu thì tư vấn size =====
//        if (savedHeight != null && savedWeight != null) {
//
//            String result = suggestSize(savedHeight, savedWeight);
//
//            // reset sau khi tư vấn
//            heightMap.remove(conversationId);
//            weightMap.remove(conversationId);
//
//            return result;
//        }
//
//        // ===== Logic tìm sản phẩm =====
//        String size = extractSize(msg);
//        int[] color = extractColor(msg);
//        Long price = extractMaxPrice(msg);
//        String name = extractProductName(msg);
//
//
//        // ===== Voucher =====
//        if (containsAny(msg, "voucher", "ma giam gia", "ma giam", "code giam", "code", "phieu giam gia", "uu dai", "khuyen mai")) {
//            return handleVoucher();
//        }
//
//// ===== Sản phẩm đang sale mạnh =====
//        if (containsAny(msg, "sale", "dang sale", "giam gia", "giam sau", "giam manh", "sp giam", "re", "re khong", "uu dai", "su kien", "sk", "hot", "hay")) {
//            return handleSaleProducts();
//        }
//
//        // ===== Kiểm tra đơn =====
//        if (containsAny(msg, "don hang", "don", "hoa don", "hd", "giao den dau", "den dau roi", "tinh trang don", "kiem tra don")) {
//            return handleOrder(msg);
//        }
//
//        // ===== Gợi ý sản phẩm =====
//        if (containsAny(msg, "goi y", "ban chay", "top")) {
//            return handleTopProducts();
//        }
//// ===== Hỏi giá sản phẩm =====
//        // ===== Hỏi giá sản phẩm =====
//        if (containsAny(msg, "gia", "bao nhieu", "bao tien", "price")) {
//
//            if (name != null) {
//                return handleProductPrice(name);
//            } else {
//                return "Anh/chị muốn hỏi giá sản phẩm nào ạ? Ví dụ: giá áo bomber hoặc giá hoodie.";
//            }
//        }
//        if (size != null || color != null || price != null || name != null) {
//            return searchProduct(size, color, price, name);
//        }
//        // ===== Fallback =====
//        return "Shop có áo khoác nam nhiều mẫu. Bạn có thể hỏi ví dụ:\n" + "• áo khoác xanh size m\n" + "• áo bomber đen dưới 500k\n" + "• shop có voucher không\n" + "• cao 1m7 mặc size gì";
//    }
//
//    private String handleProductPrice(String msg) {
//
//        List<ChiTietSanPham> list = chiTietRepo.getAllActive();
//
//        List<ChiTietSanPham> result = list.stream()
//                .filter(sp -> normalize(msg)
//                        .contains(normalize(sp.getIdSanPham().getTenSp())))
//                .limit(5)
//                .toList();
//
//        if (result.isEmpty()) {
//            return "Shop chưa tìm thấy sản phẩm bạn hỏi.";
//        }
//
//        StringBuilder sb = new StringBuilder();
//
//        sb.append("Thông tin sản phẩm:\n\n");
//
//        for (ChiTietSanPham sp : result) {
//            sb.append("• ")
//                    .append(sp.getIdSanPham().getTenSp())
//                    .append(" | ")
//                    .append(sp.getIdMauSac().getTenMauSac())
//                    .append(" | Size ")
//                    .append(sp.getIdKichCo().getTenKichCo())
//                    .append(" | ")
//                    .append(vnFormat.format(sp.getGiaBan()))
//                    .append("đ\n");
//        }
//
//        return sb.toString();
//    }
//
//    private String suggestSize(double height, int weight) {
//
//        String size;
//
//        if (height < 1.60) {
//            size = "S";
//        } else if (height < 1.72) {
//            size = weight < 65 ? "M" : "L";
//        } else {
//            size = weight < 75 ? "L" : "XL";
//        }
//
//        return "Với chiều cao " + height + "m và " + weight + "kg, shop gợi ý anh/chị mặc size " + size + " ạ.";
//    }
//
//    private Integer extractWeight(String msg) {
//
//        msg = msg.replace("kg", "").replace("ky", "").replace("kí", "");
//
//        String[] words = msg.split(" ");
//
//        for (String w : words) {
//            if (w.matches("\\d{2,3}")) {
//                int weight = Integer.parseInt(w);
//
//                if (weight >= 35 && weight <= 150) {
//                    return weight;
//                }
//            }
//        }
//
//        return null;
//    }
//
//    private Double extractHeight(String msg) {
//
//        msg = msg.replace("met", "m");
//
//        // case: m7 -> 1.7
//        if (msg.matches(".*m\\d{1,2}.*")) {
//            for (String w : msg.split(" ")) {
//                if (w.matches("m\\d{1,2}")) {
//                    String num = w.substring(1);
//                    return Double.parseDouble("1." + num);
//                }
//            }
//        }
//
//        // case: 1m7
//        if (msg.matches(".*1m\\d{1,2}.*")) {
//            for (String w : msg.split(" ")) {
//                if (w.matches("1m\\d{1,2}")) {
//                    String num = w.substring(2);
//                    return Double.parseDouble("1." + num);
//                }
//            }
//        }
//
//        // case: 170
//        if (msg.matches(".*1\\d{2}.*")) {
//            for (String w : msg.split(" ")) {
//                if (w.matches("1\\d{2}")) {
//                    return Integer.parseInt(w) / 100.0;
//                }
//            }
//        }
//
//        return null;
//    }
//
//    private int[] extractColor(String msg) {
//
//        msg = normalize(msg);
//
//        // đỏ
//        if (msg.contains("do") || msg.contains("red")) return new int[]{255, 0, 0};
//
//        // đen
//        if (msg.contains("den") || msg.contains("black")) return new int[]{0, 0, 0};
//
//        // trắng
//        if (msg.contains("trang") || msg.contains("white")) return new int[]{255, 255, 255};
//
//        // vàng
//        if (msg.contains("vang") || msg.contains("yellow")) return new int[]{255, 255, 0};
//
//        // xanh lá
//        if (msg.contains("xanh la") || msg.contains("la")) return new int[]{0, 128, 0};
//
//        // xanh dương
//        if (msg.contains("xanh duong") || msg.contains("blue") || msg.contains("xanh da troi"))
//            return new int[]{0, 0, 255};
//
//        // xanh chung
//        if (msg.contains("xanh")) return new int[]{0, 120, 200};
//
//        return null;
//    }
//
//    private int[] hexToRgb(String hex) {
//
//        try {
//
//            if (hex == null) return null;
//
//            hex = hex.replace("#", "").trim();
//
//            if (hex.length() != 6) return null;
//
//            int r = Integer.parseInt(hex.substring(0, 2), 16);
//            int g = Integer.parseInt(hex.substring(2, 4), 16);
//            int b = Integer.parseInt(hex.substring(4, 6), 16);
//
//            return new int[]{r, g, b};
//
//        } catch (Exception e) {
//            return null;
//        }
//    }
//
//    private double colorDistance(int[] c1, int[] c2) {
//
//        if (c1 == null || c2 == null) return Double.MAX_VALUE;
//
//        int r = c1[0] - c2[0];
//        int g = c1[1] - c2[1];
//        int b = c1[2] - c2[2];
//
//        return Math.sqrt(r * r + g * g + b * b);
//    }
//
//    private String extractProductName(String msg) {
//        if (msg.contains("bomber")) return "bomber";
//        if (msg.contains("hoodie")) return "hoodie";
//        if (msg.contains("gio")) return "gio";
//        if (msg.contains("du")) return "du";
//        if (msg.contains("denim") || msg.contains("jean")) return "denim";
//        if (msg.contains("ni")) return "ni";
//        if (msg.contains("phao")) return "phao";
//        if (msg.contains("cardigan")) return "cardigan";
//        if (msg.contains("blazer")) return "blazer";
//        if (msg.contains("varsity")) return "varsity";
//        if (msg.contains("khoac")) return "khoac";
//        return null;
//    }
//
//    private int levenshtein(String a, String b) {
//
//        int[][] dp = new int[a.length() + 1][b.length() + 1];
//
//        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
//        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
//
//        for (int i = 1; i <= a.length(); i++) {
//            for (int j = 1; j <= b.length(); j++) {
//
//                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
//
//                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
//            }
//        }
//
//        return dp[a.length()][b.length()];
//    }
//
//    private String searchProduct(String size, int[] color, Long price, String name) {
//
//        List<ChiTietSanPham> list = chiTietRepo.getAllActive();
//
//        List<ChiTietSanPham> result = list.stream()
//
//                .filter(sp -> {
//
//                    boolean ok = true;
//
//                    if (size != null) {
//                        ok &= sp.getIdKichCo().getTenKichCo().equalsIgnoreCase(size);
//                    }
//
//                    if (color != null) {
//
//                        try {
//
//                            String rgbString = sp.getIdMauSac().getRgb();
//
//                            if (rgbString != null && rgbString.contains(",")) {
//
//                                String[] parts = rgbString.split(",");
//
//                                int[] rgbProduct = new int[]{Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim())};
//
//                                double distance = colorDistance(rgbProduct, color);
//
//                                ok &= distance < 150;
//
//                            }
//
//                        } catch (Exception e) {
//                            ok &= false;
//                        }
//                    }
//
//                    if (name != null) {
//
//                        String ten = normalize(sp.getIdSanPham().getTenSp());
//
//                        if (!ten.contains(name)) {
//
//                            int distance = levenshtein(ten, name);
//
//                            ok &= distance <= 3;
//                        }
//                    }
//
//                    if (price != null) {
//                        ok &= sp.getGiaBan().compareTo(BigDecimal.valueOf(price)) <= 0;
//                    }
//
//                    return ok;
//
//                }).limit(3).toList();
//
//        if (result.isEmpty()) {
//
//            // Nếu user có nhập giá -> tìm sản phẩm gần giá nhất
//            if (price != null) {
//
//                List<ChiTietSanPham> nearest = list.stream().sorted((sp1, sp2) -> {
//
//                    BigDecimal diff1 = sp1.getGiaBan().subtract(BigDecimal.valueOf(price)).abs();
//                    BigDecimal diff2 = sp2.getGiaBan().subtract(BigDecimal.valueOf(price)).abs();
//
//                    return diff1.compareTo(diff2);
//                }).limit(3).toList();
//
//                StringBuilder sb = new StringBuilder();
//
//                sb.append("Shop hiện chưa có sản phẩm dưới ").append(vnFormat.format(price)).append("đ.\n\n");
//
//                sb.append("Nhưng đây là vài sản phẩm có giá gần nhất:\n\n");
//
//                for (ChiTietSanPham sp : nearest) {
//
//                    Integer productId = sp.getIdSanPham().getId();
//                    String link = "/home/product/" + productId;
//
//                    sb.append("• ").append(sp.getIdSanPham().getTenSp()).append(" | ").append(sp.getIdMauSac().getTenMauSac()).append(" | Size ").append(sp.getIdKichCo().getTenKichCo()).append(" | ").append(vnFormat.format(sp.getGiaBan())).append("đ\n").append("Xem sản phẩm: ").append(link).append("\n\n");
//                }
//
//                return sb.toString();
//            }
//
//            // Nếu không có giá -> trả best seller như cũ
//            List<SanPhamBanChayResponse> bestSeller = thongKeRepository.getTopBanChay().stream().limit(3).toList();
//
//            StringBuilder sb = new StringBuilder();
//
//            sb.append("Hiện tại bên shop đã hết các sản phẩm phù hợp với yêu cầu của bạn.\n");
//            sb.append("Tuy nhiên shop có một số mẫu đang bán rất chạy:\n\n");
//
//            for (SanPhamBanChayResponse sp : bestSeller) {
//
//                sb.append("• ").append(sp.getTenSanPham()).append(" | ").append(vnFormat.format(sp.getGiaBan())).append("đ\n").append("Đã bán: ").append(sp.getSoLuongDaBan()).append("\n\n");
//            }
//
//            return sb.toString();
//        }
//
//        StringBuilder sb = new StringBuilder();
//
//        sb.append("Shop tìm thấy sản phẩm phù hợp:\n\n");
//
//        for (ChiTietSanPham sp : result) {
//
//            Integer productId = sp.getIdSanPham().getId();
//
//            String link = "/home/product/" + productId;
//
//            sb.append("• ").append(sp.getIdSanPham().getTenSp()).append(" | ").append(sp.getIdMauSac().getTenMauSac()).append(" | Size ").append(sp.getIdKichCo().getTenKichCo()).append(" | ").append(vnFormat.format(sp.getGiaBan())).append("đ\n").append("Xem sản phẩm: ").append(link).append("\n\n");
//        }
//
//        return sb.toString();
//    }
//
//    private String handleDynamicFilter(String size, Long maxPrice) {
//
//        BigDecimal min = BigDecimal.ZERO;
//        BigDecimal max = maxPrice != null ? BigDecimal.valueOf(maxPrice) : null;
//
//        Integer kichCoId = null;
//
//        if (size != null) {
//            // bạn cần query tìm id kích cỡ theo tên
//            // hoặc hardcode nếu biết id
//        }
//
//        Page<ChiTietSanPham> page = chiTietRepo.filter(null, null, null, kichCoId, 1, min, max, PageRequest.of(0, 3));
//
//        List<ChiTietSanPham> list = page.getContent();
//
//        if (list.isEmpty()) {
//            return "Không tìm thấy sản phẩm phù hợp yêu cầu ạ.";
//        }
//
//        StringBuilder sb = new StringBuilder();
//        sb.append("Sản phẩm phù hợp yêu cầu của bạn:\n\n");
//
//        for (ChiTietSanPham sp : list) {
//            sb.append("• ").append(sp.getIdSanPham().getTenSp()).append(" - Size ").append(sp.getIdKichCo().getTenKichCo()).append(" - ").append(vnFormat.format(sp.getGiaBan())).append("đ\n");
//        }
//
//        return sb.toString();
//    }
//
//    private String extractSize(String msg) {
//
//        msg = " " + msg.toUpperCase() + " ";
//
//        if (msg.contains(" SIZE S ") || msg.contains(" S ")) {
//            return "S";
//        }
//        if (msg.contains(" SIZE M ") || msg.contains(" M ")) {
//            return "M";
//        }
//        if (msg.contains(" SIZE L ") || msg.contains(" L ")) {
//            return "L";
//        }
//
//        return null;
//    }
//
//    private String handleSize(String size) {
//
//        List<ChiTietSanPham> list = chiTietRepo.getAllActive();
//
//        List<ChiTietSanPham> filtered = list.stream().filter(sp -> sp.getIdKichCo().getTenKichCo().equalsIgnoreCase(size)).toList();
//
//        if (filtered.isEmpty()) {
//            return "Hiện tại shop chưa có size " + size + " ạ.";
//        }
//
//        StringBuilder sb = new StringBuilder();
//        sb.append("👕 Các mẫu size ").append(size).append(":\n\n");
//
//        for (ChiTietSanPham sp : filtered.stream().limit(3).toList()) {
//            sb.append("• ").append(sp.getIdSanPham().getTenSp()).append(" - ").append(vnFormat.format(sp.getGiaBan())).append("đ (Còn ").append(sp.getSoLuongTon()).append(")\n");
//        }
//
//        return sb.toString();
//    }
//
//    private Long extractMaxPrice(String msg) {
//
//        msg = msg.replace("k", "000");
//
//        String[] words = msg.split(" ");
//
//        for (String w : words) {
//            if (w.matches("\\d+")) {
//                return Long.parseLong(w);
//            }
//        }
//        return null;
//    }
//
//    private String handlePriceRange(Long min, Long max) {
//
//        Page<ChiTietSanPham> page = chiTietRepo.filter(null, null, null, null, 1, BigDecimal.valueOf(min), BigDecimal.valueOf(max), PageRequest.of(0, 3));
//
//        List<ChiTietSanPham> filtered = page.getContent();
//
//        if (filtered.isEmpty()) {
//            return "Không có sản phẩm trong khoảng giá này ạ.";
//        }
//
//        StringBuilder sb = new StringBuilder();
//        sb.append(" Sản phẩm trong tầm giá:\n\n");
//
//        for (ChiTietSanPham sp : filtered) {
//            sb.append("• ").append(sp.getIdSanPham().getTenSp()).append(" - Size ").append(sp.getIdKichCo().getTenKichCo()).append(" - ").append(vnFormat.format(sp.getGiaBan())).append("đ\n");
//        }
//
//        return sb.toString();
//    }
//
//
//    private String handleStock() {
//
//        List<ChiTietSanPham> list = chiTietRepo.getAllActive();
//
//        if (list.isEmpty()) {
//            return "Hiện tại sản phẩm đang tạm hết hàng ạ.";
//        }
//
//        ChiTietSanPham sp = list.get(random.nextInt(list.size()));
//
//        if (sp.getSoLuongTon() <= 0) {
//            return "Sản phẩm hiện đã hết hàng ạ.";
//        }
//
//        return "Dạ mẫu " + sp.getIdSanPham().getTenSp() + " - Size " + sp.getIdKichCo().getTenKichCo() + " hiện còn " + sp.getSoLuongTon() + " sản phẩm trong kho.";
//    }
//
//    private String handleVoucher() {
//
//        List<PhieuGiamGia> list = voucherRepo.findActiveVoucher();
//
//        if (list.isEmpty()) {
//            return "Hiện tại shop chưa có chương trình khuyến mãi nào ạ.";
//        }
//
//        PhieuGiamGia p = list.get(random.nextInt(list.size()));
//
//        return randomIntro() + "\n Chương trình: " + p.getTenPgg() + "\n Giá trị giảm: " + p.getGiaTri() + "\n\nAnh/chị cần em hướng dẫn cách áp dụng không ạ?";
//    }
//
//    private String handleOrder(String msg) {
//
//        String[] words = msg.split(" ");
//
//        for (String word : words) {
//            if (word.toUpperCase().startsWith("HD")) {
//
//                Optional<HoaDon> hoaDon = hoaDonRepo.findByMaHoaDon(word.toUpperCase());
//
//                if (hoaDon.isPresent()) {
//
//                    return " Đơn hàng " + word.toUpperCase() + " hiện đang ở trạng thái: " + hoaDon.get().getTrangThai() + ".\n" + "Anh/chị cần hỗ trợ thêm thông tin gì không ạ?";
//                }
//            }
//        }
//
//        return "Anh/chị vui lòng cung cấp mã đơn hàng (VD: HD01) để em kiểm tra giúp ạ.";
//    }
//
//    private String handleProduct() {
//
//        List<ChiTietSanPham> list = chiTietRepo.getAllActive();
//
//        if (list.isEmpty()) {
//            return "Hiện tại shop đang hết hàng ạ.";
//        }
//
//        ChiTietSanPham sp = list.get(random.nextInt(list.size()));
//
//        return randomIntro() + "\n " + sp.getIdSanPham().getTenSp() + "\n Màu: " + sp.getIdMauSac().getTenMauSac() + "\n Size: " + sp.getIdKichCo().getTenKichCo() + "\n Giá: " + vnFormat.format(sp.getGiaBan()) + "đ" + "\n Còn: " + sp.getSoLuongTon();
//    }
//
//    private String handleTopProducts() {
//
//        List<ChiTietSanPham> list = chiTietRepo.getAllActive();
//
//        if (list.isEmpty()) {
//            return "Hiện tại chưa có sản phẩm ạ.";
//        }
//
//        StringBuilder sb = new StringBuilder();
//        sb.append("⭐ Một số mẫu nổi bật:\n\n");
//
//        for (ChiTietSanPham sp : list.stream().limit(3).toList()) {
//            sb.append("• ").append(sp.getIdSanPham().getTenSp()).append(" - Size ").append(sp.getIdKichCo().getTenKichCo()).append(" - ").append(vnFormat.format(sp.getGiaBan())).append("đ\n");
//        }
//
//        return sb.toString();
//    }
//
//    private String normalize(String msg) {
//
//        msg = msg.toLowerCase().trim();
//
//        msg = msg.replace("á", "a").replace("à", "a").replace("ả", "a").replace("ã", "a").replace("ạ", "a");
//
//        msg = msg.replace("é", "e").replace("è", "e").replace("ẻ", "e").replace("ẽ", "e").replace("ẹ", "e");
//
//        msg = msg.replace("í", "i").replace("ì", "i").replace("ỉ", "i").replace("ĩ", "i").replace("ị", "i");
//
//        msg = msg.replace("ó", "o").replace("ò", "o").replace("ỏ", "o").replace("õ", "o").replace("ọ", "o");
//
//        msg = msg.replace("ú", "u").replace("ù", "u").replace("ủ", "u").replace("ũ", "u").replace("ụ", "u");
//
//        msg = msg.replace("đ", "d");
//
//        msg = msg.replace("khoc", "khoac");
//        msg = msg.replace("xnah", "xanh");
//        msg = msg.replace("bom er", "bomber");
//
//        return msg;
//    }
//
//    private String handleSaleProducts() {
//
//        Page<ChiTietSanPham> page = chiTietDggRepo.findActiveSaleProducts(PageRequest.of(0, 3));
//
//        List<ChiTietSanPham> list = page.getContent();
//
//        if (list.isEmpty()) {
//            return "Hiện tại shop chưa có sản phẩm nào đang giảm giá mạnh ạ.";
//        }
//
//        StringBuilder sb = new StringBuilder();
//        sb.append("🔥 Một số sản phẩm đang giảm giá tốt tại shop:\n\n");
//
//        for (ChiTietSanPham sp : list) {
//
//            Integer productId = sp.getIdSanPham().getId();
//            String link = "/home/product/" + productId;
//
//            sb.append("• ").append(sp.getIdSanPham().getTenSp()).append(" | ").append(sp.getIdMauSac().getTenMauSac()).append(" | Size ").append(sp.getIdKichCo().getTenKichCo()).append("\nGiá: ").append(vnFormat.format(sp.getGiaBan())).append("đ").append("\nXem sản phẩm: ").append(link).append("\n\n");
//        }
//
//        return sb.toString();
//    }
//}
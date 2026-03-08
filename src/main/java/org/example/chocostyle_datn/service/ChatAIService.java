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

        String msg = normalize(message);

        String size = extractSize(msg);
        String color = extractColor(msg);
        Long price = extractMaxPrice(msg);
        String name = extractProductName(msg);

        if(size != null || color != null || price != null || name != null){
            return searchProduct(size,color,price,name);
        }

        if (containsAny(msg,"voucher","giam gia","sale","khuyen mai")){
            return handleVoucher();
        }

        if (containsAny(msg,"don hang","hoa don","hd")){
            return handleOrder(msg);
        }

        if (containsAny(msg,"goi y","ban chay","top")){
            return handleTopProducts();
        }

        return "Shop có áo khoác nam nhiều mẫu. Bạn có thể hỏi ví dụ:\n"
                +"• áo khoác xanh size m\n"
                +"• áo bomber đen dưới 500k\n"
                +"• shop có voucher không";
    }

    private String extractColor(String msg){

        if(msg.contains("xanh")) return "xanh";
        if(msg.contains("den")) return "den";
        if(msg.contains("trang")) return "trang";
        if(msg.contains("do")) return "do";
        if(msg.contains("vang")) return "vang";

        return null;
    }

    private String extractProductName(String msg){

        if(msg.contains("bomber")) return "bomber";
        if(msg.contains("hoodie")) return "hoodie";
        if(msg.contains("khoac")) return "khoac";

        return null;
    }

    private int levenshtein(String a, String b){

        int[][] dp = new int[a.length()+1][b.length()+1];

        for(int i=0;i<=a.length();i++) dp[i][0]=i;
        for(int j=0;j<=b.length();j++) dp[0][j]=j;

        for(int i=1;i<=a.length();i++){
            for(int j=1;j<=b.length();j++){

                int cost = a.charAt(i-1)==b.charAt(j-1)?0:1;

                dp[i][j] = Math.min(
                        Math.min(dp[i-1][j]+1,dp[i][j-1]+1),
                        dp[i-1][j-1]+cost
                );
            }
        }

        return dp[a.length()][b.length()];
    }

    private String searchProduct(String size,String color,Long price,String name){

        List<ChiTietSanPham> list = chiTietRepo.getAllActive();

        List<ChiTietSanPham> result = list.stream()

                .filter(sp->{

                    boolean ok=true;

                    if(size!=null){
                        ok &= sp.getIdKichCo().getTenKichCo()
                                .equalsIgnoreCase(size);
                    }

                    if(color!=null){
                        ok &= normalize(sp.getIdMauSac().getTenMauSac())
                                .contains(color);
                    }

                    if(name!=null){

                        String ten = normalize(sp.getIdSanPham().getTenSp());

                        if(!ten.contains(name)){

                            int distance = levenshtein(ten,name);

                            ok &= distance<=3;
                        }
                    }

                    if(price!=null){

                        ok &= sp.getGiaBan()
                                .compareTo(BigDecimal.valueOf(price))<=0;
                    }

                    return ok;

                })
                .limit(3)
                .toList();

        if(result.isEmpty()){
            return "Shop chưa tìm thấy sản phẩm phù hợp ạ.";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Shop tìm thấy sản phẩm phù hợp:\n\n");

        for(ChiTietSanPham sp:result){

            sb.append("• ")
                    .append(sp.getIdSanPham().getTenSp())
                    .append(" | ")
                    .append(sp.getIdMauSac().getTenMauSac())
                    .append(" | Size ")
                    .append(sp.getIdKichCo().getTenKichCo())
                    .append(" | ")
                    .append(vnFormat.format(sp.getGiaBan()))
                    .append("đ\n");
        }

        return sb.toString();
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

    private String normalize(String msg) {

        msg = msg.toLowerCase().trim();

        msg = msg.replace("á", "a")
                .replace("à", "a")
                .replace("ả", "a")
                .replace("ã", "a")
                .replace("ạ", "a");

        msg = msg.replace("é", "e")
                .replace("è", "e")
                .replace("ẻ", "e")
                .replace("ẽ", "e")
                .replace("ẹ", "e");

        msg = msg.replace("í", "i")
                .replace("ì", "i")
                .replace("ỉ", "i")
                .replace("ĩ", "i")
                .replace("ị", "i");

        msg = msg.replace("ó", "o")
                .replace("ò", "o")
                .replace("ỏ", "o")
                .replace("õ", "o")
                .replace("ọ", "o");

        msg = msg.replace("ú", "u")
                .replace("ù", "u")
                .replace("ủ", "u")
                .replace("ũ", "u")
                .replace("ụ", "u");

        msg = msg.replace("đ", "d");

        msg = msg.replace("khoc", "khoac");
        msg = msg.replace("xnah", "xanh");
        msg = msg.replace("bom er", "bomber");

        return msg;
    }
}
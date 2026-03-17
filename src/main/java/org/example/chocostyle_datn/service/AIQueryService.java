package org.example.chocostyle_datn.service;


import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.*;
import org.example.chocostyle_datn.repository.*;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class AIQueryService {


    private final SanPhamRepository sanPhamRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;
    private final PhieuGiamGiaRepository phieuGiamGiaRepository;
    private final PhieuGiamGiaKhachHangRepository phieuGiamGiaKhachHangRepository;
    private final DotGiamGiaRepository dotGiamGiaRepository;
    private final ChiTietDotGiamGiaRepository chiTietDotGiamGiaRepository;
    private final MauSacRepository mauSacRepository;
    private final KichCoRepository kichCoRepository;
    private final ChatLieuRepository chatLieuRepository;
    private final PhongCachMacRepository phongCachMacRepository;
    private final LoaiAoRepository loaiAoRepository;


    public String getThongTinSanPham(String keyword) {


        keyword = keyword.trim().toLowerCase();


        List<SanPham> sanPhams = sanPhamRepository.searchSanPhamForAI(keyword);


        System.out.println("KEYWORD = " + keyword);
        System.out.println("RESULT SIZE = " + sanPhams.size());


        if (sanPhams.isEmpty()) {
            return "Shop chưa tìm thấy sản phẩm phù hợp.";
        }


        StringBuilder result = new StringBuilder();
        int count = 0;


        for (SanPham sp : sanPhams) {


            if (count >= 5) break;


            result.append("Tên sản phẩm: ").append(sp.getTenSp()).append("\n");


            // ===== THUỘC TÍNH SẢN PHẨM =====
            if (sp.getIdXuatXu() != null) {
                result.append("Xuất xứ: ")
                        .append(sp.getIdXuatXu().getTenXuatXu())
                        .append("\n");
            }


            if (sp.getIdChatLieu() != null) {
                result.append("Chất liệu: ")
                        .append(sp.getIdChatLieu().getTenChatLieu())
                        .append("\n");
            }


            result.append("\nBiến thể:\n");


            List<ChiTietSanPham> ctList =
                    chiTietSanPhamRepository.findByIdSanPham_Id(sp.getId());


            for (ChiTietSanPham ct : ctList) {

                result.append("- Màu: ")
                        .append(ct.getIdMauSac().getTenMauSac())

                        .append(" | Size: ")
                        .append(ct.getIdKichCo().getTenKichCo())

                        .append(" | Loại áo: ")
                        .append(ct.getIdLoaiAo().getTenLoai())

                        .append(" | Kiểu dáng: ")
                        .append(ct.getIdKieuDang().getTenKieuDang())

                        .append(" | Phong cách: ")
                        .append(ct.getIdPhongCachMac().getTenPhongCach())

                        .append(" | Giá: ")
                        .append(ct.getGiaBan())
                        .append("đ")

                        .append(" | Tồn kho: ")
                        .append(ct.getSoLuongTon())

                        .append("\n");
            }


            result.append("Xem sản phẩm: <a href='http://localhost:5173/home/product/")
                    .append(sp.getId())
                    .append("' target='_blank'>Click vào đây</a>\n");


            result.append("-----------------------------------\n");


            count++;
        }


        return result.toString();
    }
    public String getVoucherHienTai() {


        List<PhieuGiamGia> vouchers = phieuGiamGiaRepository.findActiveVoucher();


        if (vouchers.isEmpty()) {
            return "Hiện tại cửa hàng chưa có voucher.";
        }


        StringBuilder result = new StringBuilder("Voucher hiện có:\n");


        for (PhieuGiamGia v : vouchers) {


            result.append("- Mã: ").append(v.getMaPgg()).append(" | Giảm: ").append(v.getGiaTri());


            if ("PERCENT".equalsIgnoreCase(v.getLoaiGiam())) {
                result.append("%");
            } else {
                result.append("đ");
            }


            if (v.getGiaTriToiDa() != null) {
                result.append(" (tối đa ").append(v.getGiaTriToiDa()).append("đ)");
            }


            if (v.getDieuKienDonHang() != null) {
                result.append(" | Đơn tối thiểu ").append(v.getDieuKienDonHang()).append("đ");
            }


            result.append("\n");
        }


        return result.toString();
    }


    public String getVoucherCaNhan(Integer khachHangId) {


        List<PhieuGiamGia> vouchers = phieuGiamGiaRepository.findActiveVoucher();


        StringBuilder result = new StringBuilder("Voucher của bạn:\n");


        boolean coVoucher = false;


        for (PhieuGiamGia v : vouchers) {


            boolean co = phieuGiamGiaKhachHangRepository.existsByPhieuGiamGiaIdAndKhachHangIdAndDaSuDungFalse(v.getId(), khachHangId);


            if (co) {


                coVoucher = true;


                result.append("- Mã: ").append(v.getMaPgg()).append(" | Giảm: ").append(v.getGiaTri());


                if ("PERCENT".equalsIgnoreCase(v.getLoaiGiam())) {
                    result.append("%");
                } else {
                    result.append("đ");
                }


                result.append("\n");
            }
        }


        if (!coVoucher) {
            return "Bạn hiện chưa có voucher cá nhân.";
        }


        return result.toString();
    }


    public String getSanPhamTheoKhoangGia(Integer minPrice, Integer maxPrice) {


        List<SanPham> sanPhams = sanPhamRepository.findSanPhamTheoGia(minPrice, maxPrice);


        if (sanPhams.isEmpty()) {
            return "Không có sản phẩm trong khoảng giá này.";
        }


        StringBuilder result = new StringBuilder("Sản phẩm từ " + minPrice + "đ đến " + maxPrice + "đ:\n");


        for (SanPham sp : sanPhams) {


            result.append("Sản phẩm: ").append(sp.getTenSp()).append("\n");


            result.append("Xem sản phẩm: <a href='http://localhost:5173/home/product/").append(sp.getId()).append("' target='_blank'>Click vào đây</a>\n");


            List<ChiTietSanPham> ctList = chiTietSanPhamRepository.findByIdSanPham_Id(sp.getId());


            for (ChiTietSanPham ct : ctList) {


                if (ct.getGiaBan().compareTo(BigDecimal.valueOf(minPrice)) >= 0 && ct.getGiaBan().compareTo(BigDecimal.valueOf(maxPrice)) <= 0) {


                    result.append("- Màu ").append(ct.getIdMauSac().getTenMauSac()).append(" | Size ").append(ct.getIdKichCo().getTenKichCo()).append(" | Giá ").append(ct.getGiaBan()).append("đ | Tồn ").append(ct.getSoLuongTon()).append("\n");
                }
            }


            result.append("\n");
        }


        return result.toString();
    }


    public String getDotGiamGiaHienTai() {


        List<DotGiamGia> list = dotGiamGiaRepository.findActiveDotGiamGia();


        if (list.isEmpty()) {
            return "Hiện tại cửa hàng chưa có đợt giảm giá nào.";
        }


        Map<Integer, ChiTietDotGiamGia> bestDiscountPerProduct = new HashMap<>();


        for (DotGiamGia d : list) {


            List<ChiTietDotGiamGia> ctList =
                    chiTietDotGiamGiaRepository.findById_IdDotGiamGia(d.getId());


            for (ChiTietDotGiamGia ct : ctList) {


                ChiTietSanPham spct = ct.getIdSpct();
                int spId = spct.getIdSanPham().getId();


                if (!bestDiscountPerProduct.containsKey(spId) ||
                        d.getGiaTriGiam().compareTo(
                                bestDiscountPerProduct.get(spId)
                                        .getIdDotGiamGia()
                                        .getGiaTriGiam()
                        ) > 0) {


                    bestDiscountPerProduct.put(spId, ct);
                }
            }
        }


        if (bestDiscountPerProduct.isEmpty()) {
            return "Hiện tại chưa có sản phẩm nào trong đợt giảm giá.";
        }


        StringBuilder result = new StringBuilder("🔥 Các sản phẩm đang giảm giá:\n\n");


        int count = 0;


        for (ChiTietDotGiamGia ct : bestDiscountPerProduct.values()) {


            if (count >= 6) break;


            ChiTietSanPham spct = ct.getIdSpct();
            SanPham sp = spct.getIdSanPham();
            DotGiamGia d = ct.getIdDotGiamGia();


            result.append("- ").append(sp.getTenSp())
                    .append(" | Giá: ").append(spct.getGiaBan()).append("đ")
                    .append(" | Giảm: ").append(d.getGiaTriGiam()).append("%\n");


            result.append("Xem sản phẩm: <a href='http://localhost:5173/home/product/")
                    .append(sp.getId())
                    .append("' target='_blank'>Xem tại đây</a>\n\n");


            count++;
        }


        return result.toString();
    }
    public String getTatCaSanPham() {


        List<SanPham> sanPhams = sanPhamRepository.findAll();


        if (sanPhams.isEmpty()) {
            return "Cửa hàng hiện chưa có sản phẩm.";
        }


        StringBuilder result = new StringBuilder();


        for (SanPham sp : sanPhams) {


            result.append("Sản phẩm: ").append(sp.getTenSp()).append("\n");
            result.append("Xem sản phẩm: <a href='http://localhost:5173/home/product/")
                    .append(sp.getId())
                    .append("' target='_blank'>Xem tại đây</a>\n\n");


            List<ChiTietSanPham> ctList =
                    chiTietSanPhamRepository.findByIdSanPham_Id(sp.getId());


            for (ChiTietSanPham ct : ctList) {


                result.append("- Màu ")
                        .append(ct.getIdMauSac().getTenMauSac())
                        .append(" | Size ")
                        .append(ct.getIdKichCo().getTenKichCo())
                        .append(" | Giá ")
                        .append(ct.getGiaBan())
                        .append(" | Tồn ")
                        .append(ct.getSoLuongTon())
                        .append("\n");
            }


            result.append("\n");
        }


        return result.toString();
    }

    public String searchSanPhamAI(
            String mauSac,
            String size,
            String chatLieu,
            String loaiAo,
            Integer minPrice,
            Integer maxPrice){

        List<ChiTietSanPham> list = chiTietSanPhamRepository.findAll();

        StringBuilder result = new StringBuilder();

        int count = 0;

        Map<Integer, Boolean> daHienThi = new HashMap<>();

        for(ChiTietSanPham ct : list){

            boolean match = true;

            if(mauSac != null &&
                    !ct.getIdMauSac().getTenMauSac().toLowerCase().contains(mauSac))
                match = false;

            if(size != null &&
                    !ct.getIdKichCo().getTenKichCo().toLowerCase().contains(size.toLowerCase()))
                match = false;

            if(chatLieu != null &&
                    !ct.getIdSanPham().getIdChatLieu().getTenChatLieu().toLowerCase().contains(chatLieu))
                match = false;

            if(loaiAo != null &&
                    !ct.getIdLoaiAo().getTenLoai().toLowerCase().contains(loaiAo))
                match = false;

            if(minPrice != null &&
                    ct.getGiaBan().intValue() < minPrice)
                match = false;

            if(maxPrice != null &&
                    ct.getGiaBan().intValue() > maxPrice)
                match = false;

            if(!match) continue;

            SanPham sp = ct.getIdSanPham();

            if(daHienThi.containsKey(sp.getId())) continue;
            daHienThi.put(sp.getId(), true);

            BigDecimal giaGoc = ct.getGiaBan();

            List<ChiTietDotGiamGia> giamGia =
                    chiTietDotGiamGiaRepository.findActiveDiscountBySpctId(ct.getId());

            result.append("Tên sản phẩm: ")
                    .append(sp.getTenSp())
                    .append("\n");

            BigDecimal giaSauGiam = giaGoc;

            if(!giamGia.isEmpty()){

                DotGiamGia dot = giamGia.get(0).getIdDotGiamGia();

                BigDecimal giam = giaGoc
                        .multiply(dot.getGiaTriGiam())
                        .divide(BigDecimal.valueOf(100));

                giaSauGiam = giaGoc.subtract(giam);

                result.append("Giá gốc: ")
                        .append(giaGoc)
                        .append("đ | Đang giảm còn: ")
                        .append(giaSauGiam)
                        .append("đ\n");
            }
            else{

                result.append("Giá: ")
                        .append(giaGoc)
                        .append("đ\n");
            }

            result.append("Màu: ")
                    .append(ct.getIdMauSac().getTenMauSac())
                    .append(" | Size: ")
                    .append(ct.getIdKichCo().getTenKichCo())
                    .append("\n");

            result.append("Xem sản phẩm: <a href='http://localhost:5173/home/product/")
                    .append(sp.getId())
                    .append("' target='_blank'>Xem tại đây</a>\n\n");

            count++;

            if(count >= 6) break;
        }

        if(result.length()==0){
            return "Shop chưa tìm thấy sản phẩm phù hợp.";
        }

        return result.toString();
    }
}


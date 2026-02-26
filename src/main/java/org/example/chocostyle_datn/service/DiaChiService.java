package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.entity.DiaChi;
import org.example.chocostyle_datn.entity.KhachHang;
import org.example.chocostyle_datn.model.Request.DiaChiRequest;
import org.example.chocostyle_datn.repository.DiaChiRepository;
import org.example.chocostyle_datn.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DiaChiService {

    @Autowired
    private DiaChiRepository diaChiRepository;
    @Autowired
    private KhachHangRepository khachHangRepository;

    // 1. Lấy danh sách địa chỉ theo ID khách hàng
    public List<DiaChi> findByKhachHangId(Integer khachHangId) {
        return diaChiRepository.findByKhachHangId(khachHangId);
    }

    // 2. Thêm địa chỉ mới
    @Transactional
    public DiaChi addDiaChi(DiaChiRequest req) {

        KhachHang kh = khachHangRepository.findById(req.getKhachHangId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        DiaChi diaChi = new DiaChi();
        diaChi.setKhachHang(kh);
        diaChi.setTenDiaChi(req.getTenDiaChi());
        diaChi.setDiaChiCuThe(req.getDiaChiCuThe());
        diaChi.setThanhPho(req.getThanhPho());
        diaChi.setQuan(req.getQuan());
        diaChi.setPhuong(req.getPhuong());

        // 1. XỬ LÝ ĐỊA CHỈ MẶC ĐỊNH
        if (req.getMacDinh() != null && req.getMacDinh()) {
            Optional<DiaChi> oldDefault = diaChiRepository.findByKhachHangIdAndMacDinhTrue(kh.getId());
            if (oldDefault.isPresent()) {
                DiaChi oldAddr = oldDefault.get();
                oldAddr.setMacDinh(false);
                diaChiRepository.save(oldAddr);
            }
            diaChi.setMacDinh(true);
        } else {
            List<DiaChi> existingList = diaChiRepository.findByKhachHangId(kh.getId());
            if (existingList.isEmpty()) {
                diaChi.setMacDinh(true);
            } else {
                diaChi.setMacDinh(false);
            }
        }

        // 2. TẠO MÃ ĐỊA CHỈ TỰ ĐỘNG
        diaChi.setMaDiaChi("DC" + System.currentTimeMillis());

        // 3. LƯU VÀO DB
        return diaChiRepository.save(diaChi);
    }

    // 3. THIẾT LẬP ĐỊA CHỈ MẶC ĐỊNH (MỚI THÊM)
    @Transactional
    public void setDefaultAddress(Integer addressId, Integer khachHangId) {
        // Bước 1: Tìm địa chỉ mặc định cũ và gỡ bỏ trạng thái mặc định (set = false)
        Optional<DiaChi> oldDefault = diaChiRepository.findByKhachHangIdAndMacDinhTrue(khachHangId);
        if (oldDefault.isPresent()) {
            DiaChi oldAddr = oldDefault.get();
            oldAddr.setMacDinh(false);
            diaChiRepository.save(oldAddr);
        }

        // Bước 2: Tìm địa chỉ cần set mặc định và cập nhật trạng thái (set = true)
        DiaChi newDefault = diaChiRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với ID: " + addressId));

        // Kiểm tra xem địa chỉ này có đúng là của khách hàng này không (bảo mật)
        if (!newDefault.getKhachHang().getId().equals(khachHangId)) {
            throw new RuntimeException("Địa chỉ này không thuộc về khách hàng hiện tại!");
        }

        newDefault.setMacDinh(true);
        diaChiRepository.save(newDefault);
    }
}
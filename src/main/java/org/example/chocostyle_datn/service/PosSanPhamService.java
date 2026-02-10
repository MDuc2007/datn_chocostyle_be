package org.example.chocostyle_datn.service;


import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.model.Response.PosSanPhamResponse;
import org.example.chocostyle_datn.repository.PosSanPhamRepository;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
@RequiredArgsConstructor
public class PosSanPhamService {


    private final PosSanPhamRepository repository;


    public List<PosSanPhamResponse> getSanPhamBanTaiQuay() {
        return repository.getSanPhamBanTaiQuay();
    }
}


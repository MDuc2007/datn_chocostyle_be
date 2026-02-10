package org.example.chocostyle_datn.controller;


import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.model.Response.PosSanPhamResponse;
import org.example.chocostyle_datn.service.PosSanPhamService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;


@RestController
@RequestMapping("/api/pos/san-pham")
@RequiredArgsConstructor
@CrossOrigin("*")
public class PosSanPhamController {


    private final PosSanPhamService service;


    @GetMapping
    public List<PosSanPhamResponse> getSanPhamBanTaiQuay() {
        return service.getSanPhamBanTaiQuay();
    }
}


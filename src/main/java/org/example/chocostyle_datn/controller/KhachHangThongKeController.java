package org.example.chocostyle_datn.controller;


import org.example.chocostyle_datn.entity.KhachHangThongKe;
import org.example.chocostyle_datn.service.KhachHangThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;


@RestController
@RequestMapping("/api/admin/khach-hang-thong-ke")
public class KhachHangThongKeController {


    @Autowired
    private KhachHangThongKeService service;


    @GetMapping
    public List<KhachHangThongKe> getAll() {
        return service.getAll();
    }
}


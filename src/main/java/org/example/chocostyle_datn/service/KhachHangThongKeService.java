package org.example.chocostyle_datn.service;


import org.example.chocostyle_datn.entity.KhachHangThongKe;
import org.example.chocostyle_datn.repository.KhachHangThongKeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;


@Service
public class KhachHangThongKeService {


    @Autowired
    private KhachHangThongKeRepository repo;


    public List<KhachHangThongKe> getAll() {
        return repo.findAll();
    }
}


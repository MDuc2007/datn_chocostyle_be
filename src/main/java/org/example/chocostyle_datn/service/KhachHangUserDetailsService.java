package org.example.chocostyle_datn.service;


import org.example.chocostyle_datn.entity.KhachHang;
import org.example.chocostyle_datn.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.util.Collections;


@Service
public class KhachHangUserDetailsService implements UserDetailsService {




    @Autowired
    private KhachHangRepository khachHangRepository;


    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {


        KhachHang kh = khachHangRepository
                .findByTenTaiKhoanOrEmail(usernameOrEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Không tìm thấy khách hàng"));


        if (kh.getTrangThai() != null && kh.getTrangThai() == 0) {
            throw new UsernameNotFoundException("Tài khoản khách hàng bị khóa!");
        }


        String password = kh.getMatKhau() == null ? "" : kh.getMatKhau();


        String role = "ROLE_USER";
        if (kh.getVaiTro() != null) {
            role = "ROLE_" + kh.getVaiTro().toUpperCase();
        }


        return new User(
                kh.getTenTaiKhoan(),   // luôn dùng username chuẩn
                password,
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );


    }
}


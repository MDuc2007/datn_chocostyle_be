package org.example.chocostyle_datn.service;


import org.example.chocostyle_datn.entity.NhanVien;
import org.example.chocostyle_datn.repository.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.util.Collections;


@Service
public class NhanVienUserDetailsService implements UserDetailsService {


    @Autowired
    private NhanVienRepository nhanVienRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {


        NhanVien nv = nhanVienRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Không tìm thấy nhân viên"));


        if (nv.getTrangThai() != null && nv.getTrangThai() == 0) {
            throw new UsernameNotFoundException("Tài khoản nhân viên bị khóa!");
        }


        String role = "ROLE_" + nv.getVaiTro().toUpperCase();


        return new User(
                nv.getEmail(),
                nv.getMatKhau(),
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}




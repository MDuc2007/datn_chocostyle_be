package org.example.chocostyle_datn.service;


import org.example.chocostyle_datn.entity.AuthenticationProvider;
import org.example.chocostyle_datn.entity.KhachHang;
import org.example.chocostyle_datn.repository.KhachHangRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;


@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {


    @Autowired
    private KhachHangRepository khachHangRepository;


    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Gọi API của Google/Facebook để lấy thông tin người dùng
        OAuth2User oAuth2User = super.loadUser(userRequest);


        // 2. Lấy tên provider (google, facebook...)
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        AuthenticationProvider provider;
        if ("google".equalsIgnoreCase(registrationId)) {
            provider = AuthenticationProvider.GOOGLE;
        } else if ("facebook".equalsIgnoreCase(registrationId)) {
            provider = AuthenticationProvider.FACEBOOK;
        } else {
            throw new OAuth2AuthenticationException("Provider không được hỗ trợ");
        }


        // 3. Xử lý lưu hoặc cập nhật vào Database
        return processOAuth2User(oAuth2User, provider);
    }


    private OAuth2User processOAuth2User(OAuth2User oAuth2User, AuthenticationProvider provider) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        // Google dùng "sub" làm ID, Facebook dùng "id"
        String id = oAuth2User.getAttribute("sub") != null ? oAuth2User.getAttribute("sub") : oAuth2User.getAttribute("id");
        String avatar = null;

        if (provider == AuthenticationProvider.GOOGLE) {
            avatar = oAuth2User.getAttribute("picture");
        }

        if (provider == AuthenticationProvider.FACEBOOK) {
            Map<String, Object> pictureObj = oAuth2User.getAttribute("picture");
            if (pictureObj != null) {
                Map<String, Object> data = (Map<String, Object>) pictureObj.get("data");
                if (data != null) {
                    avatar = (String) data.get("url");
                }
            }
        }


        // Tìm xem user này đã tồn tại trong DB chưa
        Optional<KhachHang> khachHangOptional = khachHangRepository.findByEmail(email);


        KhachHang khachHang;
        if (khachHangOptional.isPresent()) {
            // Nếu tìm thấy -> Cập nhật thông tin mới nhất
            khachHang = khachHangOptional.get();
            khachHang.setAuthProvider(provider);
            khachHang.setProviderId(id);
            khachHang.setAvatar(avatar); // Cập nhật avatar nếu họ đổi bên Google
            khachHangRepository.save(khachHang);
        } else {
            // Nếu chưa thấy -> Tạo tài khoản mới
            khachHang = new KhachHang();
            khachHang.setEmail(email);
            khachHang.setTenKhachHang(name);
            khachHang.setAvatar(avatar);
            khachHang.setAuthProvider(provider);
            khachHang.setProviderId(id);
            khachHang.setVaiTro("USER");
            khachHang.setTrangThai(1); // Hoạt động
            khachHang.setNgayTao(LocalDate.now());
            khachHang.setSoDienThoai("");
            khachHang.setMaKh("KH" + String.format("%05d", khachHangRepository.count() + 1));


            khachHangRepository.save(khachHang);
        }


        return oAuth2User;
    }
}








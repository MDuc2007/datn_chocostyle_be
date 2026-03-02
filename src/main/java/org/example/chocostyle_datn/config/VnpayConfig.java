package org.example.chocostyle_datn.config;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

public class VnpayConfig {
    // Thông tin tài khoản Sandbox dùng chung
    public static String vnp_TmnCode = "T0TSMTQ9";
    public static String vnp_HashSecret = "AK2T7GC3TBKJ9LMV5JFC8AQMJXE7F11N";
    public static String vnp_PayUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public static String vnp_ReturnUrl = "http://localhost:8080/api/vnpay/payment-return";

    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] bytes = hmac512.doFinal(data.getBytes());
            StringBuilder hash = new StringBuilder();
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}
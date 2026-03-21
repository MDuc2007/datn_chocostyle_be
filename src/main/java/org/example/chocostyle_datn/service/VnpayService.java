package org.example.chocostyle_datn.service;

import org.example.chocostyle_datn.config.VnpayConfig;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VnpayService {

    public String createPaymentUrl(Integer hoaDonId, BigDecimal amount) {
        String vnp_TxnRef = String.valueOf(hoaDonId);
        String vnp_IpAddr = "127.0.0.1";
        long vnp_Amount = amount.multiply(BigDecimal.valueOf(100)).longValue();

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", VnpayConfig.vnp_TmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(vnp_Amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toan hoa don " + hoaDonId);
        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", VnpayConfig.vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnp_Params.put("vnp_CreateDate", formatter.format(cld.getTime()));
        cld.add(Calendar.MINUTE, 15);
        vnp_Params.put("vnp_ExpireDate", formatter.format(cld.getTime()));

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }
        String queryUrl = query.toString();
        String vnp_SecureHash = VnpayConfig.hmacSHA512(VnpayConfig.vnp_HashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
        return VnpayConfig.vnp_PayUrl + "?" + queryUrl;
    }

    public boolean validateSignature(Map<String, String> params) {
        String vnp_SecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");

        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }
        String calculatedHash = VnpayConfig.hmacSHA512(VnpayConfig.vnp_HashSecret, hashData.toString());
        return calculatedHash.equalsIgnoreCase(vnp_SecureHash);
    }
    // ==========================================
    // LOGIC TẠO THANH TOÁN MOMO
    // ==========================================
    public String createMomoUrl(Integer hoaDonId, BigDecimal amount) throws Exception {
        // Thông tin cấu hình Test của MoMo (Bạn sẽ thay bằng Key thật của bạn sau)
        String partnerCode = "MOMO";
        String accessKey = "F8BBA842ECF85";
        String secretKey = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
        String endpoint = "https://test-payment.momo.vn/v2/gateway/api/create";
        String returnUrl = "http://localhost:8080/api/vnpay/momo/payment-return";
        String notifyUrl = "http://localhost:8080/api/vnpay/momo/payment-notify";

        String orderId = "MOMO_" + hoaDonId + "_" + System.currentTimeMillis();
        String amountStr = String.valueOf(amount.longValue());
        String orderInfo = "Thanh toan don hang " + hoaDonId;
        String requestId = String.valueOf(System.currentTimeMillis());
        String extraData = "";

        // Raw signature data
        String rawHash = "accessKey=" + accessKey +
                "&amount=" + amountStr +
                "&extraData=" + extraData +
                "&ipnUrl=" + notifyUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + returnUrl +
                "&requestId=" + requestId +
                "&requestType=payWithMethod";

        String signature = hmacSHA256(rawHash, secretKey);

        // Tạo JSON body để bắn sang MoMo
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("partnerCode", partnerCode);
        requestBody.put("partnerName", "ChocoStyle");
        requestBody.put("storeId", "ChocoStyleStore");
        requestBody.put("requestId", requestId);
        requestBody.put("amount", amount.longValue());
        requestBody.put("orderId", orderId);
        requestBody.put("orderInfo", orderInfo);
        requestBody.put("redirectUrl", returnUrl);
        requestBody.put("ipnUrl", notifyUrl);
        requestBody.put("lang", "vi");
        requestBody.put("extraData", extraData);
        requestBody.put("requestType", "payWithMethod");
        requestBody.put("signature", signature);

        // Gọi API sang MoMo bằng RestTemplate
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        Map<String, Object> response = restTemplate.postForObject(endpoint, requestBody, Map.class);

        if (response != null && response.containsKey("payUrl")) {
            return (String) response.get("payUrl");
        }
        throw new RuntimeException("Không thể tạo URL MoMo");
    }

    // Thuật toán mã hoá riêng của MoMo (HmacSHA256)
    private String hmacSHA256(String data, String key) throws Exception {
        javax.crypto.Mac sha256_HMAC = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.spec.SecretKeySpec secret_key = new javax.crypto.spec.SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes("UTF-8"));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
    public String createZaloPayUrl(Integer hoaDonId, BigDecimal amount) throws Exception {
        // 1. ĐỔI appId TỪ STRING SANG INT (Số nguyên)
        int appId = 2553;
        String key1 = "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL";
        String endpoint = "https://sb-openapi.zalopay.vn/v2/create";

        String returnUrl = "http://localhost:8080/api/vnpay/zalopay/payment-return?hoaDonId=" + hoaDonId;

        String appTransId = new java.text.SimpleDateFormat("yyMMdd").format(new Date()) + "_" + hoaDonId + "_" + System.currentTimeMillis();
        long amountZalo = amount.longValue();

        Map<String, Object> order = new HashMap<>();
        order.put("app_id", appId); // Đã sửa: truyền Số nguyên thay vì Chuỗi
        order.put("app_trans_id", appTransId);
        order.put("app_time", System.currentTimeMillis());
        order.put("app_user", "ChocoStyle_Customer");
        order.put("amount", amountZalo);
        order.put("description", "ChocoStyle - Thanh toan don hang #" + hoaDonId);

        // 2. XÓA BỎ DÒNG bank_code RỖNG ĐỂ TRÁNH LỖI VALIDATE TỪ ZALOPAY
        // order.put("bank_code", "");

        order.put("item", "[]");
        order.put("embed_data", "{\"redirecturl\": \"" + returnUrl + "\"}");

        // Tạo chuỗi data để băm chữ ký MAC
        String data = appId + "|" + appTransId + "|" + order.get("app_user") + "|" + amountZalo + "|"
                + order.get("app_time") + "|" + order.get("embed_data") + "|" + order.get("item");

        String mac = hmacSHA256(data, key1);
        order.put("mac", mac);

        // Gọi API sang ZaloPay
        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        org.springframework.http.HttpEntity<Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(order, headers);

        Map<String, Object> response = restTemplate.postForObject(endpoint, entity, Map.class);

        // Kiểm tra mã thành công return_code = 1
        if (response != null && (Integer) response.get("return_code") == 1) {
            return (String) response.get("order_url");
        }
        throw new RuntimeException("Không thể tạo URL ZaloPay: " + response);
    }
}
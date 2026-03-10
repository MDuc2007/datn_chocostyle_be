package org.example.chocostyle_datn.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiService {

    @Value("#{'${gemini.api.keys}'.split(',')}")
    private List<String> apiKeys;

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

    private static final String SYSTEM_PROMPT = "Bạn là trợ lý AI của cửa hàng thời trang nam ChocoStyle. " + "Cửa hàng chuyên bán áo khoác nam như: áo khoác bomber, áo khoác dù, áo khoác kaki, áo khoác nỉ và áo khoác mùa đông. " + "Bạn hỗ trợ khách hàng về: sản phẩm (size S, M, L, XL), màu sắc, chất liệu, kiểu dáng, giá bán, " + "chính sách đổi trả, vận chuyển, khuyến mãi và voucher giảm giá. " + "Chỉ được trả lời dựa trên dữ liệu sản phẩm và voucher được cung cấp trong câu hỏi. Không được tự tạo thông tin. " + "Nếu câu hỏi liên quan đến khiếu nại, hoàn tiền, lỗi đơn hàng cụ thể, " + "hoặc khách yêu cầu nói chuyện với nhân viên — chỉ trả về đúng chuỗi: CHUYEN_NHAN_VIEN. " + "Trả lời ngắn gọn, thân thiện, bằng tiếng Việt. Không quá 200 từ.";
    private static final String SYSTEM_PROMPT_NOI_BO = "Bạn là trợ lý AI nội bộ của cửa hàng thời trang nam ChocoStyle. " + "Bạn hỗ trợ nhân viên về: quy trình bán hàng, quản lý hóa đơn, tra cứu đơn hàng, " + "tình trạng tồn kho áo khoác, quản lý khuyến mãi, quy định làm việc và chính sách nội bộ. " + "Nếu vấn đề cần sự phê duyệt của quản lý hoặc admin (ví dụ: hoàn tiền lớn, xử lý khiếu nại đặc biệt, " + "thay đổi chính sách) — chỉ trả về đúng chuỗi: CHUYEN_NHAN_VIEN. " + "Trả lời ngắn gọn, chuyên nghiệp, bằng tiếng Việt. Không quá 200 từ.";
    private final AIQueryService aiQueryService;
    private final RestTemplate restTemplate = new RestTemplate();
    private int currentIndex = 0;

    private synchronized String getNextKey() {

        String key = apiKeys.get(currentIndex);

        currentIndex++;

        if (currentIndex >= apiKeys.size()) {
            currentIndex = 0;
        }

        return key;
    }

    public String hoiGemini(String tinNhanKhach, Integer khachHangId) {

        String duLieuSanPham = "";
        String duLieuDotGiamGia = "";
        String duLieuVoucher = "";
        String duLieuVoucherCaNhan = "";

        Integer[] priceRange = extractPriceRange(tinNhanKhach);
        String msg = tinNhanKhach.toLowerCase();

        // hỏi giảm giá
        if (msg.contains("giảm") || msg.contains("sale") || msg.contains("khuyến mãi")) {

            duLieuDotGiamGia = aiQueryService.getDotGiamGiaHienTai();

        }
        // hỏi voucher
        else if (msg.contains("voucher") || msg.contains("mã")) {

            duLieuVoucher = aiQueryService.getVoucherHienTai();
            duLieuVoucherCaNhan = aiQueryService.getVoucherCaNhan(khachHangId);

        }
        // hỏi sản phẩm theo giá
        else if (priceRange != null) {

            duLieuSanPham = aiQueryService.getSanPhamTheoKhoangGia(priceRange[0], priceRange[1]);

        }
        // hỏi sản phẩm theo keyword
        else {

            String keyword = extractKeyword(tinNhanKhach);
            duLieuSanPham = aiQueryService.getThongTinSanPham(keyword);
        }

        String prompt = """
                Dữ liệu sản phẩm trong cửa hàng:
                
                %s
                
                Đợt giảm giá hiện tại:
                
                %s
                
                Voucher hiện có:
                
                %s
                
                Voucher cá nhân của khách:
                
                %s
                
                Câu hỏi của khách:
                %s
                
                Hãy trả lời dựa trên dữ liệu trên.
                
                QUY TẮC:
                - Không được tự tạo thông tin ngoài dữ liệu.
                - Nếu trong dữ liệu có link sản phẩm thì PHẢI giữ nguyên link.
                - Không được xóa hoặc thay đổi link.
                - Nếu không có dữ liệu phù hợp hãy nói cửa hàng hiện chưa có thông tin.
                """.formatted(duLieuSanPham, duLieuDotGiamGia, duLieuVoucher, duLieuVoucherCaNhan, tinNhanKhach);

        return goiGemini(SYSTEM_PROMPT, prompt, "Xin lỗi, hiện tại tôi không thể xử lý yêu cầu của bạn.");
    }

    public String hoiGeminiNoiBo(String tinNhan) {
        return goiGemini(SYSTEM_PROMPT_NOI_BO, tinNhan, "Xin lỗi, hiện tại tôi không thể xử lý yêu cầu. Vui lòng thử lại sau.");
    }

    private String goiGemini(String systemPrompt, String tinNhan, String fallback) {
        Map<String, Object> systemInstruction = new LinkedHashMap<>();
        systemInstruction.put("parts", List.of(Map.of("text", systemPrompt)));

        Map<String, Object> userContent = Map.of("role", "user", "parts", List.of(Map.of("text", tinNhan == null ? "" : tinNhan)));

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("system_instruction", systemInstruction);
        requestBody.put("contents", List.of(userContent));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        int maxRetry = 3;

        for (int attempt = 1; attempt <= maxRetry; attempt++) {
            try {

                String apiKey = getNextKey();
                String url = GEMINI_URL + "?key=" + apiKey;

                log.info("[GeminiService] Gọi Gemini lần {}", attempt);

                ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    String text = extractText(response.getBody());
                    log.info("[GeminiService] Gemini phản hồi thành công");
                    return text;
                }

            } catch (RestClientResponseException e) {

                int status = e.getStatusCode().value();
                log.error("[GeminiService] Lỗi HTTP {}", status);

                if (status == 429 && attempt < maxRetry) {
                    log.warn("[GeminiService] Hết quota, thử key khác");
                    sleep3s();
                    continue;
                }

            } catch (Exception e) {
                log.error("[GeminiService] Lỗi gọi Gemini API", e);
            }

        }

        return fallback;
    }

    @SuppressWarnings("unchecked")
    private String extractText(Map<?, ?> body) {
        try {
            List<?> candidates = (List<?>) body.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
                Map<?, ?> content = (Map<?, ?>) candidate.get("content");
                List<?> parts = (List<?>) content.get("parts");

                if (parts != null && !parts.isEmpty()) {
                    Map<?, ?> part = (Map<?, ?>) parts.get(0);
                    Object text = part.get("text");
                    if (text != null) {
                        return String.valueOf(text).trim();
                    }
                }
            }
        } catch (Exception e) {
            log.error("[GeminiService] Lỗi parse response", e);
        }

        return "Xin lỗi, tôi không hiểu câu hỏi của bạn. Bạn có thể nói rõ hơn không?";
    }

    private void sleep3s() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String extractKeyword(String message) {
        if (message == null) return "";

        String keyword = message.toLowerCase();

        keyword = keyword.replace("giá sp", "").replace("giá sản phẩm", "").replace("giá", "").replace("bao nhiêu", "").replace("bao nhiu", "").replace("áo", "").replace("khoác", "").replace("sp", "").trim();

        return keyword;
    }

    private Integer[] extractPriceRange(String message) {

        if (message == null) return null;

        message = message.toLowerCase();

        // Regex tìm 2 số trong câu
        Pattern pattern = Pattern.compile("(\\d+)\\s*(k|nghìn|ngan)?\\s*[-đếnto]+\\s*(\\d+)\\s*(k|nghìn|ngan)?");
        Matcher matcher = pattern.matcher(message);

        if (matcher.find()) {

            int min = Integer.parseInt(matcher.group(1));
            int max = Integer.parseInt(matcher.group(3));

            // nếu user viết k (50k)
            if (matcher.group(2) != null) {
                min *= 1000;
            }

            if (matcher.group(4) != null) {
                max *= 1000;
            }

            return new Integer[]{min, max};
        }

        return null;
    }
}
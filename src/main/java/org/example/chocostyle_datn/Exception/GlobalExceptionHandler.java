package org.example.chocostyle_datn.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException; // Đã sửa lại thư viện chuẩn của Web MVC
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Xử lý lỗi trùng lặp (Custom Exception của bạn)
    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<?> handleDuplicate(DuplicateException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT) // 409
                .body(Map.of("message", ex.getMessage()));
    }

    // 2. Xử lý lỗi từ Validation (@Valid) của Spring Boot
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        // Lấy thông báo lỗi đầu tiên trong danh sách lỗi
        FieldError firstError = ex.getBindingResult().getFieldErrors().get(0);

        // Đóng gói thành chuỗi JSON: { "message": "Nội dung lỗi" }
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", firstError.getDefaultMessage());

        return ResponseEntity.badRequest().body(errorResponse);
    }

    // 3. Xử lý tất cả các lỗi RuntimeException (Gộp 2 hàm trùng lặp thành 1)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeExceptions(RuntimeException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", ex.getMessage());

        return ResponseEntity.badRequest().body(errorResponse);
    }
}
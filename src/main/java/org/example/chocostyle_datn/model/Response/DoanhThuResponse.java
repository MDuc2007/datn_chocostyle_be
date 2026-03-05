package org.example.chocostyle_datn.model.Response;

import java.math.BigDecimal;

public interface DoanhThuResponse {
    String getThoiGian();           // Trả về ngày (yyyy-MM-dd)

    BigDecimal getDoanhThu();       // Tổng doanh thu (Thực tế + Dự kiến)

    BigDecimal getDoanhThuThucTe(); // Doanh thu thực tế (Từ đơn Hoàn thành: trạng thái = 4)

    BigDecimal getDoanhThuDuKien(); // Doanh thu dự kiến (Từ đơn đang xử lý: trạng thái 0, 1, 2, 3)

    Integer getSoLuongDon();        // Tổng số lượng đơn hàng (Không tính đơn hủy)
}
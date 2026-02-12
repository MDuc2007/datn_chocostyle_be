package org.example.chocostyle_datn.service;


import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;


@Service
public class ShippingService {


    private static final double SHOP_LAT = 21.0175;
    private static final double SHOP_LNG = 105.8523;


    private static final Map<String, double[]> DISTRICT_COORDS = new HashMap<>();


    static {
        DISTRICT_COORDS.put("Hai Bà Trưng", new double[]{21.0059, 105.8575});
        DISTRICT_COORDS.put("Hoàn Kiếm", new double[]{21.0285, 105.8542});
        DISTRICT_COORDS.put("Đống Đa", new double[]{21.0180, 105.8292});
        DISTRICT_COORDS.put("Ba Đình", new double[]{21.0338, 105.8142});
        DISTRICT_COORDS.put("Cầu Giấy", new double[]{21.0362, 105.7906});
        DISTRICT_COORDS.put("Thanh Xuân", new double[]{20.9980, 105.8060});
        DISTRICT_COORDS.put("Hoàng Mai", new double[]{20.9770, 105.8460});
        DISTRICT_COORDS.put("Long Biên", new double[]{21.0500, 105.8900});
        DISTRICT_COORDS.put("Hà Đông", new double[]{20.9600, 105.7700});
    }


    public int calculateShippingByDistrict(String district) {


        double[] coords = DISTRICT_COORDS.get(district);


        if (coords == null) return 50000;


        double distance = calculateDistance(
                SHOP_LAT,
                SHOP_LNG,
                coords[0],
                coords[1]
        );


        return calculateFeeByKm(distance);
    }


    private double calculateDistance(double lat1, double lon1,
                                     double lat2, double lon2) {


        final int R = 6371;


        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);


        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);


        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));


        return R * c;
    }


    private int calculateFeeByKm(double km) {


        if (km <= 3) return 20000;
        if (km <= 6) return 25000;
        if (km <= 10) return 30000;
        if (km <= 15) return 40000;


        return 50000;
    }
}



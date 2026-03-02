package org.example.chocostyle_datn.controller;


import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.service.ShippingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    @GetMapping
    public Integer calculate(
            @RequestParam String district
    ) {
        return shippingService.calculateShipping(district);
    }
}



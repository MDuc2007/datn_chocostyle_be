package org.example.chocostyle_datn.controller;

import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.PhongCachMac;
import org.example.chocostyle_datn.service.PhongCachMacService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/phong-cach-mac")
@RequiredArgsConstructor
public class PhongCachMacController {

    private final PhongCachMacService service;

    @GetMapping
    public List<PhongCachMac> getAll() {
        return service.getAll();
    }

    @PostMapping
    public PhongCachMac create(@RequestBody PhongCachMac e) {
        return service.create(e);
    }

    @PutMapping("/{id}")
    public PhongCachMac update(@PathVariable Integer id, @RequestBody PhongCachMac e) {
        return service.update(id, e);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}


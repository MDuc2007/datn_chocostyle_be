package org.example.chocostyle_datn.controller;

import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.XuatXu;
import org.example.chocostyle_datn.service.XuatXuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/xuat-xu")
@RequiredArgsConstructor
@CrossOrigin
public class XuatXuController {

    private final XuatXuService service;

    @GetMapping
    public List<XuatXu> getAll() { return service.getAll(); }

    @PostMapping
    public XuatXu create(@RequestBody XuatXu e) { return service.create(e); }

    @PutMapping("/{id}")
    public XuatXu update(@PathVariable Integer id, @RequestBody XuatXu e) {
        return service.update(id, e);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) { service.delete(id); }
}

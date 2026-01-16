package org.example.chocostyle_datn.controller;

import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.LoaiAo;
import org.example.chocostyle_datn.service.LoaiAoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loai-ao")
@RequiredArgsConstructor
@CrossOrigin
public class LoaiAoController {

    private final LoaiAoService service;

    @GetMapping
    public List<LoaiAo> getAll() {
        return service.getAll();
    }

    @PostMapping
    public LoaiAo create(@RequestBody LoaiAo e) {
        return service.create(e);
    }

    @PutMapping("/{id}")
    public LoaiAo update(@PathVariable Integer id, @RequestBody LoaiAo e) {
        return service.update(id, e);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}


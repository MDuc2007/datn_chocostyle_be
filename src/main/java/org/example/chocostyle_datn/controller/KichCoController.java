package org.example.chocostyle_datn.controller;

import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.KichCo;
import org.example.chocostyle_datn.service.KichCoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kich-co")
@RequiredArgsConstructor
@CrossOrigin
public class KichCoController {

    private final KichCoService service;

    @GetMapping
    public List<KichCo> getAll() { return service.getAll(); }

    @PostMapping
    public KichCo create(@RequestBody KichCo e) { return service.create(e); }

    @PutMapping("/{id}")
    public KichCo update(@PathVariable Integer id, @RequestBody KichCo e) {
        return service.update(id, e);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) { service.delete(id); }
}


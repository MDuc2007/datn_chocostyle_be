package org.example.chocostyle_datn.controller;

import jakarta.validation.Valid;
import org.example.chocostyle_datn.model.Request.PhieuGiamGiaRequest;
import org.example.chocostyle_datn.model.Response.PhieuGiamGiaResponse;
import org.example.chocostyle_datn.service.PhieuGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/admin/voucher")
public class PhieuGiamGiaController {
    @Autowired
    private PhieuGiamGiaService phieuGiamGiaService;

    @GetMapping
    public ResponseEntity<List<PhieuGiamGiaResponse>> getAllPGG() {
        return ResponseEntity.ok(phieuGiamGiaService.getAllPGG());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhieuGiamGiaResponse> getPGGById(@PathVariable Integer id) {
        return ResponseEntity.ok(phieuGiamGiaService.getPGGById(id));
    }

    @PutMapping("/{id}/toggle")
    public PhieuGiamGiaResponse toggle(@PathVariable Integer id) {
        return phieuGiamGiaService.toggleTrangThai(id);
    }

    @GetMapping("/next-code")
    public ResponseEntity<String> getNextMaPgg() {
        return ResponseEntity.ok(phieuGiamGiaService.generateMaPgg());
    }

    @PostMapping
    public ResponseEntity<PhieuGiamGiaResponse> createPGG(@Valid @RequestBody PhieuGiamGiaRequest dto) {
        return ResponseEntity.ok(phieuGiamGiaService.createPGG(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhieuGiamGiaResponse> updatePGG(@PathVariable Integer id, @Valid @RequestBody PhieuGiamGiaRequest dto) {
        return ResponseEntity.ok(phieuGiamGiaService.updatePGG(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePGG(@PathVariable Integer id) {
        phieuGiamGiaService.deletePGG(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<List<PhieuGiamGiaResponse>> filterPGG(
            @RequestParam(required = false) String loaiGiam,
            @RequestParam(required = false) String kieuApDung,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        return ResponseEntity.ok(
                phieuGiamGiaService.filterPGG(loaiGiam,kieuApDung, trangThai, fromDate, toDate)
        );
    }
}

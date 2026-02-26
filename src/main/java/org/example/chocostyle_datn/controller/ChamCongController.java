package org.example.chocostyle_datn.controller;

import org.example.chocostyle_datn.service.ChamCongService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/cham-cong")
public class ChamCongController {

    private final ChamCongService service;

    public ChamCongController(ChamCongService service) {
        this.service = service;
    }

    @PostMapping("/check-in/{idNv}")
    public ResponseEntity<?> checkIn(@PathVariable Integer idNv){

        try {
            return ResponseEntity.ok(service.checkIn(idNv));
        } catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/check-out/{idNv}")
    public ResponseEntity<?> checkOut(@PathVariable Integer idNv) {
        try {
            return ResponseEntity.ok(service.checkOut(idNv));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
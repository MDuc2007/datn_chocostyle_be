package org.example.chocostyle_datn.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.DotGiamGia;
import org.example.chocostyle_datn.model.Request.DotGiamGiaRequest;
import org.example.chocostyle_datn.model.Response.DotGiamGiaResponse;
import org.example.chocostyle_datn.service.DotGiamGiaService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
public class DotGiamGiaController {


    private final DotGiamGiaService service;


    @PostMapping
    public DotGiamGia create(
            @RequestBody @Valid DotGiamGiaRequest request
    ) {
        return service.createDGG(request);
    }

    @PutMapping("/{id}")
    public DotGiamGia update(
            @PathVariable Integer id,
            @RequestBody @Valid DotGiamGiaRequest request
    ) {
        return service.updateDGG(id, request);
    }


    @GetMapping("/{id}")
    public DotGiamGiaResponse getById(@PathVariable Integer id) {
        return service.getById(id);
    }


    @GetMapping
    public List<DotGiamGiaResponse> getAll() {
        return service.getAllDGG();
    }


    @DeleteMapping("/{id}")
    public Boolean delete(@PathVariable Integer id) {
        return service.deleteDGG(id);
    }


    @GetMapping("/filter")
    public Page<DotGiamGiaResponse> filterPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer trangThai,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return service.filterPage(keyword, trangThai, start, end, page, size);
    }

    @PatchMapping("/{id}/toggle")
    public DotGiamGiaResponse toggle(@PathVariable Integer id) {
        return service.toggleTrangThai(id);
    }


}




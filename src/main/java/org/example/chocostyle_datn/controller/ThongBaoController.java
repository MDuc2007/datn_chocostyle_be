package org.example.chocostyle_datn.controller;

import org.example.chocostyle_datn.entity.ThongBao;
import org.example.chocostyle_datn.repository.ThongBaoRepository;
import org.example.chocostyle_datn.service.ThongBaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/thong-bao")
@CrossOrigin("*")
public class ThongBaoController {

    @Autowired
    private ThongBaoRepository thongBaoRepository;

    @Autowired
    private ThongBaoService thongBaoService;

    @GetMapping
    public Page<ThongBao> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable =
                PageRequest.of(page, size, Sort.by("ngayTao").descending());

        return thongBaoRepository.findAll(pageable);
    }

    @GetMapping("/count")
    public Long countThongBao(){
        return thongBaoRepository.countByDaDocFalse();
    }

    @PutMapping("/{id}/read")
    public void danhDauDaDoc(@PathVariable Long id){

        ThongBao tb = thongBaoRepository.findById(id).orElse(null);

        if(tb != null){
            tb.setDaDoc(true);
            thongBaoRepository.save(tb);
        }
    }

    @PostMapping("/support-request")
    public void requestSupport(@RequestBody Map<String, Integer> body){
        Integer khachHangId = body.get("khachHangId");
        Integer conversationId = body.get("conversationId");
        thongBaoService.thongBaoSupportRequest(khachHangId, conversationId);
    }

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanOldNotifications() {

        List<ThongBao> list = thongBaoRepository.findAll(
                PageRequest.of(1000, Integer.MAX_VALUE)
        ).getContent();

        thongBaoRepository.deleteAll(list);
    }
}
package org.example.chocostyle_datn.controller;

import lombok.RequiredArgsConstructor;
import org.example.chocostyle_datn.entity.ChatLieu;
import org.example.chocostyle_datn.service.ChatLieuService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat-lieu")
@RequiredArgsConstructor
public class ChatLieuController {

    private final ChatLieuService service;

    @GetMapping
    public List<ChatLieu> getAll() { return service.getAll(); }

    @PostMapping
    public ChatLieu create(@RequestBody ChatLieu e) { return service.create(e); }

    @PutMapping("/{id}")
    public ChatLieu update(@PathVariable Integer id, @RequestBody ChatLieu e) {
        return service.update(id, e);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) { service.delete(id); }
}


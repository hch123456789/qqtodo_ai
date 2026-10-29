package com.example.qqtodoAi.controller;

import com.example.qqtodoAi.model.ContactInfo;
import com.example.qqtodoAi.repository.ContactInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactInfoRepository contactInfoRepository;

    @GetMapping
    public List<ContactInfo> list() {
        return contactInfoRepository.findAllByOrderByIdDesc();
    }

    @PostMapping
    public Map<String, Object> add(@RequestBody Map<String, String> body) {
        String qqStr = body.get("qq");
        if (qqStr == null || qqStr.isBlank()) {
            throw new IllegalArgumentException("QQ号不能为空");
        }

        ContactInfo c = new ContactInfo();
        c.setQq(Long.parseLong(qqStr));
        c.setName(body.getOrDefault("name", ""));
        c.setRole(body.getOrDefault("role", ""));
        String groupId = body.get("groupId");
        c.setGroupId(groupId == null || groupId.isBlank() ? null : Long.parseLong(groupId));
        c.setIsMarked(true);
        contactInfoRepository.save(c);
        return Map.of("success", true);
    }

    @PostMapping("/{id}/toggle")
    public Map<String, Object> toggle(@PathVariable Long id) {
        ContactInfo c = contactInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("联系人不存在: " + id));
        c.setIsMarked(!c.getIsMarked());
        contactInfoRepository.save(c);
        return Map.of("success", true);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        contactInfoRepository.deleteById(id);
        return Map.of("success", true);
    }
}
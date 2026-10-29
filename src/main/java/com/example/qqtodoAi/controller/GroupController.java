package com.example.qqtodoAi.controller;

import com.example.qqtodoAi.model.GroupInfo;
import com.example.qqtodoAi.repository.GroupInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupInfoRepository groupInfoRepository;

    @GetMapping
    public List<GroupInfo> list() {
        return groupInfoRepository.findAllByOrderByIdDesc();
    }

    @PostMapping
    public Map<String, Object> add(@RequestBody Map<String, String> body) {
        String groupIdStr = body.get("groupId");
        if (groupIdStr == null || groupIdStr.isBlank()) {
            throw new IllegalArgumentException("群号不能为空");
        }

        GroupInfo g = new GroupInfo();
        g.setGroupId(Long.parseLong(groupIdStr));
        g.setGroupName(body.getOrDefault("groupName", ""));
        g.setDescription(body.getOrDefault("description", ""));
        g.setEnabled(true);
        groupInfoRepository.save(g);
        return Map.of("success", true);
    }

    @PostMapping("/{id}/toggle")
    public Map<String, Object> toggle(@PathVariable Long id) {
        GroupInfo g = groupInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("群不存在: " + id));
        g.setEnabled(!g.getEnabled());
        groupInfoRepository.save(g);
        return Map.of("success", true);
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        groupInfoRepository.deleteById(id);
        return Map.of("success", true);
    }
}
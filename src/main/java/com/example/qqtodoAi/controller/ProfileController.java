package com.example.qqtodoAi.controller;

import com.example.qqtodoAi.model.UserProfile;
import com.example.qqtodoAi.repository.UserProfileRepository;
import com.example.qqtodoAi.service.RuleRefineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final RuleRefineService ruleRefineService;


    private final UserProfileRepository userProfileRepository;

    @PostMapping
    public Map<String, Object> update(@RequestBody Map<String, String> body) {
        saveOrUpdate("base_identity", body.get("identity"));
        saveOrUpdate("learned_rules", body.get("rules"));
        return Map.of("success", true, "message", "画像已更新");
    }

    private void saveOrUpdate(String key, String value) {
        if (value == null) {
            return;
        }
        Optional<UserProfile> opt = userProfileRepository.findByProfileKey(key);
        UserProfile profile = opt.orElseGet(UserProfile::new);
        profile.setProfileKey(key);
        profile.setProfileValue(value);
        userProfileRepository.save(profile);
    }


    @PostMapping("/refine")
    public Map<String, Object> refine() {
        String rules = ruleRefineService.refineRulesNow();
        return Map.of("success", true, "rules", rules);
    }


}

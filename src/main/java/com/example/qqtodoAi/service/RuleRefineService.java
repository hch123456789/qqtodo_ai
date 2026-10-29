package com.example.qqtodoAi.service;

import com.example.qqtodoAi.model.Feedback;
import com.example.qqtodoAi.model.Message;
import com.example.qqtodoAi.model.UserProfile;
import com.example.qqtodoAi.repository.FeedbackRepository;
import com.example.qqtodoAi.repository.MessageRepository;
import com.example.qqtodoAi.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleRefineService {

    private final FeedbackRepository feedbackRepository;
    private final UserProfileRepository userProfileRepository;
    private final MessageRepository messageRepository;
    private final AiService aiService;

    @Scheduled(cron = "0 0 3 * * *")
    public void autoRefine() {
        try {
            String rules = refineRulesNow();
            log.info("[提炼] 自动提炼完成: {}", rules);
        } catch (Exception e) {
            log.error("[提炼] 自动提炼失败", e);
        }
    }

    public String refineRulesNow() {
        List<Feedback> feedbacks = feedbackRepository.findTop50ByOrderByIdDesc();
        if (feedbacks.isEmpty()) {
            return "暂无反馈，无法提炼规则";
        }
        String summary = buildFeedbackSummary(feedbacks);
        String existing = userProfileRepository.findByProfileKey("learned_rules")
                .map(UserProfile::getProfileValue).orElse("");
        String newRules = aiService.refineRules(summary, existing);
        saveProfile("learned_rules", newRules);
        log.info("[提炼] 新规则: {}", newRules);
        return newRules;
    }

    private String buildFeedbackSummary(List<Feedback> list) {
        List<Long> messageIds = list.stream()
                .map(Feedback::getMessageId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, String> contentMap = messageRepository.findAllById(messageIds).stream()
                .collect(Collectors.toMap(Message::getId, Message::getContent, (a, b) -> a));

        StringBuilder sb = new StringBuilder();
        for (Feedback f : list) {
            String content = contentMap.getOrDefault(f.getMessageId(), "");
            sb.append("- 消息：").append(truncate(content, 80)).append("\n");
            sb.append("  反馈：").append(f.getUserFeedback());
            if (f.getRelevanceScore() != null) {
                sb.append("，不相关程度 ").append(f.getRelevanceScore());
            }
            if (f.getUserNote() != null && !f.getUserNote().isBlank()) {
                sb.append("，备注：").append(f.getUserNote());
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }

    private void saveProfile(String key, String value) {
        UserProfile profile = userProfileRepository.findByProfileKey(key).orElseGet(UserProfile::new);
        profile.setProfileKey(key);
        profile.setProfileValue(value);
        userProfileRepository.save(profile);
    }
}
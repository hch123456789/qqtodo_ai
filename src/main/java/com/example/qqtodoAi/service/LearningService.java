package com.example.qqtodoAi.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.qqtodoAi.model.Feedback;
import com.example.qqtodoAi.model.Message;
import com.example.qqtodoAi.model.UserProfile;
import com.example.qqtodoAi.repository.FeedbackRepository;
import com.example.qqtodoAi.repository.MessageRepository;
import com.example.qqtodoAi.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LearningService {

    private final UserProfileRepository userProfileRepository;
    private final FeedbackRepository feedbackRepository;
    private final MessageRepository messageRepository;

    public String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();

        sb.append("你是我的个人消息助理，负责判断 QQ 群消息是否与我相关，并提取我需要做的任务。\n\n");

        String identity = getProfile("base_identity");
        if (identity != null && !identity.isBlank()) {
            sb.append("【我的身份】\n").append(identity).append("\n\n");
        }
        sb.append("【当前时间】")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .append("\n\n");

        String rules = getProfile("learned_rules");
        if (rules != null && !rules.isBlank()) {
            sb.append("【我学到的偏好规则】\n").append(rules).append("\n\n");
        }

        sb.append("""
        【任务】
        判断下面的 QQ 群消息是否与我相关，相关则提取我需要做的事。
        请根据上面的【我的身份】和【我学到的偏好规则】来判断。
        如果画像为空，则只提取明显需要我行动的任务（有明确截止时间、明确指定我、或与我专业/身份相关）。

        【输出格式】只输出一个 JSON：
        {
          "relevant": true,
          "task": "任务内容",
          "time": "2026-11-03 23:59",
          "location": "地点",
          "source": "来源",
          "priority": "高"
        }
        不相关时 relevant=false，其他字段为 null。
        time 格式 YYYY-MM-DD HH:mm，无年份默认 2026。
        """);

        appendSamples(sb);

        return sb.toString();
    }

    private void appendSamples(StringBuilder sb) {
        List<Feedback> rejects = feedbackRepository.findTop10ByUserFeedbackOrderByIdDesc("reject");
        List<Feedback> corrects = feedbackRepository.findTop5ByUserFeedbackOrderByIdDesc("correct");

        boolean hasReject = rejects != null && !rejects.isEmpty();
        boolean hasCorrect = corrects != null && !corrects.isEmpty();

        if (!hasReject && !hasCorrect) {
            return;
        }

        List<Long> messageIds = new ArrayList<>();
        if (hasReject) {
            rejects.forEach(f -> {
                if (f.getMessageId() != null) messageIds.add(f.getMessageId());
            });
        }
        if (hasCorrect) {
            corrects.forEach(f -> {
                if (f.getMessageId() != null) messageIds.add(f.getMessageId());
            });
        }

        Map<Long, String> contentMap = messageRepository.findAllById(messageIds).stream()
                .collect(Collectors.toMap(Message::getId, Message::getContent, (a, b) -> a));

        sb.append("\n【我过去的反馈样本，请重点参考，避免再犯同样的错误】\n");

        if (hasReject) {
            for (Feedback f : rejects) {
                String msgContent = contentMap.getOrDefault(f.getMessageId(), "");
                sb.append("- 消息：").append(truncate(msgContent, 100)).append("\n");
                sb.append("  我的反馈：不相关（不相关程度 ").append(f.getRelevanceScore()).append("）");
                if (f.getUserNote() != null && !f.getUserNote().isBlank()) {
                    sb.append("，原因：").append(f.getUserNote());
                }
                sb.append("\n");
            }
        }

        if (hasCorrect) {
            for (Feedback f : corrects) {
                String msgContent = contentMap.getOrDefault(f.getMessageId(), "");
                sb.append("- 消息：").append(truncate(msgContent, 100)).append("\n");
                sb.append("  我的反馈：相关，但需要修正");
                if (f.getUserNote() != null && !f.getUserNote().isBlank()) {
                    sb.append("，修正说明：").append(f.getUserNote());
                }
                sb.append("\n");
            }
        }
    }

    private String getProfile(String key) {
        return userProfileRepository.findByProfileKey(key)
                .map(UserProfile::getProfileValue)
                .orElse(null);
    }

    private String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
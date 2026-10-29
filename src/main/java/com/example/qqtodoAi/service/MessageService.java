package com.example.qqtodoAi.service;

import com.example.qqtodoAi.model.*;
import com.example.qqtodoAi.repository.*;
import com.example.qqtodoAi.util.TimeUtils;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final GroupInfoRepository groupInfoRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final MessageRepository messageRepository;
    private final TodoItemRepository todoItemRepository;
    private final AiService aiService;
    private final UserProfileRepository userProfileRepository;

    private static final String[] KEYWORDS = {
            "作业", "截止", "提交", "考试", "会议",
            "通知", "报告", "实验", "报名", "任务",
            "答辩", "测评", "填写", "材料", "安排"
    };

    public void handleGroupMessage(JsonNode event) {
        long groupId = event.path("group_id").asLong(0);
        long senderQq = event.path("user_id").asLong(0);
        String rawMessage = event.path("raw_message").asText();

        if (!groupInfoRepository.existsByGroupIdAndEnabledTrue(groupId)) {
            log.info("[过滤] 群 {} 不在白名单，忽略", groupId);
            return;
        }

        boolean isMarkedPerson = contactInfoRepository.existsMarked(senderQq, groupId);
        boolean hasKeyword = containsKeyword(rawMessage);
        if (!isMarkedPerson && !hasKeyword) {
            log.info("[过滤] 发送者 {} 未标记且无关键词，忽略", senderQq);
            return;
        }

        Message msg = saveMessage(event, groupId, senderQq, rawMessage);
        String prompt = buildPrompt(groupId, senderQq, rawMessage);
        analyzeAndCreateTodo(prompt, msg.getId());
    }

    public void handleRecognizedText(String text, JsonNode event) {
        long groupId = event.path("group_id").asLong(0);
        long senderQq = event.path("user_id").asLong(0);

        if (text == null || text.isBlank()) {
            log.info("[视觉] 图片未识别出文字，忽略");
            return;
        }

        Message msg = saveMessage(event, groupId, senderQq, "[图片识别] " + text);
        String prompt = buildPrompt(groupId, senderQq, text);
        analyzeAndCreateTodo(prompt, msg.getId());
    }

    private Message saveMessage(JsonNode event, long groupId, long senderQq, String content) {
        Message msg = new Message();
        msg.setMessageId(event.path("message_id").asLong(0));
        msg.setGroupId(groupId);
        msg.setSenderQq(senderQq);
        msg.setContent(content);
        msg.setMsgTime(LocalDateTime.now());
        msg.setRawJson(event.toString());
        messageRepository.save(msg);
        log.info("[入库] 已保存消息: {}", content);
        return msg;
    }

    private void analyzeAndCreateTodo(String prompt, Long messageId) {
        try {
            AiResult result = aiService.analyze(prompt);
            if (result != null && result.isRelevant()
                    && result.getTask() != null && !result.getTask().isBlank()) {
                TodoItem todo = new TodoItem();
                todo.setTask(result.getTask());
                todo.setTime(TimeUtils.parse(result.getTime()));
                todo.setLocation(result.getLocation());
                todo.setSource(result.getSource());
                todo.setPriority(result.getPriority());
                todo.setMessageId(messageId);
                todoItemRepository.save(todo);
                log.info("[AI] 生成待办: {}", result.getTask());
            } else {
                log.info("[AI] 消息不相关或未提取到任务");
            }
        } catch (Exception e) {
            log.error("[AI] 分析失败", e);
        }
    }

    private String buildPrompt(long groupId, long senderQq, String content) {
        String groupName = groupInfoRepository.findByGroupId(groupId)
                .map(GroupInfo::getGroupName)
                .orElse("未知群");

        return "群信息：群号 " + groupId + "，群名 " + groupName + "\n"
                + "发送者QQ：" + senderQq + "\n"
                + "消息内容：" + content + "\n"
                + "请判断这条消息是否需要我本人去做。";
    }

    private boolean containsKeyword(String text) {
        if (text == null) {
            return false;
        }
        for (String kw : KEYWORDS) {
            if (text.contains(kw)) {
                return true;
            }
        }
        return false;
    }
}
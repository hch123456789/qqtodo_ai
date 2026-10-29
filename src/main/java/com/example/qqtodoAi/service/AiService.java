package com.example.qqtodoAi.service;

import com.example.qqtodoAi.model.AiResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final LearningService learningService;

    @Value("${ai.api-url}")
    private String apiUrl;

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.model}")
    private String model;

    private static final String SUMMARIZE_PROMPT = """
            你是一个资料整理助手。请阅读下面从文件提取的文本内容，用中文总结。

            输出要求：
            1. 先用一两句话概括文件主题。
            2. 列出关键要点（如时间、地点、截止日期、任务要求、重点概念、名单等）。
            3. 如果文件里有需要我完成的任务，明确单独列出。
            4. 内容简洁，控制在 300 字以内，用条理清晰的方式输出。
            """;
    private static final String REFINE_PROMPT = """
        你是我的个人助理。下面是我过去的一些反馈记录（我对 AI 判断结果的修正）。
        请根据这些反馈，总结出我的偏好规则。

        输出要求：
        1. 用一句一句的规则表达，规则之间用分号分隔。
        2. 规则要具体、可执行，让 AI 下次判断消息时能直接套用。
        3. 例如："只关心本班事务，不关心其他班级/专业的事务；涉及学委职责的消息默认相关；不关心文体活动报名"
        4. 只输出规则文本，不要 markdown、不要解释、不要列表符号。
        """;

    public String refineRules(String feedbackSummary, String existingRules) {
        String userPrompt = "我现有的规则：\n" + (existingRules == null ? "" : existingRules)
                + "\n\n我最近的反馈：\n" + feedbackSummary;
        return call(REFINE_PROMPT, userPrompt, false);
    }

    public AiResult analyze(String userPrompt) throws JsonProcessingException {
        String systemPrompt = learningService.buildSystemPrompt();
        String content = call(systemPrompt, userPrompt, true);
        return objectMapper.readValue(content, AiResult.class);
    }

    public String summarizeFile(String fileName, String content) {
        String userPrompt = "文件名：" + fileName + "\n\n文件内容：\n" + content;
        return call(SUMMARIZE_PROMPT, userPrompt, false);
    }

    private String call(String systemPrompt, String userPrompt, boolean jsonMode) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.1);

        if (jsonMode) {
            requestBody.put("response_format", Map.of("type", "json_object"));
        }

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.add(userMsg);

        requestBody.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
        Map<?, ?> body = response.getBody();
        if (body == null) {
            throw new RuntimeException("AI 响应为空");
        }

        if (body.containsKey("error")) {
            Map<?, ?> error = (Map<?, ?>) body.get("error");
            String errMsg = error != null ? String.valueOf(error.get("message")) : "未知错误";
            throw new RuntimeException("AI API 错误: " + errMsg);
        }

        List<?> choices = (List<?>) body.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("AI 响应缺少 choices");
        }
        Map<?, ?> choice = (Map<?, ?>) choices.get(0);
        Map<?, ?> message = (Map<?, ?>) choice.get("message");
        String content = (String) message.get("content");

        log.info("[AI] 原始返回: {}", content);
        return content;
    }

}
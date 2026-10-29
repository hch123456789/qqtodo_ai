package com.example.qqtodoAi.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisionService {

    private final RestTemplate restTemplate;

    @Value("${vision.api-url:}")
    private String apiUrl;

    @Value("${vision.api-key:}")
    private String apiKey;

    @Value("${vision.model:qwen-vl-plus}")
    private String model;

    /**
     * 识别图片中的文字
     */
    public String recognize(File imageFile) {

        if (apiKey == null || apiKey.isBlank()) {
            log.info("[视觉] 未配置视觉 API，跳过识别");
            return null;
        }

        try {
            byte[] bytes = Files.readAllBytes(imageFile.toPath());
            String base64 = Base64.getEncoder().encodeToString(bytes);
            String mime = detectMime(imageFile.getName());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);

            List<Map<String, Object>> content = new ArrayList<>();


            Map<String, Object> textPart = new HashMap<>();
            textPart.put("type", "text");
            textPart.put("text", """
                    请观察这张图片，按以下规则输出：

                    【含文字的图片】
                    - 通知、课表、截图：完整识别所有文字，按原排版顺序输出，保留时间、地点、人物等关键信息
                    - 题目/试卷：按原题格式逐题输出，保留题号、选项、题目要求，每道题之间空一行，不要解答

                    【数学公式规范】
                    使用正确的 Unicode 数学符号，示例：
                    - 分数用 ½ ⅓ ²⁄₃ 或 a/b 表示
                    - 根号用 √ 表示，如 √2、√(x+1)
                    - 乘除用 × ÷ 表示
                    - 幂次用上标数字或 ^ 表示，如 x²、x³、e^x
                    - 不等号用 ≤ ≥ ≠ 表示
                    - 希腊字母用 α β γ θ π Δ Σ 表示
                    - 约等于 ≈，正负 ±，无穷 ∞
                    - 积分 ∫，求和 Σ，连乘 Π
                    - 向量用箭头标注，如 →AB

                    【不含文字的图片】
                    用一两句话客观描述图片内容。
                    如果包含色情等敏感内容
                    用“包含色情等敏感内容：（大致描绘图片内容，包含具体敏感内容）”描述

                    直接输出结果，不要加前缀说明。
                    """);
            content.add(textPart);




            Map<String, Object> imagePart = new HashMap<>();
            imagePart.put("type", "image_url");
            imagePart.put("image_url", Map.of("url", "data:" + mime + ";base64," + base64));
            content.add(imagePart);

            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", content);
            requestBody.put("messages", List.of(userMsg));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> resp = restTemplate.postForEntity(apiUrl, entity, Map.class);
            Map<?, ?> body = resp.getBody();
            if (body == null) {
                throw new RuntimeException("视觉 API 响应为空");
            }

            List<?> choices = (List<?>) body.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("视觉 API 缺少 choices");
            }
            Map<?, ?> choice = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) choice.get("message");
            String text = (String) message.get("content");

            log.info("[视觉] 识别结果: {}", text);
            return text;
        } catch (Exception e) {
            log.error("[视觉] 识别失败", e);
            return null;
        }
    }

    private String detectMime(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }
}

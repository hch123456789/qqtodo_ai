package com.example.qqtodoAi.service;

import com.example.qqtodoAi.model.FileInfo;
import com.example.qqtodoAi.repository.FileInfoRepository;
import com.example.qqtodoAi.repository.GroupInfoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final GroupInfoRepository groupInfoRepository;
    private final VisionService visionService;
    private final MessageService messageService;
    private final AiService aiService;
    private final FileInfoRepository fileInfoRepository;
    private final RestTemplate restTemplate;

    @Value("${file.storage-path:./files}")
    private String storagePath;

    /**
     * 处理 QQ 图片消息
     */
    public void handleImageMessage(JsonNode event) {
        long groupId = event.path("group_id").asLong(0);

        if (!groupInfoRepository.existsByGroupIdAndEnabledTrue(groupId)) {
            log.info("[视觉] 群 {} 不在白名单，忽略", groupId);
            return;
        }

        JsonNode messageArray = event.path("message");
        if (!messageArray.isArray()) {
            return;
        }

        for (JsonNode segment : messageArray) {
            if (!"image".equals(segment.path("type").asText())) {
                continue;
            }
            String url = segment.path("data").path("url").asText("");
            String fileName = segment.path("data").path("file").asText("image.jpg");
            long messageId = event.path("message_id").asLong(0);
            long senderQq = event.path("user_id").asLong(0);

            File image = downloadImage(url, messageId, fileName);
            if (image == null) {
                log.warn("[视觉] 图片下载失败: {}", fileName);
                continue;
            }

            String text = visionService.recognize(image);
            if (text == null || text.isBlank()) {
                log.info("[视觉] 图片未识别出内容，跳过");
                continue;
            }

            messageService.handleRecognizedText(text, event);

            try {
                String summary = aiService.summarizeFile(fileName, text);
                FileInfo fileInfo = new FileInfo();
                fileInfo.setMessageId(messageId);
                fileInfo.setGroupId(groupId);
                fileInfo.setSenderQq(senderQq);
                fileInfo.setFileName("[图片识别] " + fileName);
                fileInfo.setFilePath(image.getAbsolutePath());
                fileInfo.setFileType("image");
                fileInfo.setSummary(summary);
                fileInfoRepository.save(fileInfo);
                log.info("[视觉] 识别结果已保存到资料区域");
            } catch (Exception e) {
                log.error("[视觉] 保存到资料区域失败", e);
            }
        }
    }

    private File downloadImage(String url, Long messageId, String fileName) {
        try {
            Path dir = Paths.get(storagePath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            String safeName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
            File target = new File(dir.toFile(), messageId + "_" + safeName);

            byte[] bytes = restTemplate.getForObject(url, byte[].class);
            if (bytes == null) {
                return null;
            }
            Files.write(target.toPath(), bytes);
            log.info("[视觉] 图片下载成功: {} ({})", fileName, bytes.length);
            return target;
        } catch (Exception e) {
            log.error("[视觉] 图片下载异常", e);
            return null;
        }
    }
}
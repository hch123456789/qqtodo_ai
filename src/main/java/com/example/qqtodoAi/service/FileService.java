package com.example.qqtodoAi.service;

import com.example.qqtodoAi.model.FileInfo;
import com.example.qqtodoAi.model.Message;
import com.example.qqtodoAi.repository.FileInfoRepository;
import com.example.qqtodoAi.repository.GroupInfoRepository;
import com.example.qqtodoAi.repository.MessageRepository;
import com.example.qqtodoAi.util.FileUtils;
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
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final GroupInfoRepository groupInfoRepository;
    private final MessageRepository messageRepository;
    private final FileInfoRepository fileInfoRepository;
    private final FileParseService fileParseService;
    private final AiService aiService;
    private final VisionService visionService;
    private final RestTemplate restTemplate;

    @Value("${file.storage-path:./files}")
    private String storagePath;

    public void handleFileMessage(JsonNode event) {
        long groupId = event.path("group_id").asLong(0);
        long senderQq = event.path("user_id").asLong(0);

        if (!groupInfoRepository.existsByGroupIdAndEnabledTrue(groupId)) {
            log.info("[文件] 群 {} 不在白名单，忽略", groupId);
            return;
        }

        Message msg = new Message();
        msg.setMessageId(event.path("message_id").asLong(0));
        msg.setGroupId(groupId);
        msg.setSenderQq(senderQq);
        msg.setContent("[文件消息] " + extractFileNames(event.path("message")));
        msg.setMsgTime(LocalDateTime.now());
        msg.setRawJson(event.toString());
        messageRepository.save(msg);
        log.info("[文件] 已保存文件消息记录 id={}", msg.getId());

        JsonNode messageArray = event.path("message");
        if (!messageArray.isArray()) {
            return;
        }

        for (JsonNode segment : messageArray) {
            if (!"file".equals(segment.path("type").asText())) {
                continue;
            }
            JsonNode data = segment.path("data");
            String fileName = data.path("file").asText("未知文件");
            String fileUrl = data.path("url").asText("");
            processSingleFile(msg.getId(), groupId, senderQq, fileName, fileUrl);
        }
    }

    private void processSingleFile(Long messageId, Long groupId, Long senderQq,
                                   String fileName, String fileUrl) {
        try {
            File localFile = downloadFile(fileUrl, messageId, fileName);
            if (localFile == null) {
                log.warn("[文件] 下载失败: {}", fileName);
                return;
            }

            String ext = FileUtils.getExtension(fileName).toLowerCase();

            if (isImage(ext)) {
                processImageFile(messageId, groupId, senderQq, fileName, localFile);
                return;
            }

            String text = fileParseService.extractText(localFile, fileName);
            if (text == null || text.isBlank()) {
                log.warn("[文件] 无法提取文本: {}", fileName);
                return;
            }

            String summary = aiService.summarizeFile(fileName, text);
            log.info("[文件] AI 总结完成: {}", fileName);

            saveFileInfo(messageId, groupId, senderQq, fileName, localFile, summary);

        } catch (Exception e) {
            log.error("[文件] 处理失败: {}", fileName, e);
        }
    }

    private void processImageFile(Long messageId, Long groupId, Long senderQq,
                                  String fileName, File localFile) {
        String text = visionService.recognize(localFile);
        if (text == null || text.isBlank()) {
            log.warn("[文件] 图片未识别出文字: {}", fileName);
            return;
        }

        String summary = aiService.summarizeFile(fileName, text);
        log.info("[文件] 图片识别并总结完成: {}", fileName);

        saveFileInfo(messageId, groupId, senderQq, fileName, localFile, summary);
    }

    private void saveFileInfo(Long messageId, Long groupId, Long senderQq,
                              String fileName, File localFile, String summary) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setMessageId(messageId);
        fileInfo.setGroupId(groupId);
        fileInfo.setSenderQq(senderQq);
        fileInfo.setFileName(fileName);
        fileInfo.setFilePath(localFile.getAbsolutePath());
        fileInfo.setFileType(FileUtils.getExtension(fileName));
        fileInfo.setSummary(summary);
        fileInfoRepository.save(fileInfo);
        log.info("[文件] 已入库: {}", fileName);
    }

    private File downloadFile(String fileUrl, Long messageId, String fileName) {
        try {
            Path dir = Paths.get(storagePath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            String safeName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
            File target = new File(dir.toFile(), messageId + "_" + safeName);

            byte[] bytes = restTemplate.getForObject(fileUrl, byte[].class);
            if (bytes == null) {
                return null;
            }
            Files.write(target.toPath(), bytes);
            log.info("[文件] 下载成功: {} ({})", fileName, bytes.length);
            return target;
        } catch (Exception e) {
            log.error("[文件] 下载异常: {}", fileUrl, e);
            return null;
        }
    }

    private String extractFileNames(JsonNode messageArray) {
        StringBuilder sb = new StringBuilder();
        if (messageArray != null && messageArray.isArray()) {
            for (JsonNode segment : messageArray) {
                if ("file".equals(segment.path("type").asText())) {
                    String name = segment.path("data").path("file").asText("");
                    if (!name.isBlank()) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }
                        sb.append(name);
                    }
                }
            }
        }
        return sb.length() > 0 ? sb.toString() : "未知文件";
    }

    private boolean isImage(String ext) {
        return "png".equals(ext) || "jpg".equals(ext) || "jpeg".equals(ext)
                || "gif".equals(ext) || "webp".equals(ext) || "bmp".equals(ext);
    }
}
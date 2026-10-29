package com.example.qqtodoAi.controller;

import com.example.qqtodoAi.model.Feedback;
import com.example.qqtodoAi.model.FileInfo;
import com.example.qqtodoAi.repository.FeedbackRepository;
import com.example.qqtodoAi.repository.FileInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final FileInfoRepository fileInfoRepository;
    private final FeedbackRepository feedbackRepository;

    @Value("${file.storage-path:./files}")
    private String storagePath;

    /**
     * 删除文件记录 + 本地文件
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        FileInfo f = fileInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在: " + id));
        try {
            Files.deleteIfExists(Paths.get(f.getFilePath()));
            log.info("[文件] 已删除本地文件: {}", f.getFilePath());
        } catch (Exception e) {
            log.warn("[文件] 删除本地文件失败", e);
        }
        fileInfoRepository.deleteById(id);
        return Map.of("success", true, "message", "已删除");
    }

    /**
     * 下载原文件（强制下载，避免乱码；中文文件名正确编码）
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<?> download(@PathVariable Long id) {
        FileInfo f = fileInfoRepository.findById(id).orElse(null);
        if (f == null) {
            return ResponseEntity.status(404).body("文件记录不存在");
        }

        File file = findFile(f);
        if (file == null || !file.exists()) {
            log.warn("[文件] 文件不存在: {}", f.getFilePath());
            return ResponseEntity.status(404).body("文件不存在或已被删除");
        }

        Resource resource = new FileSystemResource(file);

        // 正确编码中文文件名（filename*=UTF-8''）
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(f.getFileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * 找文件：先按存储路径，找不到再兜底
     */
    private File findFile(FileInfo f) {
        File file = new File(f.getFilePath());
        if (file.exists()) {
            return file;
        }
        // 兜底：用 storagePath + 文件名
        Path fallback = Paths.get(storagePath).resolve(f.getFileName());
        File fb = fallback.toFile();
        if (fb.exists()) {
            return fb;
        }
        return null;
    }

    /**
     * 对文件摘要/识别结果提交反馈
     */
    @PostMapping("/{id}/feedback")
    public Map<String, Object> feedback(@PathVariable Long id,
                                        @RequestBody Map<String, String> body) {
        FileInfo f = fileInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在: " + id));

        Feedback fb = new Feedback();
        fb.setMessageId(f.getMessageId());
        fb.setFileInfoId(id);
        fb.setUserFeedback("file_feedback");
        fb.setUserNote(body.getOrDefault("note", ""));
        feedbackRepository.save(fb);

        log.info("[反馈] 文件反馈: {} -> {}", f.getFileName(), fb.getUserNote());
        return Map.of("success", true, "message", "反馈已提交");
    }
}

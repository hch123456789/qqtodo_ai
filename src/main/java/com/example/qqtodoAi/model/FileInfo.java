package com.example.qqtodoAi.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "file_info")
public class FileInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "sender_qq")
    private Long senderQq;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_type")
    private String fileType;

    @Column(columnDefinition = "LONGTEXT")
    private String summary;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

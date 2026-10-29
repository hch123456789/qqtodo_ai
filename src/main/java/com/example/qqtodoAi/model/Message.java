package com.example.qqtodoAi.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "sender_qq")
    private Long senderQq;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "msg_time")
    private LocalDateTime msgTime;

    @Column(name = "raw_json", columnDefinition = "LONGTEXT")
    private String rawJson;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}

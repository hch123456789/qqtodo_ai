package com.example.qqtodoAi.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "todo_item_id")
    private Long todoItemId;

    @Column(name = "ai_relevant")
    private Boolean aiRelevant;

    @Column(name = "ai_result", columnDefinition = "TEXT")
    private String aiResult;

    @Column(name = "user_feedback")
    private String userFeedback;

    @Column(name = "relevance_score")
    private Integer relevanceScore;

    @Column(name = "user_note", columnDefinition = "TEXT")
    private String userNote;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "file_info_id")
    private Long fileInfoId;

}

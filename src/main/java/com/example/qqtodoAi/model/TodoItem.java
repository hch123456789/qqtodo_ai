package com.example.qqtodoAi.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "todo_item")
public class TodoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "LONGTEXT")
    private String task;

    private LocalDateTime time;

    @Column(name = "reminded")
    @ColumnDefault("false")
    private Boolean reminded = false;

    private String location;

    private String source;

    private String priority;

    @Column(name = "status")
    @ColumnDefault("false")
    private Boolean completed = false;

    @Column(name = "message_id")
    private Long messageId;

    @Column(name = "confirmed")
    @ColumnDefault("false")
    private Boolean confirmed = false;

    @Column(name = "rejected")
    @ColumnDefault("false")
    private Boolean rejected = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
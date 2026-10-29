package com.example.qqtodoAi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "group_info")
public class GroupInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", unique = true, nullable = false)
    private Long groupId;

    @Column(name = "group_name")
    private String groupName;

    private String description;

    private Boolean enabled = true;
}

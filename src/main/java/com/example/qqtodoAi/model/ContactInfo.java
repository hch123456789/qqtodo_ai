package com.example.qqtodoAi.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "contact_info")
public class ContactInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long qq;

    private String name;

    private String role;

    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "is_marked")
    private Boolean isMarked = false;
}

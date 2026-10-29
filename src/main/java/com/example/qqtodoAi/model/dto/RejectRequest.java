package com.example.qqtodoAi.model.dto;

import lombok.Data;

@Data
public class RejectRequest {
    private Integer relevanceScore;
    private String note;
}

package com.example.qqtodoAi.model.dto;

import lombok.Data;

@Data
public class CorrectRequest {
    private String task;
    private String time;
    private String location;
    private String priority;
    private String note;
}

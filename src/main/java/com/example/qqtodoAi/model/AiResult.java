package com.example.qqtodoAi.model;

import lombok.Data;

@Data
public class AiResult {
    private boolean relevant;
    private String task;
    private String time;
    private String location;
    private String source;
    private String priority;
    private String reason;
}

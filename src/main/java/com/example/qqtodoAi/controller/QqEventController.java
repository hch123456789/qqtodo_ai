package com.example.qqtodoAi.controller;

import com.example.qqtodoAi.service.QqEventService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class QqEventController {

    private final QqEventService qqEventService;

    @Value("${qq.event-token:}")
    private String eventToken;

    @PostMapping("/qq/event")
    public Map<String, Object> handleEvent(@RequestBody JsonNode event,
                                           @RequestHeader(value = "X-Event-Token", required = false) String token) {
        if (eventToken != null && !eventToken.isBlank() && !eventToken.equals(token)) {
            return Map.of("error", "invalid token");
        }
        qqEventService.handleEventAsync(event);
        return Collections.emptyMap();
    }
}
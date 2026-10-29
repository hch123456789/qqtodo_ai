package com.example.qqtodoAi.service;

import com.example.qqtodoAi.service.handler.MessageEventHandler;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QqEventService {

    private final MessageEventHandler messageEventHandler;

    @Async("qqEventExecutor")
    public void handleEventAsync(JsonNode event) {
        try {
            String postType = event.path("post_type").asText();

            if ("meta_event".equals(postType)) {
                return;
            }

            if ("message".equals(postType)) {
                messageEventHandler.handle(event);
                return;
            }

            log.warn("未处理的事件类型: {}", postType);

        } catch (Exception e) {
            log.error("处理 QQ 事件失败", e);
        }
    }
}

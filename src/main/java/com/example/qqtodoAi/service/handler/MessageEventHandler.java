package com.example.qqtodoAi.service.handler;

import com.example.qqtodoAi.service.FileService;
import com.example.qqtodoAi.service.ImageService;
import com.example.qqtodoAi.service.MessageService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventHandler {

    private final MessageService messageService;
    private final FileService fileService;
    private final ImageService imageService;

    public void handle(JsonNode event) {
        String messageType = event.path("message_type").asText();

        if ("group".equals(messageType)) {
            if (hasSegmentOfType(event, "file")) {
                fileService.handleFileMessage(event);
            } else if (hasSegmentOfType(event, "image")) {
                imageService.handleImageMessage(event);
            } else {
                messageService.handleGroupMessage(event);
            }
        } else if ("private".equals(messageType)) {
            log.info("[跳过] 私聊消息暂不处理");
        }
    }

    private boolean hasSegmentOfType(JsonNode event, String type) {
        JsonNode messageArray = event.path("message");
        if (!messageArray.isArray()) {
            return false;
        }
        for (JsonNode segment : messageArray) {
            if (type.equals(segment.path("type").asText())) {
                return true;
            }
        }
        return false;
    }
}
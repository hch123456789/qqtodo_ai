package com.example.qqtodoAi.service;

import com.example.qqtodoAi.model.TodoItem;
import com.example.qqtodoAi.repository.TodoItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final TodoItemRepository todoItemRepository;
    private final RestTemplate restTemplate;

    @Value("${qq.http-api-url:http://127.0.0.1:3000}")
    private String qqApiUrl;

    @Value("${qq.self-id}")
    private Long selfId;

    @Value("${qq.reminder-hours:24}")
    private int reminderHours;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * 每小时检查一次
     */
    @Scheduled(fixedRate = 1200000)
    public void checkAndRemind() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusHours(reminderHours);

        List<TodoItem> dueList = todoItemRepository.findDueReminders(now, deadline);

        if (dueList.isEmpty()) {
            log.info("[提醒] 暂无临近截止的待办");
            return;
        }

        log.info("[提醒] 发现 {} 条临近截止的待办", dueList.size());

        for (TodoItem todo : dueList) {
            boolean ok = sendPrivateMsg(selfId, buildMessage(todo));
            if (ok) {
                todo.setReminded(true);
                todoItemRepository.save(todo);
                log.info("[提醒] 已提醒: {}", todo.getTask());
            }
        }
    }

    private String buildMessage(TodoItem todo) {
        StringBuilder sb = new StringBuilder();
        sb.append("【待办提醒】\n");
        sb.append("任务：").append(todo.getTask()).append("\n");
        if (todo.getTime() != null) {
            sb.append("截止：").append(todo.getTime().format(TIME_FORMAT)).append("\n");
        }
        if (todo.getLocation() != null && !todo.getLocation().isBlank()) {
            sb.append("地点：").append(todo.getLocation()).append("\n");
        }
        if (todo.getSource() != null && !todo.getSource().isBlank()) {
            sb.append("来源：").append(todo.getSource());
        }
        return sb.toString();
    }

    /**
     * 通过 NapCat HTTP API 发送私聊消息
     */
    private boolean sendPrivateMsg(Long userId, String message) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("user_id", userId);
            body.put("message", message);

            String url = qqApiUrl + "/send_private_msg";
            ResponseEntity<Map> resp = restTemplate.postForEntity(url, body, Map.class);

            Map<?, ?> result = resp.getBody();
            if (result != null && "ok".equals(result.get("status"))) {
                return true;
            }
            log.warn("[提醒] 发送失败，返回: {}", result);
            return false;
        } catch (Exception e) {
            log.error("[提醒] 发送异常，请检查 NapCat HTTP API 是否配置", e);
            return false;
        }
    }
}

package com.example.qqtodoAi.service;

import com.example.qqtodoAi.model.Feedback;
import com.example.qqtodoAi.model.TodoItem;
import com.example.qqtodoAi.model.dto.CorrectRequest;
import com.example.qqtodoAi.model.dto.RejectRequest;
import com.example.qqtodoAi.repository.FeedbackRepository;
import com.example.qqtodoAi.repository.TodoItemRepository;
import com.example.qqtodoAi.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoItemRepository todoItemRepository;
    private final FeedbackRepository feedbackRepository;

    public List<TodoItem> listTodos() {
        return todoItemRepository.findByRejectedFalseOrderByTimeAsc();
    }

    public TodoItem confirm(Long id) {
        TodoItem todo = getTodo(id);
        todo.setConfirmed(true);
        todo.setCompleted(false);
        todoItemRepository.save(todo);

        saveFeedback(todo.getMessageId(), id, true, todo.getTask(),
                "confirm", null, null);
        log.info("[反馈] 确认 todo: {}", todo.getTask());
        return todo;
    }

    public void reject(Long id, RejectRequest req) {
        TodoItem todo = getTodo(id);
        todo.setRejected(true);
        todoItemRepository.save(todo);

        saveFeedback(todo.getMessageId(), id, true, todo.getTask(),
                "reject", req.getRelevanceScore(), req.getNote());
        log.info("[反馈] 拒绝 todo: {} (不相关程度={})", todo.getTask(), req.getRelevanceScore());
    }

    public TodoItem correct(Long id, CorrectRequest req) {
        TodoItem todo = getTodo(id);
        String originalTask = todo.getTask();

        if (req.getTask() != null && !req.getTask().isBlank()) {
            todo.setTask(req.getTask());
        }
        if (req.getTime() != null && !req.getTime().isBlank()) {
            LocalDateTime t = TimeUtils.parse(req.getTime());
            if (t != null) {
                todo.setTime(t);
            }
        }
        if (req.getLocation() != null) {
            todo.setLocation(req.getLocation());
        }
        if (req.getPriority() != null) {
            todo.setPriority(req.getPriority());
        }
        todoItemRepository.save(todo);

        saveFeedback(todo.getMessageId(), id, true, originalTask,
                "correct", null, req.getNote());
        log.info("[反馈] 修正 todo: {} -> {}", originalTask, todo.getTask());
        return todo;
    }

    private TodoItem getTodo(Long id) {
        return todoItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("todo 不存在: " + id));
    }

    private void saveFeedback(Long messageId, Long todoItemId, Boolean aiRelevant,
                              String aiResult, String userFeedback,
                              Integer relevanceScore, String userNote) {
        Feedback f = new Feedback();
        f.setMessageId(messageId);
        f.setTodoItemId(todoItemId);
        f.setAiRelevant(aiRelevant);
        f.setAiResult(aiResult);
        f.setUserFeedback(userFeedback);
        f.setRelevanceScore(relevanceScore);
        f.setUserNote(userNote);
        feedbackRepository.save(f);
    }
}
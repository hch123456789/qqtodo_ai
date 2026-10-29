package com.example.qqtodoAi.controller;

import com.example.qqtodoAi.model.TodoItem;
import com.example.qqtodoAi.model.dto.CorrectRequest;
import com.example.qqtodoAi.model.dto.RejectRequest;
import com.example.qqtodoAi.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todo")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @GetMapping
    public List<TodoItem> list() {
        return todoService.listTodos();
    }

    @PostMapping("/{id}/confirm")
    public Map<String, Object> confirm(@PathVariable Long id) {
        todoService.confirm(id);
        return Map.of("success", true, "message", "已确认");
    }

    @PostMapping("/{id}/reject")
    public Map<String, Object> reject(@PathVariable Long id,
                                      @RequestBody RejectRequest request) {
        todoService.reject(id, request);
        return Map.of("success", true, "message", "已拒绝");
    }

    @PostMapping("/{id}/correct")
    public Map<String, Object> correct(@PathVariable Long id,
                                       @RequestBody CorrectRequest request) {
        todoService.correct(id, request);
        return Map.of("success", true, "message", "已修正");
    }
}

package com.example.qqtodoAi.repository;

import com.example.qqtodoAi.model.TodoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {

    List<TodoItem> findByRejectedFalseOrderByTimeAsc();

    @Query("SELECT t FROM TodoItem t " +
            "WHERE t.rejected = false " +
            "AND t.completed = false " +
            "AND t.time IS NOT NULL " +
            "AND t.time BETWEEN :now AND :deadline " +
            "AND t.reminded = false")
    List<TodoItem> findDueReminders(@Param("now") LocalDateTime now,
                                    @Param("deadline") LocalDateTime deadline);
}
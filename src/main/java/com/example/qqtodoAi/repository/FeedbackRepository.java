package com.example.qqtodoAi.repository;

import com.example.qqtodoAi.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findTop10ByUserFeedbackOrderByIdDesc(String userFeedback);
    List<Feedback> findTop5ByUserFeedbackOrderByIdDesc(String userFeedback);
    List<Feedback> findTop50ByOrderByIdDesc();
}

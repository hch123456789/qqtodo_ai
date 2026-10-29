package com.example.qqtodoAi.repository;

import com.example.qqtodoAi.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findTop50ByOrderByIdDesc();
}

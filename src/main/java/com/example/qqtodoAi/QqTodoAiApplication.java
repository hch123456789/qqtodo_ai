package com.example.qqtodoAi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QqTodoAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(QqTodoAiApplication.class, args);
    }
}

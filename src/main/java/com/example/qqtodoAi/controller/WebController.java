package com.example.qqtodoAi.controller;

import com.example.qqtodoAi.model.UserProfile;
import com.example.qqtodoAi.repository.*;
import com.example.qqtodoAi.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class WebController {
    private final GroupInfoRepository groupInfoRepository;
    private final ContactInfoRepository contactInfoRepository;
    private final TodoService todoService;
    private final MessageRepository messageRepository;
    private final FileInfoRepository fileInfoRepository;
    private final UserProfileRepository userProfileRepository;

    @GetMapping("/groups")
    public String groups(Model model) {
        model.addAttribute("groups", groupInfoRepository.findAllByOrderByIdDesc());
        return "groups";
    }

    @GetMapping("/contacts")
    public String contacts(Model model) {
        model.addAttribute("contacts", contactInfoRepository.findAllByOrderByIdDesc());
        return "contacts";
    }
    @GetMapping("/")
    public String todos(Model model) {
        model.addAttribute("todos", todoService.listTodos());
        return "todos";
    }

    @GetMapping("/messages")
    public String messages(Model model) {
        model.addAttribute("messages", messageRepository.findTop50ByOrderByIdDesc());
        return "messages";
    }

    @GetMapping("/files")
    public String files(Model model) {
        model.addAttribute("files", fileInfoRepository.findTop50ByOrderByIdDesc());
        return "files";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("identity", getProfileValue("base_identity"));
        model.addAttribute("rules", getProfileValue("learned_rules"));
        return "profile";
    }

    private String getProfileValue(String key) {
        return userProfileRepository.findByProfileKey(key)
                .map(UserProfile::getProfileValue)
                .orElse("");
    }
}

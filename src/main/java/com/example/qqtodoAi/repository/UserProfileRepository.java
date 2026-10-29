package com.example.qqtodoAi.repository;

import com.example.qqtodoAi.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByProfileKey(String profileKey);
}

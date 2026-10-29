package com.example.qqtodoAi.repository;

import com.example.qqtodoAi.model.GroupInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupInfoRepository extends JpaRepository<GroupInfo, Long> {

    boolean existsByGroupIdAndEnabledTrue(Long groupId);

    Optional<GroupInfo> findByGroupId(Long groupId);
    List<GroupInfo> findAllByOrderByIdDesc();

}

package com.example.qqtodoAi.repository;

import com.example.qqtodoAi.model.FileInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileInfoRepository extends JpaRepository<FileInfo, Long> {

    List<FileInfo> findTop50ByOrderByIdDesc();
}
